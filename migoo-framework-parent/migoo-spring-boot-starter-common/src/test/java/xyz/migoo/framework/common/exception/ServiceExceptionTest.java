package xyz.migoo.framework.common.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ServiceException} 的单元测试
 *
 * @author xiaomi
 */
class ServiceExceptionTest {

    @Test
    void noArgsConstructor_shouldCreateEmptyException() {
        // 空构造，用于反序列化场景，code 与 message 均为 null
        ServiceException exception = new ServiceException();
        assertThat(exception.getCode()).isNull();
        assertThat(exception.getMessage()).isNull();
        // 仍是 RuntimeException 的子类
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void errorCodeConstructor_shouldCopyCodeAndMessage() {
        // 从 ErrorCode 拷贝 code 与 msg
        ErrorCode errorCode = ErrorCode.of(400, "请求错误");
        ServiceException exception = new ServiceException(errorCode);
        assertThat(exception.getCode()).isEqualTo(400);
        assertThat(exception.getMessage()).isEqualTo("请求错误");
    }

    @Test
    void codeAndMessageConstructor_shouldSetFields() {
        // 直接指定 code 与 message
        ServiceException exception = new ServiceException(500, "系统错误");
        assertThat(exception.getCode()).isEqualTo(500);
        assertThat(exception.getMessage()).isEqualTo("系统错误");
    }

    @Test
    void setCode_shouldReturnThisAndUpdateCode() {
        // 流式 setter 返回当前实例
        ServiceException exception = new ServiceException();
        ServiceException result = exception.setCode(123);
        assertThat(result).isSameAs(exception);
        assertThat(exception.getCode()).isEqualTo(123);
    }

    @Test
    void setMessage_shouldReturnThisAndUpdateMessage() {
        // 流式 setter 返回当前实例
        ServiceException exception = new ServiceException();
        ServiceException result = exception.setMessage("新的提示");
        assertThat(result).isSameAs(exception);
        assertThat(exception.getMessage()).isEqualTo("新的提示");
    }

    @Test
    void getMessage_shouldReturnMessageField() {
        // getMessage() 被重写为直接返回 message 字段
        ServiceException exception = new ServiceException(100, "业务错误");
        assertThat(exception.getMessage()).isEqualTo("业务错误");
    }

    @Test
    void getCode_shouldReturnCodeField() {
        // Lombok 生成的 getter
        ServiceException exception = new ServiceException(999, "未知错误");
        assertThat(exception.getCode()).isEqualTo(999);
    }
}
