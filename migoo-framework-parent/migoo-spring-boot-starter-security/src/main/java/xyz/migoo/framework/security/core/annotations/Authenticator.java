package xyz.migoo.framework.security.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在Controller的方法中使用此注解，AuthenticatorInterceptor 将拦截此方法进行身份验证
 *
 * @author xiaomi
 * Created on 2021/11/22 19:41
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Authenticator {
}