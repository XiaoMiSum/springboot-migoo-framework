package xyz.migoo.framework.security.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * OAuth2 Resource Server 自动配置
 * <p>
 * 仅在 mode=oauth2 且 classpath 存在 JwtDecoder 时生效。
 * 提供 JwtDecoder Bean，用于解析外部授权服务器签发的 JWT token。
 *
 * @author xiaomi
 */
@Configuration
@ConditionalOnProperty(name = "migoo.security.mode", havingValue = "oauth2")
@ConditionalOnClass(name = "org.springframework.security.oauth2.jwt.JwtDecoder")
@EnableConfigurationProperties(SecurityProperties.class)
public class OAuth2ResourceServerAutoConfiguration {

    /**
     * 创建 JwtDecoder Bean
     * <p>
     * 优先使用 issuer-uri (自动发现 JWK Set)，其次使用 jwk-set-uri (直接指定)
     */
    @Bean
    @ConditionalOnMissingBean
    public JwtDecoder jwtDecoder(SecurityProperties properties) {
        SecurityProperties.OAuth2 oauth2 = properties.getOauth2();
        if (oauth2.getIssuerUri() != null && !oauth2.getIssuerUri().isBlank()) {
            return NimbusJwtDecoder.withIssuerLocation(oauth2.getIssuerUri()).build();
        }
        if (oauth2.getJwkSetUri() != null && !oauth2.getJwkSetUri().isBlank()) {
            return NimbusJwtDecoder.withJwkSetUri(oauth2.getJwkSetUri()).build();
        }
        throw new IllegalStateException(
                "OAuth2 Resource Server 模式必须配置 migoo.security.oauth2.issuer-uri 或 migoo.security.oauth2.jwk-set-uri");
    }

}
