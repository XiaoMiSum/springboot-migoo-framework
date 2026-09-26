package xyz.migoo.framework.security.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 安全审计注解
 *
 * <p>标注在方法或类上，由 {@code AuditLogAspect} 拦截并输出双通道审计：</p>
 * <ol>
 *     <li><b>审计日志</b>：以 {@code migoo.audit} 为 logger 名输出一行 JSON
 *     （操作人/客户端 IP/路由模板/动作/成败/入参），可经日志配置独立路由到审计文件；</li>
 *     <li><b>审计事件</b>：发布 {@code AuditLogEvent}，应用自行订阅落库；
 *     可观测组件将其计为 {@code migoo.security.audit.operation} 指标。</li>
 * </ol>
 *
 * <p>入参记录是安全的：只摘要 {@code @RequestBody} 与简单类型参数（自动跳过
 * HttpServletRequest、MultipartFile 等容器对象），序列化经 {@code @Sensitive} 自动掩码，
 * 且字段名含 password/token/secret 等关键词时强制置 {@code ******}。</p>
 *
 * <p>示例:
 * <pre>
 * // 默认动作（类名#方法名）+ 记录入参
 * {@code @AuditLog}
 * {@code @PostMapping}
 * public Result createOrder(@RequestBody OrderCreateReqBody req) { ... }
 *
 * // 指定低基数动作（作指标 tag 用，避免放入单号等高基数值）
 * {@code @AuditLog(action = "用户禁用", recordParams = false)}
 * public Result disableUser(Long userId) { ... }
 * </pre>
 * 总开关: {@code migoo.security.audit.enabled}（默认开启）。
 *
 * @author xiaomi
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    /**
     * 审计动作（会作为指标 tag，须低基数；留空回退 {@code 类名#方法名}）
     */
    String action() default "";

    /**
     * 是否记录入参（脱敏后），默认记录
     */
    boolean recordParams() default true;
}
