package xyz.migoo.framework.common.sensitive;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SensitiveDataUtil} 单元测试
 */
class SensitiveDataUtilTest {

    @Test
    void masksMobileKeepingThreeAndFour() {
        assertThat(SensitiveDataUtil.mask("13812345678", SensitiveType.MOBILE))
                .isEqualTo("138****5678");
    }

    @Test
    void masksIdCardKeepingFourAndFour() {
        assertThat(SensitiveDataUtil.mask("330106199001011234", SensitiveType.ID_CARD))
                .isEqualTo("3301**********1234");
    }

    @Test
    void masksBankCardKeepingFourAndFour() {
        assertThat(SensitiveDataUtil.mask("6222021234567890", SensitiveType.BANK_CARD))
                .isEqualTo("6222********7890");
    }

    @Test
    void masksEmailKeepingFirstLocalCharAndDomain() {
        assertThat(SensitiveDataUtil.mask("alice@example.com", SensitiveType.EMAIL))
                .isEqualTo("a***@example.com");
        // 本地部分单字符
        assertThat(SensitiveDataUtil.mask("a@example.com", SensitiveType.EMAIL))
                .isEqualTo("a***@example.com");
    }

    @Test
    void masksEmailWithoutAtSignEntirely() {
        assertThat(SensitiveDataUtil.mask("not-an-email", SensitiveType.EMAIL))
                .isEqualTo("************");
    }

    @Test
    void masksPasswordWithFixedLengthStars() {
        // 定长掩码，不泄露原长度
        assertThat(SensitiveDataUtil.mask("secret", SensitiveType.PASSWORD)).isEqualTo("******");
        assertThat(SensitiveDataUtil.mask("1234567890", SensitiveType.PASSWORD)).isEqualTo("******");
    }

    @Test
    void shortValueMaskedEntirely() {
        // 7 位（不足 3 + 4）→ 全部掩码
        assertThat(SensitiveDataUtil.mask("1234567", SensitiveType.MOBILE)).isEqualTo("*******");
    }

    @Test
    void nullValueAndNullTypePassThrough() {
        assertThat(SensitiveDataUtil.mask(null, SensitiveType.MOBILE)).isNull();
        assertThat(SensitiveDataUtil.mask("", SensitiveType.MOBILE)).isEmpty();
        assertThat(SensitiveDataUtil.mask("13812345678", null)).isEqualTo("13812345678");
    }
}
