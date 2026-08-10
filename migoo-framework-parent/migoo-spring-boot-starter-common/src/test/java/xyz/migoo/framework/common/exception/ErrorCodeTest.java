package xyz.migoo.framework.common.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ErrorCode} 的单元测试
 *
 * @author xiaomi
 */
class ErrorCodeTest {

    @Test
    void of_shouldCreateErrorCodeWithCodeAndMsg() {
        // 通过工厂方法创建错误码
        ErrorCode errorCode = ErrorCode.of(100, "测试错误");
        // 校验 code 与 msg
        assertThat(errorCode.code()).isEqualTo(100);
        assertThat(errorCode.msg()).isEqualTo("测试错误");
    }

    @Test
    void code_shouldReturnCode() {
        // 直接构造并校验 code 访问器
        ErrorCode errorCode = new ErrorCode(500, "系统错误");
        assertThat(errorCode.code()).isEqualTo(500);
    }

    @Test
    void msg_shouldReturnMsg() {
        // 直接构造并校验 msg 访问器
        ErrorCode errorCode = new ErrorCode(500, "系统错误");
        assertThat(errorCode.msg()).isEqualTo("系统错误");
    }

    @Test
    void equals_shouldBeEqualWhenComponentsAreSame() {
        // 组件相同的两个 record 实例应相等
        ErrorCode left = ErrorCode.of(200, "common.success");
        ErrorCode right = ErrorCode.of(200, "common.success");
        assertThat(left).isEqualTo(right);
    }

    @Test
    void equals_shouldNotBeEqualWhenComponentsAreDifferent() {
        // 组件不同则不应相等
        ErrorCode left = ErrorCode.of(200, "common.success");
        ErrorCode right = ErrorCode.of(201, "common.success");
        assertThat(left).isNotEqualTo(right);
    }

    @Test
    void hashCode_shouldBeSameForEqualErrorCodes() {
        // 相等对象的 hashCode 必须一致
        ErrorCode left = ErrorCode.of(404, "not found");
        ErrorCode right = ErrorCode.of(404, "not found");
        assertThat(left.hashCode()).isEqualTo(right.hashCode());
    }

    @Test
    void toString_shouldContainCodeAndMsg() {
        // record 自动生成的 toString 包含各组件
        ErrorCode errorCode = ErrorCode.of(100, "hello");
        assertThat(errorCode.toString()).isEqualTo("ErrorCode[code=100, msg=hello]");
    }

    @Test
    void of_shouldSupportNullComponents() {
        // record 允许 null 组件
        ErrorCode errorCode = ErrorCode.of(null, null);
        assertThat(errorCode.code()).isNull();
        assertThat(errorCode.msg()).isNull();
        // 两个组件都为 null 的实例相等
        assertThat(errorCode).isEqualTo(new ErrorCode(null, null));
    }
}
