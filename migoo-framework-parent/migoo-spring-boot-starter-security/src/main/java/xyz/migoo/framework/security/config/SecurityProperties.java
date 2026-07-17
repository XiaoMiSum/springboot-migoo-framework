package xyz.migoo.framework.security.config;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "migoo.security")
@Validated
@Data
public class SecurityProperties {

    /**
     * 安全模式
     * <p>
     * JWT: 使用自定义 JWT Filter + AuthUserDetailsFetcher (默认)
     * OAUTH2: 使用 Spring OAuth2 Resource Server
     */
    private SecurityMode mode = SecurityMode.JWT;

    /**
     * 登出url
     */
    @NotEmpty(message = "logout-url 登出url地址不能为空")
    private String logoutUrl;

    /**
     * 用户可以任意访问的url
     */
    @NotNull(message = "permit-all-urls 允许任意访问的url不能为空")
    private List<String> permitAllUrls = new ArrayList<>();

    /**
     * JWT 模式配置（仅 mode=jwt 时生效）
     */
    @NotNull(message = "jwt 不能为空")
    private Jwt jwt = new Jwt();

    /**
     * OAuth2 模式配置（仅 mode=oauth2 时生效）
     */
    @NotNull(message = "oauth2 不能为空")
    private OAuth2 oauth2 = new OAuth2();

    /**
     * 条件校验
     */
    @PostConstruct
    public void validate() {
        if (mode == SecurityMode.JWT) {
            if (jwt.getSecretKey() == null || jwt.getSecretKey().isBlank()) {
                throw new IllegalStateException("JWT 模式下 migoo.security.jwt.secret-key 不能为空");
            }
        }
        if (mode == SecurityMode.OAUTH2) {
            boolean hasIssuer = oauth2.getIssuerUri() != null && !oauth2.getIssuerUri().isBlank();
            boolean hasJwk = oauth2.getJwkSetUri() != null && !oauth2.getJwkSetUri().isBlank();
            if (!hasIssuer && !hasJwk) {
                throw new IllegalStateException("OAuth2 模式下 migoo.security.oauth2.issuer-uri 或 jwk-set-uri 至少配置一个");
            }
        }
    }

    // ==================== JWT 模式配置 ====================

    @Validated
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Jwt {

        /**
         * token 秘钥（HMAC-SHA256）
         */
        @NotEmpty(message = "jwt.secret-key 不能为空")
        private String secretKey;

        /**
         * HTTP 请求时，访问令牌的请求 Header
         */
        @NotEmpty(message = "jwt.header-name 不能为空")
        private String headerName = "Authorization";

        /**
         * access token 过期时间
         * <p>
         * 默认 30 分钟
         */
        @NotNull(message = "jwt.access-token-expires 不能为空")
        private Duration accessTokenExpires = Duration.ofMinutes(30);

        /**
         * refresh token 过期时间
         * <p>
         * 默认 7 天
         */
        @NotNull(message = "jwt.refresh-token-expires 不能为空")
        private Duration refreshTokenExpires = Duration.ofDays(7);

        /**
         * refresh token 请求 Header
         * <p>
         * 默认：X-Refresh-Token
         */
        @NotEmpty(message = "jwt.refresh-header-name 不能为空")
        private String refreshHeaderName = "X-Refresh-Token";
    }

    // ==================== OAuth2 模式配置 ====================

    @Validated
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OAuth2 {

        /**
         * 授权服务器 issuer URI
         * <p>
         * 用于 JwtDecoder 自动发现 JWK Set
         */
        private String issuerUri;

        /**
         * JWK Set URI
         * <p>
         * 直接指定 JWK Set 端点地址 (与 issuerUri 二选一)
         */
        private String jwkSetUri;
    }

    // ==================== 枚举 ====================

    public enum SecurityMode {
        /**
         * JWT 模式: 使用自定义 JWT Filter + AuthUserDetailsFetcher
         */
        JWT,
        /**
         * OAuth2 模式: 使用 Spring OAuth2 Resource Server
         */
        OAUTH2
    }
}
