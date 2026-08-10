package xyz.migoo.framework.common.validation;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link MobileValidator} 单元测试
 *
 * <p>通过反射从测试 Bean 字段上获取真实的 {@link Mobile} 注解实例来调用 initialize()，
 * 避免引入 Mockito。</p>
 *
 * @author migoo
 */
class MobileValidatorTest {

    /** 使用默认 region = "CN" */
    static class CnBean {
        @Mobile
        private String phone;
    }

    /** 使用 region = "US" */
    static class UsBean {
        @Mobile(region = "US")
        private String phone;
    }

    private static Mobile cnMobile;
    private static Mobile usMobile;

    @BeforeAll
    static void setUp() throws Exception {
        cnMobile = CnBean.class.getDeclaredField("phone").getAnnotation(Mobile.class);
        usMobile = UsBean.class.getDeclaredField("phone").getAnnotation(Mobile.class);
    }

    private MobileValidator newCnValidator() {
        MobileValidator validator = new MobileValidator();
        validator.initialize(cnMobile);
        return validator;
    }

    private MobileValidator newUsValidator() {
        MobileValidator validator = new MobileValidator();
        validator.initialize(usMobile);
        return validator;
    }

    // ========== CN 默认区域 ==========

    @Test
    void isValid_null_shouldReturnTrue() {
        assertThat(newCnValidator().isValid(null, null)).isTrue();
    }

    @Test
    void isValid_empty_shouldReturnTrue() {
        assertThat(newCnValidator().isValid("", null)).isTrue();
    }

    @Test
    void isValid_cnValidMobile_shouldReturnTrue() {
        assertThat(newCnValidator().isValid("13812345678", null)).isTrue();
    }

    @Test
    void isValid_cnWrongPrefix_shouldReturnFalse() {
        assertThat(newCnValidator().isValid("23812345678", null)).isFalse();
    }

    @Test
    void isValid_cnTooShort_shouldReturnFalse() {
        // 10 位，长度不等于 11
        assertThat(newCnValidator().isValid("1381234567", null)).isFalse();
    }

    // ========== US 区域（非 CN 走国际号码规则 ^\+\d{7,15}$） ==========

    @Test
    void isValid_usIntlMobileWithPlus_shouldReturnTrue() {
        assertThat(newUsValidator().isValid("+8613812345678", null)).isTrue();
    }

    @Test
    void isValid_usMobileWithoutPlus_shouldReturnFalse() {
        assertThat(newUsValidator().isValid("13812345678", null)).isFalse();
    }
}
