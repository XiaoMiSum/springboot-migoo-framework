package xyz.migoo.framework.security.core.authentication;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import xyz.migoo.framework.security.config.SecurityProperties;
import xyz.migoo.framework.security.core.AuthUserDetails;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

/**
 * 基于 spring-security-oauth2-jose 的 JwtTokenProvider 实现
 * <p>
 * 使用 HMAC-SHA256 签名算法，通过 NimbusJwtEncoder/NimbusJwtDecoder 操作 token
 *
 * @author xiaomi
 */
public class JJwtTokenProvider implements JwtTokenProvider {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final SecurityProperties properties;

    public JJwtTokenProvider(SecurityProperties properties) {
        this.properties = properties;
        SecretKey key = buildSecretKey(properties.getJwt().getSecretKey());
        this.jwtEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        this.jwtDecoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Override
    public String createAccessToken(AuthUserDetails<?, ?> user) {
        return createToken(user, "access", properties.getJwt().getAccessTokenExpires());
    }

    @Override
    public String createRefreshToken(AuthUserDetails<?, ?> user) {
        return createToken(user, "refresh", properties.getJwt().getRefreshTokenExpires());
    }

    @Override
    public Jwt parseToken(String token) {
        return jwtDecoder.decode(token);
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            return jwt.getExpiresAt() != null && jwt.getExpiresAt().isAfter(Instant.now());
        } catch (JwtException e) {
            return false;
        }
    }

    @Override
    public String getUserIdFromToken(Jwt jwt) {
        return jwt.getClaimAsString("userId");
    }

    private String createToken(AuthUserDetails<?, ?> user, String type, Duration expiresIn) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .subject(user.getUsername())
                .claim("userId", String.valueOf(user.getId()))
                .claim("type", type)
                .issuedAt(now)
                .expiresAt(now.plus(expiresIn));
        JwtEncoderParameters params = JwtEncoderParameters.from(claimsBuilder.build());
        return jwtEncoder.encode(params).getTokenValue();
    }

    private SecretKey buildSecretKey(String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

}
