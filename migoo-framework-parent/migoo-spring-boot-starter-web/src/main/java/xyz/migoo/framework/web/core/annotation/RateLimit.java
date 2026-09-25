package xyz.migoo.framework.web.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限流注解
 * <p>
 * 标注在方法或类上，由 {@code RateLimitAspect} 按固定窗口计数限流，
 * 超出限制抛出 429（{@code GlobalErrorCodeConstants.TOO_MANY_REQUESTS}）。
 * <p>
 * 示例:
 * <pre>
 * // 按 IP 限流: 60 秒内最多 100 次
 * {@code @RateLimit(limit = 100, window = 60)}
 * public Result list() { ... }
 *
 * // 按用户名限流（SpEL 引用方法参数），如登录接口防爆破
 * {@code @RateLimit(type = RateLimitType.KEY, key = "#username", limit = 5, window = 60)}
 * public Result login(String username, String password) { ... }
 * </pre>
 * <p>
 * 统计键格式为 {@code 类名#方法名:维度值}，不同方法、不同维度互不影响。
 * 总开关: {@code migoo.web.rate-limit.enabled}（默认开启）。
 *
 * @author xiaomi
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流维度
     */
    RateLimitType type() default RateLimitType.IP;

    /**
     * 自定义限流 key（SpEL 表达式）
     * <p>
     * {@link RateLimitType#KEY} 时必填，支持方法参数名（编译已启用 -parameters）或 #p0/#a0 下标占位符。
     * 其他维度忽略此属性。
     */
    String key() default "";

    /**
     * 统计窗口内允许通过的最大次数
     */
    int limit();

    /**
     * 统计窗口时长（秒）
     */
    long window() default 60L;

    /**
     * 超限时的提示信息（留空使用全局 i18n 消息 common.too.many.requests）
     */
    String message() default "";
}
