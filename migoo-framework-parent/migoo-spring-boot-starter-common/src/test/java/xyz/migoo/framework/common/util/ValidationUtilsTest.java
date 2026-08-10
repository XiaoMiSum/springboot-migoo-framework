package xyz.migoo.framework.common.util;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ValidationUtils 单元测试
 * <p>
 * 说明：classpath 上没有 EL 实现，hibernate-validator 会打印 HV000183 警告，
 * 但 @NotNull/@Min 等约束校验仍然正常工作，测试中不断言消息内容。
 */
class ValidationUtilsTest {

    @Test
    void isMobile() {
        assertThat(ValidationUtils.isMobile("13812345678")).isTrue();
        // 必须以 1 开头
        assertThat(ValidationUtils.isMobile("23812345678")).isFalse();
        // 10 位
        assertThat(ValidationUtils.isMobile("1381234567")).isFalse();
        // 12 位
        assertThat(ValidationUtils.isMobile("138123456789")).isFalse();
        assertThat(ValidationUtils.isMobile(null)).isFalse();
        assertThat(ValidationUtils.isMobile("")).isFalse();
        assertThat(ValidationUtils.isMobile("   ")).isFalse();
    }

    @Test
    void isMobile_withRegion() {
        // CN 使用国内号码规则
        assertThat(ValidationUtils.isMobile("13812345678", "CN")).isTrue();
        assertThat(ValidationUtils.isMobile("+8613812345678", "CN")).isFalse();
        // 非 CN 使用国际号码规则：+ 后 7-15 位数字
        assertThat(ValidationUtils.isMobile("+8613812345678", "US")).isTrue();
        assertThat(ValidationUtils.isMobile("13812345678", "US")).isFalse();
        assertThat(ValidationUtils.isMobile("+123", "US")).isFalse();
        assertThat(ValidationUtils.isMobile(null, "US")).isFalse();
    }

    @Test
    void isEmail() {
        assertThat(ValidationUtils.isEmail("migoo@example.com")).isTrue();
        assertThat(ValidationUtils.isEmail("a.b+c-d@example.co.uk")).isTrue();
        assertThat(ValidationUtils.isEmail("abc")).isFalse();
        assertThat(ValidationUtils.isEmail("abc@def")).isFalse();
        assertThat(ValidationUtils.isEmail("@example.com")).isFalse();
        assertThat(ValidationUtils.isEmail("abc@.com")).isFalse();
    }

    @Test
    void isUrl() {
        assertThat(ValidationUtils.isUrl("https://example.com")).isTrue();
        assertThat(ValidationUtils.isUrl("http://example.com/path?query=1&x=2")).isTrue();
        assertThat(ValidationUtils.isUrl("ftp://example.com/file.txt")).isTrue();
        assertThat(ValidationUtils.isUrl("file:///tmp/test.txt")).isTrue();
        assertThat(ValidationUtils.isUrl("not a url")).isFalse();
        assertThat(ValidationUtils.isUrl(null)).isFalse();
        assertThat(ValidationUtils.isUrl("")).isFalse();
    }

    @Test
    void validate_validBeanDoesNotThrow() {
        assertThatCode(() -> ValidationUtils.validate(new TestBean("migoo", 18)))
                .doesNotThrowAnyException();
    }

    @Test
    void validate_invalidBeanThrowsConstraintViolationException() {
        TestBean bean = new TestBean();
        bean.setAge(0);
        assertThatThrownBy(() -> ValidationUtils.validate(bean))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void validate_withValidatorAndGroups() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        GroupedBean bean = new GroupedBean();
        bean.setName("migoo");
        // code 上的 @NotNull 属于 UpdateGroup，默认分组不校验 -> 不抛异常
        assertThatCode(() -> ValidationUtils.validate(validator, bean)).doesNotThrowAnyException();
        // 指定 UpdateGroup 分组时触发校验 -> 抛异常
        assertThatThrownBy(() -> ValidationUtils.validate(validator, bean, UpdateGroup.class))
                .isInstanceOf(ConstraintViolationException.class);
    }

    /** 校验测试用 Bean */
    public static class TestBean {

        @NotNull
        private String name;

        @Min(1)
        private int age;

        public TestBean() {
        }

        public TestBean(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

    /** 分组校验用分组接口 */
    public interface UpdateGroup {
    }

    /** 分组校验测试用 Bean */
    public static class GroupedBean {

        @NotNull(groups = UpdateGroup.class)
        private String code;

        @NotNull
        private String name;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
