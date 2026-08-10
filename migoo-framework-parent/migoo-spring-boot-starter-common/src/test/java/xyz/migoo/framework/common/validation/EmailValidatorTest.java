package xyz.migoo.framework.common.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link EmailValidator} 单元测试
 *
 * @author migoo
 */
class EmailValidatorTest {

    private final EmailValidator validator = new EmailValidator();

    @Test
    void isValid_null_shouldReturnTrue() {
        validator.initialize(null); // initialize 为空方法，传 null 安全
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    void isValid_empty_shouldReturnTrue() {
        validator.initialize(null);
        assertThat(validator.isValid("", null)).isTrue();
    }

    @Test
    void isValid_validSimpleEmail_shouldReturnTrue() {
        validator.initialize(null);
        assertThat(validator.isValid("test@example.com", null)).isTrue();
    }

    @Test
    void isValid_validComplexEmail_shouldReturnTrue() {
        validator.initialize(null);
        assertThat(validator.isValid("a.b+c@sub.domain.co.uk", null)).isTrue();
    }

    @Test
    void isValid_missingAt_shouldReturnFalse() {
        validator.initialize(null);
        assertThat(validator.isValid("not-an-email", null)).isFalse();
    }

    @Test
    void isValid_emptyDomain_shouldReturnFalse() {
        validator.initialize(null);
        assertThat(validator.isValid("test@.com", null)).isFalse();
    }

    @Test
    void isValid_missingLocalPart_shouldReturnFalse() {
        validator.initialize(null);
        assertThat(validator.isValid("@example.com", null)).isFalse();
    }

    @Test
    void isValid_missingTld_shouldReturnFalse() {
        validator.initialize(null);
        assertThat(validator.isValid("test@example", null)).isFalse();
    }
}
