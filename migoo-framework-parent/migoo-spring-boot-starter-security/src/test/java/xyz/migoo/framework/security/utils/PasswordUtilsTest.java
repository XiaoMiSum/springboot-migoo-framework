package xyz.migoo.framework.security.utils;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link PasswordUtils} 单元测试
 */
class PasswordUtilsTest {

    /** 使用真实的 BCrypt 加密器（与 MiGooSecurityAutoConfiguration 默认一致） */
    private final PasswordUtils passwordUtils = new PasswordUtils(new BCryptPasswordEncoder());

    @Test
    void encodeReturnsHashNotPlainText() {
        String hashed = PasswordUtils.encode("password123");
        // BCrypt 哈希不以明文形式出现，且以 $2a/$2b/$2y 前缀开头
        assertThat(hashed).isNotEqualTo("password123").startsWith("$2");
    }

    @Test
    void encodeWithSamePasswordProducesDifferentHashes() {
        // BCrypt 使用随机盐，两次 encode 结果必然不同
        assertThat(PasswordUtils.encode("password123"))
                .isNotEqualTo(PasswordUtils.encode("password123"));
    }

    @Test
    void verifyRoundtripReturnsTrue() {
        String hashed = PasswordUtils.encode("password123");
        assertThat(PasswordUtils.verify("password123", hashed)).isTrue();
    }

    @Test
    void verifyWrongPasswordReturnsFalse() {
        String hashed = PasswordUtils.encode("password123");
        assertThat(PasswordUtils.verify("wrong-password", hashed)).isFalse();
    }

    @Test
    void verifyNullPasswordReturnsFalse() {
        String hashed = PasswordUtils.encode("password123");
        // BCrypt matches(null, hash) 返回 false 而非抛异常
        assertThat(PasswordUtils.verify(null, hashed)).isFalse();
    }

    @Test
    void verifyNullHashReturnsFalse() {
        // matches 对 null 哈希返回 false
        assertThat(PasswordUtils.verify("password123", null)).isFalse();
    }

    @Test
    void verifyNullBothReturnsFalse() {
        assertThat(PasswordUtils.verify(null, null)).isFalse();
    }

    @Test
    void encodeNullReturnsNull() {
        // Spring Security 7.x AbstractValidatingPasswordEncoder 对 null 明文直接返回 null，不抛异常
        assertThat(PasswordUtils.encode(null)).isNull();
    }
}
