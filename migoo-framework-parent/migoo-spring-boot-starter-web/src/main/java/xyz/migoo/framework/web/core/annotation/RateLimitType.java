package xyz.migoo.framework.web.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限流维度类型
 *
 * @author xiaomi
 */
public enum RateLimitType {

    /**
     * 按客户端 IP 限流（默认，取 X-Forwarded-For 等代理头）
     */
    IP,

    /**
     * 按当前登录用户编号限流（从请求属性读取，未登录时归入 anonymous）
     * <p>
     * 注意: 用户编号请求属性由 security 组件在认证通过后写入，OAuth2 模式下请改用 KEY 维度
     */
    USER,

    /**
     * 按自定义 SpEL 表达式限流（如 "#username"、"#request.getHeader('X-Api-Key')"）
     */
    KEY
}
