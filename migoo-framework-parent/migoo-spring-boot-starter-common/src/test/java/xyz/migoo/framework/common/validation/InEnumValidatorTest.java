package xyz.migoo.framework.common.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import xyz.migoo.framework.common.core.IntArrayValuable;

import java.lang.reflect.Proxy;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link InEnumValidator} 单元测试
 *
 * <p>测试枚举 {@link Status} 实现 {@link IntArrayValuable}；
 * 通过反射从测试 Bean 字段上获取真实的 {@link InEnum} 注解实例调用 initialize()。</p>
 *
 * <p>由于项目没有 Mockito，isValid 在非法值分支会调用
 * {@link ConstraintValidatorContext#buildConstraintViolationWithTemplate(String)}，
 * 这里用 JDK 动态代理伪造一个 Context 供链式调用，仅断言 isValid 返回值。</p>
 *
 * @author migoo
 */
class InEnumValidatorTest {

    /** 实现 {@link IntArrayValuable} 的测试枚举 */
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

    static class Bean {
        @InEnum(value = Status.class)
        private Integer status;
    }

    private static InEnum inEnum;

    @BeforeAll
    static void setUp() throws Exception {
        inEnum = Bean.class.getDeclaredField("status").getAnnotation(InEnum.class);
    }

    private InEnumValidator newValidator() {
        InEnumValidator validator = new InEnumValidator();
        validator.initialize(inEnum);
        return validator;
    }

    @Test
    void isValid_null_shouldReturnTrue() {
        assertThat(newValidator().isValid(null, null)).isTrue();
    }

    @Test
    void isValid_enumValue1_shouldReturnTrue() {
        assertThat(newValidator().isValid(1, mockContext())).isTrue();
    }

    @Test
    void isValid_enumValue2_shouldReturnTrue() {
        assertThat(newValidator().isValid(2, mockContext())).isTrue();
    }

    @Test
    void isValid_unknownValue_shouldReturnFalse() {
        assertThat(newValidator().isValid(3, mockContext())).isFalse();
    }

    @Test
    void isValid_stringValue_shouldReturnFalse() {
        // 集合内是 Integer，contains 使用 equals，"1" 与 Integer 1 不相等
        assertThat(newValidator().isValid("1", mockContext())).isFalse();
    }

    // ========== JDK 动态代理伪造 ConstraintValidatorContext ==========

    private ConstraintValidatorContext mockContext() {
        return (ConstraintValidatorContext) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{ConstraintValidatorContext.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getDefaultConstraintMessageTemplate":
                            return "{validation.values.range.invalid}";
                        case "buildConstraintViolationWithTemplate":
                            return mockViolationBuilder();
                        default:
                            return defaultValue(method.getReturnType());
                    }
                });
    }

    private ConstraintValidatorContext.ConstraintViolationBuilder mockViolationBuilder() {
        return (ConstraintValidatorContext.ConstraintViolationBuilder) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{ConstraintValidatorContext.ConstraintViolationBuilder.class},
                (proxy, method, args) -> {
                    if ("addConstraintViolation".equals(method.getName())) {
                        return mockContext();
                    }
                    return defaultValue(method.getReturnType());
                });
    }

    private Object defaultValue(Class<?> returnType) {
        if (returnType == void.class || returnType == Void.class) {
            return null;
        }
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == byte.class || returnType == short.class || returnType == int.class || returnType == char.class) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == float.class) {
            return 0F;
        }
        if (returnType == double.class) {
            return 0D;
        }
        return null;
    }
}
