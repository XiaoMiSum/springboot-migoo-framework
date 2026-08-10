package xyz.migoo.framework.common.validation;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link PasswordValidator} 单元测试
 *
 * <p>通过反射从测试 Bean 字段上获取真实的 {@link Password} 注解实例来调用 initialize()，
 * 避免引入 Mockito。</p>
 *
 * @author migoo
 */
class PasswordValidatorTest {

    /** 使用默认参数：min = 8, max = 32, specialChars = "_@#$!" */
    static class DefaultBean {
        @Password
        private String password;
    }

    /** 自定义长度：min = 6, max = 12 */
    static class CustomBean {
        @Password(min = 6, max = 12)
        private String password;
    }

    private static Password defaultPassword;
    private static Password customPassword;

    @BeforeAll
    static void setUp() throws Exception {
        defaultPassword = DefaultBean.class.getDeclaredField("password").getAnnotation(Password.class);
        customPassword = CustomBean.class.getDeclaredField("password").getAnnotation(Password.class);
    }

    private PasswordValidator newDefaultValidator() {
        PasswordValidator validator = new PasswordValidator();
        validator.initialize(defaultPassword);
        return validator;
    }

    private PasswordValidator newCustomValidator() {
        PasswordValidator validator = new PasswordValidator();
        validator.initialize(customPassword);
        return validator;
    }

    @Test
    void isValid_validPassword_shouldReturnTrue() {
        // 包含字母、数字、特殊字符（_@#$! 之一），长度 8
        assertThat(newDefaultValidator().isValid("Abc123!x", null)).isTrue();
    }

    @Test
    void isValid_tooShort_shouldReturnFalse() {
        // 7 位，小于默认 min = 8
        assertThat(newDefaultValidator().isValid("abc123!", null)).isFalse();
    }

    @Test
    void isValid_missingDigit_shouldReturnFalse() {
        assertThat(newDefaultValidator().isValid("Abcdefgh", null)).isFalse();
    }

    @Test
    void isValid_missingLetter_shouldReturnFalse() {
        assertThat(newDefaultValidator().isValid("12345678!", null)).isFalse();
    }

    @Test
    void isValid_missingSpecialChar_shouldReturnFalse() {
        assertThat(newDefaultValidator().isValid("Abc12345", null)).isFalse();
    }

    @Test
    void isValid_null_shouldReturnFalse() {
        assertThat(newDefaultValidator().isValid(null, null)).isFalse();
    }

    @Test
    void isValid_empty_shouldReturnFalse() {
        assertThat(newDefaultValidator().isValid("", null)).isFalse();
    }

    @Test
    void isValid_customMinMax_shouldReturnTrue() {
        // min = 6 时恰好 6 位（含字母、数字、特殊字符）应通过
        assertThat(newCustomValidator().isValid("Ab1!ef", null)).isTrue();
    }
}
