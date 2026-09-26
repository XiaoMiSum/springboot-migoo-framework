package xyz.migoo.framework.web.core.idempotent;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.web.bind.annotation.RequestBody;
import xyz.migoo.framework.common.exception.GlobalErrorCodeConstants;
import xyz.migoo.framework.common.exception.ServiceExceptionUtil;
import xyz.migoo.framework.common.util.JsonUtils;
import xyz.migoo.framework.web.core.annotation.Idempotent;
import xyz.migoo.framework.web.core.store.StateStore;
import xyz.migoo.framework.web.core.util.WebFrameworkUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 幂等切面（防重复提交）
 * <p>
 * 拦截标注了 {@link Idempotent} 的方法/类：
 * <ol>
 *     <li>按 {@code migoo:idempotent:类名#方法名:维度值} 构造幂等键，原子占位
 *     （处理中短占位窗口 {@value #CLAIM_SECONDS} 秒，崩溃后可自动恢复）；</li>
 *     <li>占位失败 = 防重窗口内的重复请求 → 抛 900
 *     （{@link GlobalErrorCodeConstants#REPEATED_REQUESTS}）；</li>
 *     <li>执行成功 → 占位升级为完整防重窗口；执行失败 → 释放占位允许重试
 *     （与 MQ 幂等拦截器「失败删标」语义一致）。</li>
 * </ol>
 * 存储复用限流的 {@link StateStore}：单机默认内存实现，检测到 Redis 自动多实例共享。
 *
 * @author xiaomi
 */
@Aspect
@Slf4j
public class IdempotentAspect {

    /**
     * 幂等键前缀
     */
    static final String KEY_PREFIX = "migoo:idempotent:";

    /**
     * 处理中占位窗口（秒）：业务执行异常/进程崩溃后，键最多存在此时长即可重试
     */
    static final long CLAIM_SECONDS = 30L;

    /**
     * 无可摘要参数时的维度占位（退化为「登录用户 + 方法」级幂等）
     */
    private static final String NO_ARGS = "noargs";

    /**
     * SpEL 表达式解析器
     */
    private static final ExpressionParser PARSER = new SpelExpressionParser();

    /**
     * 方法参数名解析器（支持 #p0/#a0 下标与参数名占位符）
     */
    private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    private final StateStore stateStore;

    /**
     * SpEL 表达式缓存（key: 方法 + 表达式）
     */
    private final Map<String, Expression> expressionCache = new ConcurrentHashMap<>();

    public IdempotentAspect(StateStore stateStore) {
        this.stateStore = stateStore;
    }

    @Around(value = "@annotation(idempotent) || @within(idempotent)", argNames = "joinPoint,idempotent")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        Duration expire = Duration.ofSeconds(Math.max(idempotent.expire(), 1));
        Duration claimTtl = expire.compareTo(Duration.ofSeconds(CLAIM_SECONDS)) < 0
                ? expire : Duration.ofSeconds(CLAIM_SECONDS);
        String fullKey = buildKey(idempotent, joinPoint);

        // 首个请求原子占位；占位失败即窗口内重复请求
        if (!stateStore.setIfAbsent(fullKey, claimTtl)) {
            log.warn("[IdempotentAspect][重复请求拦截] key({})", fullKey);
            throw idempotent.message().isBlank()
                    ? ServiceExceptionUtil.get(GlobalErrorCodeConstants.REPEATED_REQUESTS)
                    : ServiceExceptionUtil.get(GlobalErrorCodeConstants.REPEATED_REQUESTS.code(),
                    idempotent.message());
        }
        try {
            Object result = joinPoint.proceed();
            // 成功：占位升级为完整防重窗口
            stateStore.put(fullKey, 1, expire);
            return result;
        } catch (Throwable ex) {
            // 失败：释放占位，允许重试
            stateStore.delete(fullKey);
            throw ex;
        }
    }

    /**
     * 构造幂等键: {@code migoo:idempotent:类名#方法名:维度值}
     */
    private String buildKey(Idempotent idempotent, ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodKey = method.getDeclaringClass().getSimpleName() + "#" + method.getName();
        String dimension = idempotent.key() == null || idempotent.key().isBlank()
                ? defaultDimension(method, joinPoint.getArgs())
                : evaluateKey(idempotent.key(), method, joinPoint.getArgs());
        return KEY_PREFIX + methodKey + ":" + dimension;
    }

    /**
     * 默认维度: 登录用户 + 可摘要参数的 SHA-256 摘要（前 16 位十六进制）
     * <p>
     * 无请求上下文时用户取 anonymous；不同用户、不同入参互不影响。
     */
    private String defaultDimension(Method method, Object[] args) {
        Object userId = WebFrameworkUtils.getLoginUserId();
        String user = userId != null ? String.valueOf(userId) : "anonymous";
        return user + ":" + digestArgs(method, args);
    }

    /**
     * 参数摘要
     * <p>
     * 只序列化 {@code @RequestBody} 参数与简单类型（基本类型/字符串/数值/布尔/枚举/UUID/时间），
     * 跳过 HttpServletRequest、MultipartFile 等容器对象，避免序列化失败或摘要不稳定；
     * 全部不可摘要时返回 {@link #NO_ARGS}（方法级幂等，建议配 SpEL key 精确化）。
     */
    private String digestArgs(Method method, Object[] args) {
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
            return NO_ARGS;
        }
        try {
            String json = JsonUtils.toJsonString(payload);
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(json.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest, 0, 8);
        } catch (Exception ex) {
            log.warn("[IdempotentAspect][参数摘要失败，退化为方法级幂等] method({})", method, ex);
            return NO_ARGS;
        }
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

    /**
     * 求值自定义 SpEL key
     */
    private String evaluateKey(String keyExpression, Method method, Object[] args) {
        Expression expression = expressionCache.computeIfAbsent(
                method + "|" + keyExpression, k -> PARSER.parseExpression(keyExpression));

        StandardEvaluationContext context = new StandardEvaluationContext();
        String[] names = PARAMETER_NAME_DISCOVERER.getParameterNames(method);
        for (int i = 0; i < args.length; i++) {
            context.setVariable("p" + i, args[i]);
            context.setVariable("a" + i, args[i]);
            if (names != null && i < names.length) {
                context.setVariable(names[i], args[i]);
            }
        }
        Object value = expression.getValue(context);
        return value != null ? String.valueOf(value) : "null";
    }
}
