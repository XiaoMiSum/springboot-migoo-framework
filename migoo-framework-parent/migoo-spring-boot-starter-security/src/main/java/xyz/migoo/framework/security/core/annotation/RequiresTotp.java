package xyz.migoo.framework.security.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要 TOTP 二次验证的接口
 * <p>
 * 在 Controller 方法上使用此注解，TotpInterceptor 将拦截此方法进行 TOTP 验证
 *
 * @author xiaomi
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresTotp {
}
