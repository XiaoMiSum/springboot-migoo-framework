package xyz.migoo.framework.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

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

    @Data
    public static class Cors {

        /**
         * 是否启用 CORS 过滤器
         */
        private boolean enabled = true;

        /**
         * 允许的来源（支持 * 通配符）
         */
        private List<String> allowedOrigins = List.of("*");

        /**
         * 允许的请求方法
         */
        private List<String> allowedMethods = List.of("*");

        /**
         * 允许的请求头
         */
        private List<String> allowedHeaders = List.of("*");

        /**
         * 是否允许携带凭证
         */
        private boolean allowCredentials = true;

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
