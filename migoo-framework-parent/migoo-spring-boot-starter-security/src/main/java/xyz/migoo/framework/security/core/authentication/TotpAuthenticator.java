package xyz.migoo.framework.security.core.authentication;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.codec.binary.Base32;
import org.jspecify.annotations.Nullable;
import org.springframework.util.StringUtils;
import xyz.migoo.framework.common.exception.ErrorCode;
import xyz.migoo.framework.common.exception.ServiceExceptionUtil;
import xyz.migoo.framework.security.core.util.SecurityFrameworkUtils;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * TOTP 二次验证器
 *
 * @author xiaomi
 */
public class TotpAuthenticator {

    private static final ErrorCode ERROR_2FA_FAILURE = new ErrorCode(999, "2fa.failure");

    private static final TimeBasedOneTimePasswordGenerator TOTP_GENERATOR;
    private static final KeyGenerator KEY_GENERATOR;
    private static final Base32 BASE32 = new Base32();

    static {
        try {
            TOTP_GENERATOR = new TimeBasedOneTimePasswordGenerator();
            // 密钥长度应匹配 HMAC 输出长度 (SHA-1: 160 bits, SHA-256: 256 bits, SHA-512: 512 bits)
            int macLengthInBits = Mac.getInstance(TOTP_GENERATOR.getAlgorithm()).getMacLength() * 8;
            KEY_GENERATOR = KeyGenerator.getInstance(TOTP_GENERATOR.getAlgorithm());
            KEY_GENERATOR.init(macLengthInBits);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("初始化 TOTP 失败", e);
        }
    }

    /**
     * 校验 TOTP 验证码
     *
     * @param code 用户输入的 TOTP 验证码
     */
    public void verify(@Nullable String code) {
        if (!StringUtils.hasText(code)) {
            throw ServiceExceptionUtil.get(ERROR_2FA_FAILURE);
        }
        var authUserDetails = SecurityFrameworkUtils.getLoginUser();
        if (Objects.isNull(authUserDetails)) {
            throw ServiceExceptionUtil.get(ERROR_2FA_FAILURE);
        }
        if (!authUserDetails.isTwoFactorEnabled()) {
            return;
        }
        try {
            SecretKey key = restoreKey(authUserDetails.getTotpSecret());
            Instant now = Instant.now();
            Duration timeStep = TOTP_GENERATOR.getTimeStep();
            // 验证当前和上一个时间窗口（允许 1 个时间步的时钟偏差）
            String currentCode = TOTP_GENERATOR.generateOneTimePasswordString(key, now);
            String previousCode = TOTP_GENERATOR.generateOneTimePasswordString(key, now.minus(timeStep));
            if (!currentCode.equals(code) && !previousCode.equals(code)) {
                throw ServiceExceptionUtil.get(ERROR_2FA_FAILURE);
            }
        } catch (InvalidKeyException e) {
            throw ServiceExceptionUtil.get(ERROR_2FA_FAILURE);
        }
    }

    /**
     * 生成 TOTP 绑定信息（密钥 + otpauth URL）
     *
     * @param label  用户标识 (如: user@host)
     * @param issuer 应用名称
     * @return 密钥和 otpauth URL
     */
    public TotpBinding generateBinding(String label, String issuer) {
        SecretKey key = KEY_GENERATOR.generateKey();
        // Base32 编码存储（符合 TOTP 标准，兼容 Google Authenticator 等）
        String secret = BASE32.encodeToString(key.getEncoded());
        // otpauth URI 使用简化的算法名称 (SHA1, SHA256, SHA512)
        String algorithm = TOTP_GENERATOR.getAlgorithm().replace("Hmac", "");
        String otpAuthUri = "otpauth://totp/" + issuer + ":" + label
                + "?secret=" + secret
                + "&issuer=" + issuer
                + "&algorithm=" + algorithm
                + "&digits=" + TOTP_GENERATOR.getPasswordLength()
                + "&period=" + TOTP_GENERATOR.getTimeStep().getSeconds();
        return new TotpBinding(secret, otpAuthUri);
    }

    /**
     * 从 Base32 编码的密钥字符串还原 SecretKey
     */
    private SecretKey restoreKey(String base32Secret) {
        byte[] decodedKey = BASE32.decode(base32Secret);
        return new SecretKeySpec(decodedKey, TOTP_GENERATOR.getAlgorithm());
    }

    /**
     * TOTP 绑定信息（密钥 + otpauth URL）
     */
    @Data
    @AllArgsConstructor
    @EqualsAndHashCode()
    public static class TotpBinding {

        /**
         * TOTP 密钥 (Base32 编码)
         */
        private String totpSecret;

        /**
         * otpauth:// URL，用于生成 QR 码
         */
        private String otpAuthUri;
    }

}
