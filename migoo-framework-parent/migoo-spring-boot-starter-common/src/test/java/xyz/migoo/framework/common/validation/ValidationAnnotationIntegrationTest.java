package xyz.migoo.framework.common.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import xyz.migoo.framework.common.core.IntArrayValuable;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 校验注解端到端集成测试（使用 hibernate-validator 作为实现）
 *
 * <p>注意：classpath 上没有 EL 实现，hibernate-validator 会打印 HV000183 警告，
 * 消息插值保留原始 {validation.xxx} 模板。因此本测试<b>绝不</b>断言消息文本，
 * 只断言违反约束的数量与属性路径（getPropertyPath()）。</p>
 *
 * @author migoo
 */
class ValidationAnnotationIntegrationTest {

    static enum Status implements IntArrayValuable {
        ENABLED(1), DISABLED(2);

        private final int value;

        Status(int value) {
            this.value = value;
        }

        @Override
        public int[] array() {
            return new int[]{1, 2};
        }
    }

    static class ValidBean {
        @Email
        private String email;

        @Mobile
        private String mobile;

        @Password
        private String password;

        @InEnum(value = Status.class)
        private Integer status;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        if (factory != null) {
            factory.close();
        }
    }

    private static ValidBean validBean() {
        ValidBean bean = new ValidBean();
        bean.setEmail("test@example.com");
        bean.setMobile("13812345678");
        bean.setPassword("Abc123!x");
        bean.setStatus(1);
        return bean;
    }

    @Test
    void validate_allFieldsValid_shouldHaveNoViolations() {
        Set<ConstraintViolation<ValidBean>> violations = validator.validate(validBean());
        assertThat(violations).isEmpty();
    }

    @Test
    void validate_invalidEmail_shouldHaveOneViolation() {
        ValidBean bean = validBean();
        bean.setEmail("not-an-email");
        Set<ConstraintViolation<ValidBean>> violations = validator.validate(bean);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactly("email");
    }

    @Test
    void validate_invalidMobile_shouldHaveOneViolation() {
        ValidBean bean = validBean();
        bean.setMobile("23812345678");
        Set<ConstraintViolation<ValidBean>> violations = validator.validate(bean);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactly("mobile");
    }

    @Test
    void validate_invalidPassword_shouldHaveOneViolation() {
        ValidBean bean = validBean();
        bean.setPassword("Abcdefgh"); // 缺少数字
        Set<ConstraintViolation<ValidBean>> violations = validator.validate(bean);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactly("password");
    }

    @Test
    void validate_invalidStatus_shouldHaveOneViolation() {
        ValidBean bean = validBean();
        bean.setStatus(3); // 不在枚举值 [1, 2] 中
        Set<ConstraintViolation<ValidBean>> violations = validator.validate(bean);
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactly("status");
    }

    @Test
    void validate_allFieldsInvalid_shouldHaveFourViolations() {
        ValidBean bean = new ValidBean();
        bean.setEmail("not-an-email");
        bean.setMobile("23812345678");
        bean.setPassword("Abcdefgh");
        bean.setStatus(3);
        Set<ConstraintViolation<ValidBean>> violations = validator.validate(bean);
        assertThat(violations).hasSize(4);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("email", "mobile", "password", "status");
    }
}
