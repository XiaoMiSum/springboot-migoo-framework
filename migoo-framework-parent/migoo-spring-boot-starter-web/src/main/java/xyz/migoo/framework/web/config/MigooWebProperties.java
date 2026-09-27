package xyz.migoo.framework.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import xyz.migoo.framework.web.core.cors.CorsMode;

import java.util.List;

/**
 * Web 模块配置属性
 */
@Data
@ConfigurationProperties(prefix = "migoo.web")
public class MigooWebProperties {

    /**
     * CORS 跨域配置
     */
    private Cors cors = new Cors();

    /**
     * 请求体缓存配置
     */
    private CacheBody cacheBody = new CacheBody();

    /**
     * 限流配置（@RateLimit 注解切面）
     */
    private RateLimit rateLimit = new RateLimit();

    /**
     * 幂等配置（@Idempotent 注解切面）
     */
    private Idempotent idempotent = new Idempotent();

    @Data
    public static class RateLimit {

        /**
         * 是否启用 @RateLimit 注解限流切面
         */
        private boolean enabled = true;
    }

    @Data
    public static class Idempotent {

        /**
         * 是否启用 @Idempotent 注解幂等切面（防重复提交）
         */
        private boolean enabled = true;
    }

    @Data
    public static class Cors {

        /**
         * 是否启用 CORS 过滤器
         */
        private boolean enabled = true;

        /**
         * 来源限制模式（见 {@code xyz.migoo.framework.web.core.cors.CorsMode}）
         * <p>
         * STRICT（默认）= 仅放行下方列表中列出的来源，列表为空则不放行任何跨域；
         * OPEN = 放行所有来源但强制关闭凭证（Token 型开放平台 API 适用）；
         * PATTERN = 按 {@link #allowedOriginPatterns} 模式匹配（自有子域名生态适用）；
         * DYNAMIC = 交由应用注册的 CorsOriginPredicate Bean 逐请求判定（动态域名开放平台适用）
         */
        private CorsMode mode = CorsMode.STRICT;

        /**
         * 允许的精确来源列表（STRICT 模式），如 {@code https://admin.example.com}
         * <p>
         * 默认为空 = 不放行任何跨域来源（安全默认）
         */
        private List<String> allowedOrigins = List.of();

        /**
         * 允许的来源模式列表（PATTERN 模式），如 {@code https://*.example.com}
         */
        private List<String> allowedOriginPatterns = List.of();

        /**
         * 允许的请求方法
         */
        private List<String> allowedMethods = List.of("*");

        /**
         * 允许的请求头
         */
        private List<String> allowedHeaders = List.of("*");

        /**
         * 是否允许携带凭证（Cookie/HTTP 认证）
         * <p>
         * 默认关闭。开启时禁止来源为 {@code *} 或 {@code *} 模式（规范不允许，启动时校验拦截）
         */
        private boolean allowCredentials = false;

        /**
         * 预检请求的最大缓存时间（秒）
         */
        private long maxAge = 1800;
    }

    @Data
    public static class CacheBody {

        /**
         * 是否启用请求体缓存过滤器
         */
        private boolean enabled = true;

        /**
         * 最大缓存请求体大小（字节），默认 10MB
         */
        private int maxSize = 10 * 1024 * 1024;
    }
}
