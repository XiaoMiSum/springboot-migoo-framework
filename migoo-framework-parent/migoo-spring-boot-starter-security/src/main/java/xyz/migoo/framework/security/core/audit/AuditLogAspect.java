package xyz.migoo.framework.security.core.audit;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestBody;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import xyz.migoo.framework.common.observability.AuditLogEvent;
import xyz.migoo.framework.common.util.JsonUtils;
import xyz.migoo.framework.security.core.annotation.AuditLog;
import xyz.migoo.framework.security.core.util.SecurityFrameworkUtils;
import xyz.migoo.framework.web.core.util.ServletUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * 安全审计切面
 *
 * <p>拦截标注了 {@link AuditLog} 的方法/类，双通道输出：</p>
 * <ol>
 *     <li><b>审计日志</b>：以 logger 名 {@value #AUDIT_LOGGER} 输出一行 JSON
 *     （操作人/客户端 IP/路由模板/动作/成败/入参），日志配置可将该 logger
 *     单独路由到审计文件（如 logback {@code <logger name="migoo.audit" .../>}）；</li>
 *     <li><b>审计事件</b>：发布 {@link AuditLogEvent} 供应用自行落库；
 *     输出/发布失败只记告警，绝不影响业务返回。</li>
 * </ol>
 *
 * <p>入参安全策略：只摘要 {@code @RequestBody} 与简单类型参数，跳过容器对象
 * （HttpServletRequest、MultipartFile 等，避免序列化失败或不稳定）；
 * 序列化经 {@code @Sensitive} 自动掩码，再对字段名含 password/token/secret 等
 * 关键词的字段强制置 {@code ******}，双重兜底。</p>
 *
 * @author xiaomi
 */
@Aspect
@Slf4j
public class AuditLogAspect {

    /**
     * 审计日志专用 logger 名（按名称可独立路由到审计文件）
     */
    static final String AUDIT_LOGGER = "migoo.audit";

    /**
     * 敏感字段脱敏占位（与 SensitiveDataUtil PASSWORD 掩码一致）
     */
    static final String REDACTED = "******";

    /**
     * 敏感字段关键词（字段名含其一即整体置 ******，大小写不敏感）
     */
    private static final Set<String> SENSITIVE_FIELD_KEYWORDS = Set.of(
            "password", "passwd", "secret", "token", "credential", "authorization");

    /**
     * 审计日志输出器
     */
    private static final Logger AUDIT_LOG = LoggerFactory.getLogger(AUDIT_LOGGER);

    private final ApplicationEventPublisher eventPublisher;

    public AuditLogAspect(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * 审计通知
     * <p>
     * Spring 7 起，复合切点（{@code ||}）不再回填注解参数绑定，形参绑定会拿到 {@code null}，
     * 故此处不绑定注解、改由 {@link #resolveAuditLog(ProceedingJoinPoint)} 在方法体内解析；
     * 切点表达式与「方法注解 / 类注解」语义保持不变。
     *
     * @param joinPoint 被切方法
     * @return 被切方法（或其所在类）上的 {@link AuditLog} 注解
     */
    @Around(value = "@annotation(xyz.migoo.framework.security.core.annotation.AuditLog)"
            + " || @within(xyz.migoo.framework.security.core.annotation.AuditLog)",
            argNames = "joinPoint")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        AuditLog auditLog = resolveAuditLog(joinPoint);
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String action = resolveAction(auditLog, method);
        String params = auditLog.recordParams() ? serializeParams(method, joinPoint.getArgs()) : null;

        try {
            Object result = joinPoint.proceed();
            record(action, params, true, null);
            return result;
        } catch (Throwable ex) {
            String errorMessage = ex.getMessage() == null
                    ? ex.getClass().getSimpleName()
                    : ex.getClass().getSimpleName() + ": " + ex.getMessage();
            record(action, params, false, errorMessage);
            throw ex;
        }
    }

    /**
     * 解析被切方法的 {@link AuditLog}：方法注解优先，未标注时取所在类（{@code @within} 语义）。
     * <p>
     * 经 {@link AopUtils#getMostSpecificMethod} 先定位实现类方法，避免 JDK 动态代理下
     * 只看到接口方法而漏掉实现上的注解。
     */
    private AuditLog resolveAuditLog(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object target = joinPoint.getTarget();
        Class<?> targetClass = target != null ? target.getClass() : method.getDeclaringClass();
        Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);

        AuditLog auditLog = AnnotationUtils.findAnnotation(specificMethod, AuditLog.class);
        if (auditLog == null && target != null) {
            auditLog = AnnotationUtils.findAnnotation(targetClass, AuditLog.class);
        }
        if (auditLog == null) {
            // 切点已匹配却解析不到注解，属内部不一致：快速失败便于定位，不静默放行
            throw new IllegalStateException("@AuditLog 切点匹配但未解析到注解: " + joinPoint.getSignature());
        }
        return auditLog;
    }

    /**
     * 双通道输出：专用 logger JSON 行 + 发布事件
     */
    private void record(String action, String params, boolean success, String errorMessage) {
        AuditLogEvent event = new AuditLogEvent(
                resolveOperator(),
                Objects.requireNonNullElse(ServletUtils.getClientIP(), "unknown"),
                ServletUtils.getRoutePattern(),
                action, success, errorMessage, params);

        try {
            // 一行一个 JSON，便于审计流水采集/检索
            AUDIT_LOG.info("{}", JsonUtils.toJsonString(event));
        } catch (Exception ex) {
            log.warn("[AuditLogAspect][审计日志输出失败] action({})", action, ex);
        }
        if (eventPublisher != null) {
            try {
                eventPublisher.publishEvent(event);
            } catch (Exception ex) {
                log.warn("[AuditLogAspect][发布审计事件失败] action({})", action, ex);
            }
        }
    }

    /**
     * 审计动作：显式配置回退 类名#方法名（两者均低基数，可作指标 tag）
     */
    private String resolveAction(AuditLog auditLog, Method method) {
        return auditLog.action().isBlank()
                ? method.getDeclaringClass().getSimpleName() + "#" + method.getName()
                : auditLog.action();
    }

    /**
     * 操作人：{@code id(username)}，未登录为 {@code anonymous}
     */
    private String resolveOperator() {
        var user = SecurityFrameworkUtils.getLoginUser();
        if (user == null) {
            return "anonymous";
        }
        String id = String.valueOf(user.getId());
        String username = user.getUsername();
        return username == null || username.isBlank() ? id : id + "(" + username + ")";
    }

    /**
     * 入参序列化（脱敏后）：只摘要 {@code @RequestBody} 与简单类型参数，
     * 全部不可摘要（纯容器对象参数）时返回 null
     */
    private String serializeParams(Method method, Object[] args) {
        Parameter[] parameters = method.getParameters();
        List<Object> payload = new ArrayList<>(args.length);
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null) {
                continue;
            }
            boolean requestBody = i < parameters.length
                    && parameters[i].isAnnotationPresent(RequestBody.class);
            if (requestBody || isSimpleValue(arg.getClass())) {
                payload.add(arg);
            }
        }
        if (payload.isEmpty()) {
            return null;
        }
        try {
            return redact(JsonUtils.toJsonString(payload));
        } catch (Exception ex) {
            log.warn("[AuditLogAspect][入参序列化失败，审计 params 置空] {}", ex.getMessage());
            return null;
        }
    }

    /**
     * 敏感关键词字段强制脱敏（包级可见便于单测）
     *
     * @param json 入参 JSON
     * @return 脱敏后的 JSON
     */
    static String redact(String json) {
        try {
            JsonNode node = JsonUtils.toJSON(json);
            redactNode(node);
            return node.toString();
        } catch (Exception ex) {
            return json;
        }
    }

    /**
     * 递归遍历对象/数组，命中敏感关键词的字段整体置为 {@link #REDACTED}
     */
    private static void redactNode(JsonNode node) {
        if (node.isObject()) {
            ObjectNode object = (ObjectNode) node;
            // 先收集字段名，避免遍历中修改
            List<String> fieldNames = new ArrayList<>(object.propertyNames());
            for (String name : fieldNames) {
                if (isSensitiveField(name)) {
                    object.put(name, REDACTED);
                } else {
                    redactNode(object.get(name));
                }
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                redactNode(child);
            }
        }
    }

    /**
     * 是否敏感字段名（大小写不敏感的关键词包含匹配）
     */
    static boolean isSensitiveField(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return SENSITIVE_FIELD_KEYWORDS.stream().anyMatch(lower::contains);
    }

    /**
     * 是否简单类型（可稳定 JSON 序列化）
     */
    private static boolean isSimpleValue(Class<?> type) {
        return type.isPrimitive()
                || Number.class.isAssignableFrom(type)
                || CharSequence.class.isAssignableFrom(type)
                || type == Boolean.class
                || type == Character.class
                || type.isEnum()
                || type == UUID.class
                || type.getName().startsWith("java.time.");
    }
}
