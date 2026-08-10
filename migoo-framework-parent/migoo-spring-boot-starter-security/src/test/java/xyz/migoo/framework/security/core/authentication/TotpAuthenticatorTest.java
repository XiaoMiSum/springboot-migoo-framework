package xyz.migoo.framework.security.core.authentication;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import org.apache.commons.codec.binary.Base32;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import xyz.migoo.framework.common.exception.ServiceException;
import xyz.migoo.framework.security.core.TestAuthUser;
import xyz.migoo.framework.security.core.authentication.TotpAuthenticator.TotpBinding;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link TotpAuthenticator} 单元测试
 * <p>
 * 使用真实 TOTP 算法生成验证码，验证 verify 对正确/错误/过期/空 code 的行为
 */
class TotpAuthenticatorTest {

    private final TotpAuthenticator authenticator = new TotpAuthenticator();

    private final TimeBasedOneTimePasswordGenerator generator = new TimeBasedOneTimePasswordGenerator();

    private final Base32 base32 = new Base32();

    @BeforeEach
    void setUp() {
        // 保证每个测试用例从干净的 SecurityContext 开始
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /** 将指定用户写入 SecurityContext（模拟已登录状态） */
    private static void login(TestAuthUser user) {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }

    /** 从 Base32 secret 还原 HMAC 密钥 */
    private SecretKey restoreKey(String base32Secret) {
        return new SecretKeySpec(base32.decode(base32Secret), generator.getAlgorithm());
    }

    @Test
    void verifyBlankCodeThrowsFailure() {
        // 空/空白 code 直接抛出 2fa 失败异常
        assertThatThrownBy(() -> authenticator.verify(null)).isInstanceOf(ServiceException.class);
        assertThatThrownBy(() -> authenticator.verify("")).isInstanceOf(ServiceException.class);
        assertThatThrownBy(() -> authenticator.verify("   ")).isInstanceOf(ServiceException.class);
    }

    @Test
    void verifyWithoutLoginUserThrowsFailure() {
        // 未登录时，即使 code 非空也抛出异常
        assertThatThrownBy(() -> authenticator.verify("123456")).isInstanceOf(ServiceException.class);
    }

    @Test
    void verifySkippedWhenTwoFactorNotEnabled() {
        // 未开启二次验证的用户，任何 code 都被放行（verify 直接返回）
        TestAuthUser user = TestAuthUser.of(1L, "admin");
        user.setTwoFactorEnabled(false);
        login(user);
        authenticator.verify("000000");
    }

    @Test
    void verifyCorrectCodePasses() throws InvalidKeyException {
        TotpBinding binding = authenticator.generateBinding("user@host", "migoo");
        TestAuthUser user = TestAuthUser.of(1L, "admin");
        user.setTotpSecret(binding.getTotpSecret());
        user.setTwoFactorEnabled(true);
        login(user);

        // 用上一个时间窗口生成验证码，规避窗口边界翻转的偶发失败
        SecretKey key = restoreKey(binding.getTotpSecret());
        String code = generator.generateOneTimePasswordString(key, Instant.now().minus(generator.getTimeStep()));
        authenticator.verify(code);
    }

    @Test
    void verifyWrongCodeThrowsFailure() throws InvalidKeyException {
        TotpBinding binding = authenticator.generateBinding("user@host", "migoo");
        TestAuthUser user = TestAuthUser.of(1L, "admin");
        user.setTotpSecret(binding.getTotpSecret());
        user.setTwoFactorEnabled(true);
        login(user);

        assertThatThrownBy(() -> authenticator.verify("000000")).isInstanceOf(ServiceException.class);
    }

    @Test
    void verifyStaleCodeThrowsFailure() throws InvalidKeyException {
        // 超过 1 个时间步（时钟偏差容差）的旧 code 应当被拒绝
        TotpBinding binding = authenticator.generateBinding("user@host", "migoo");
        TestAuthUser user = TestAuthUser.of(1L, "admin");
        user.setTotpSecret(binding.getTotpSecret());
        user.setTwoFactorEnabled(true);
        login(user);

        SecretKey key = restoreKey(binding.getTotpSecret());
        String staleCode = generator.generateOneTimePasswordString(key, Instant.now().minus(Duration.ofSeconds(300)));
        assertThatThrownBy(() -> authenticator.verify(staleCode)).isInstanceOf(ServiceException.class);
    }

    @Test
    void generateBindingReturnsBase32Secret() {
        TotpBinding binding = authenticator.generateBinding("user@host", "migoo");
        // Base32 编码字符集为 A-Z 和 2-7，20 字节 HMAC-SHA1 密钥编码后为 32 字符
        assertThat(binding.getTotpSecret()).isNotBlank().matches("[A-Z2-7]+");
        assertThat(binding.getTotpSecret()).hasSize(32);
    }

    @Test
    void generateBindingReturnsOtpauthUrl() {
        TotpBinding binding = authenticator.generateBinding("user@host", "migoo");
        String uri = binding.getOtpAuthUri();
        assertThat(uri).startsWith("otpauth://totp/");
        // URL 包含 label、issuer、secret 及算法参数
        assertThat(uri).contains("migoo:user@host");
        assertThat(uri).contains("secret=" + binding.getTotpSecret());
        assertThat(uri).contains("issuer=migoo");
        assertThat(uri).contains("algorithm=SHA1");
        assertThat(uri).contains("digits=6");
        assertThat(uri).contains("period=30");
    }

    @Test
    void generateBindingProducesDistinctSecrets() {
        // 每次生成的密钥随机，互不相同
        assertThat(authenticator.generateBinding("a", "i").getTotpSecret())
                .isNotEqualTo(authenticator.generateBinding("a", "i").getTotpSecret());
    }
}
