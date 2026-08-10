package xyz.migoo.framework.security.core.authentication;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import xyz.migoo.framework.security.config.SecurityProperties;
import xyz.migoo.framework.security.core.TestAuthUser;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link JJwtTokenProvider} 单元测试
 * <p>
 * 使用真实 HMAC-SHA256 secret 生成并解析 token，验证完整往返
 */
class JJwtTokenProviderTest {

    /** 测试用 secret，长度必须 >= 32 字节以满足 HS256 要求 */
    private static final String SECRET = "migoo-unit-test-secret-key-0123456789abcdef";

    private SecurityProperties properties;
    private JJwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        properties = new SecurityProperties();
        properties.getJwt().setSecretKey(SECRET);
        tokenProvider = new JJwtTokenProvider(properties);
    }

    @Test
    void createAccessTokenReturnsJwtWithThreeSegments() {
        String token = tokenProvider.createAccessToken(TestAuthUser.of(1001L, "admin"));
        // JWT 由 header.payload.signature 三段组成
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void createRefreshTokenReturnsJwtWithThreeSegments() {
        String token = tokenProvider.createRefreshToken(TestAuthUser.of(1001L, "admin"));
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void accessAndRefreshTokenHaveDifferentTypeClaim() {
        var user = TestAuthUser.of(1001L, "admin");
        Jwt access = tokenProvider.parseToken(tokenProvider.createAccessToken(user));
        Jwt refresh = tokenProvider.parseToken(tokenProvider.createRefreshToken(user));
        assertThat(access.getClaimAsString("type")).isEqualTo("access");
        assertThat(refresh.getClaimAsString("type")).isEqualTo("refresh");
    }

    @Test
    void parseTokenRoundtripReturnsUserClaims() {
        String token = tokenProvider.createAccessToken(TestAuthUser.of(1001L, "admin"));
        Jwt jwt = tokenProvider.parseToken(token);
        // subject 为用户名，userId 为编号
        assertThat(jwt.getSubject()).isEqualTo("admin");
        assertThat(jwt.getClaimAsString("userId")).isEqualTo("1001");
        assertThat(jwt.getClaimAsString("type")).isEqualTo("access");
        // 过期时间存在且在未来
        assertThat(jwt.getExpiresAt()).isNotNull().isAfter(jwt.getIssuedAt());
    }

    @Test
    void getUserIdFromTokenReturnsUserIdClaim() {
        String token = tokenProvider.createAccessToken(TestAuthUser.of(88L, "alice"));
        Jwt jwt = tokenProvider.parseToken(token);
        assertThat(tokenProvider.getUserIdFromToken(jwt)).isEqualTo("88");
    }

    @Test
    void isTokenValidReturnsTrueForValidToken() {
        String token = tokenProvider.createAccessToken(TestAuthUser.of(1001L, "admin"));
        assertThat(tokenProvider.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValidReturnsFalseForTamperedToken() {
        String token = tokenProvider.createAccessToken(TestAuthUser.of(1001L, "admin"));
        // 篡改签名段：替换第一个字符，签名校验必然失败
        String[] segments = token.split("\\.");
        String tamperedSignature = segments[2].charAt(0) == 'A'
                ? 'B' + segments[2].substring(1)
                : 'A' + segments[2].substring(1);
        String tampered = segments[0] + "." + segments[1] + "." + tamperedSignature;
        assertThat(tampered).isNotEqualTo(token);
        assertThat(tokenProvider.isTokenValid(tampered)).isFalse();
    }

    @Test
    void isTokenValidReturnsFalseForGarbageToken() {
        assertThat(tokenProvider.isTokenValid("not.a.jwt")).isFalse();
        assertThat(tokenProvider.isTokenValid("")).isFalse();
        assertThat(tokenProvider.isTokenValid(null)).isFalse();
    }

    @Test
    void isTokenValidReturnsFalseForExpiredToken() {
        // 手工构造签名有效但已过期的 token（issuedAt/expiresAt 均在过去）
        String expired = buildExpiredToken();
        assertThat(tokenProvider.isTokenValid(expired)).isFalse();
    }

    @Test
    void parseTokenThrowsForExpiredToken() {
        String expired = buildExpiredToken();
        assertThatThrownBy(() -> tokenProvider.parseToken(expired)).isInstanceOf(JwtException.class);
    }

    @Test
    void parseTokenThrowsForInvalidToken() {
        assertThatThrownBy(() -> tokenProvider.parseToken("invalid.token.value"))
                .isInstanceOf(JwtException.class);
    }

    /**
     * 使用与 {@link JJwtTokenProvider} 相同的 secret 手工构造一个签名有效、
     * 但 issuedAt/expiresAt 均已过期（expiresAt 仍在 issuedAt 之后）的 token。
     */
    private String buildExpiredToken() {
        SecretKey key = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        NimbusJwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("admin")
                .claim("userId", "1001")
                .claim("type", "access")
                .issuedAt(now.minusSeconds(120))
                .expiresAt(now.minusSeconds(60))
                .build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }
}
