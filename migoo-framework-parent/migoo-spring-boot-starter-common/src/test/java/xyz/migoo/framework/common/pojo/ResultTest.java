package xyz.migoo.framework.common.pojo;

import org.junit.jupiter.api.Test;
import xyz.migoo.framework.common.exception.ErrorCode;
import xyz.migoo.framework.common.exception.GlobalErrorCodeConstants;
import xyz.migoo.framework.common.exception.ServiceException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link Result} 的单元测试
 *
 * @author xiaomi
 */
class ResultTest {

    @Test
    void ok_withData_shouldReturnSuccessResult() {
        // ok(data) 应返回 code=200、msg=common.success、data 原样保留
        Result<String> result = Result.ok("数据");
        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMsg()).isEqualTo("common.success");
        assertThat(result.getData()).isEqualTo("数据");
    }

    @Test
    void ok_withoutData_shouldReturnNullData() {
        // ok() 的 data 为 null
        Result<String> result = Result.ok();
        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMsg()).isEqualTo("common.success");
        assertThat(result.getData()).isNull();
    }

    @Test
    void error_withCodeAndMessage_shouldSetCodeAndMessage() {
        // error(code, msg) 应设置 code 与 msg，data 为 null
        Result<String> result = Result.error(500, "系统错误");
        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMsg()).isEqualTo("系统错误");
        assertThat(result.getData()).isNull();
    }

    @Test
    void error_withErrorCode_shouldUseCodeAndMsg() {
        // error(ErrorCode) 使用错误码对象的 code 与 msg
        Result<String> result = Result.error(GlobalErrorCodeConstants.BAD_REQUEST);
        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMsg()).isEqualTo("common.request.bad");
    }

    @Test
    void error_withServiceException_shouldTransferCodeAndMessage() {
        // error(ServiceException) 使用异常的 code 与 message
        ServiceException exception = new ServiceException(ErrorCode.of(403, "common.permission.miss"));
        Result<String> result = Result.error(exception);
        assertThat(result.getCode()).isEqualTo(403);
        assertThat(result.getMsg()).isEqualTo("common.permission.miss");
    }

    @Test
    void error_withResult_shouldTransferCodeAndMessage() {
        // error(Result) 将源结果的 code 与 msg 转移到新结果
        Result<Integer> source = Result.error(450, "请求超时");
        Result<String> result = Result.error(source);
        assertThat(result.getCode()).isEqualTo(450);
        assertThat(result.getMsg()).isEqualTo("请求超时");
    }

    @Test
    void error_withSuccessCode_shouldThrowIllegalArgumentException() {
        // Preconditions.checkArgument 禁止用成功码 200 构造错误结果
        assertThatThrownBy(() -> Result.error(200, "错误"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("code 必须是错误的！");
    }
}
