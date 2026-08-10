package xyz.migoo.framework.common.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link GlobalErrorCodeConstants} 的单元测试
 *
 * @author xiaomi
 */
class GlobalErrorCodeConstantsTest {

    @Test
    void success_shouldBe200WithCommonSuccessMsg() {
        // 成功码固定为 200
        assertThat(GlobalErrorCodeConstants.SUCCESS.code()).isEqualTo(200);
        assertThat(GlobalErrorCodeConstants.SUCCESS.msg()).isEqualTo("common.success");
    }

    @Test
    void clientErrorCodes_shouldHaveExpectedValues() {
        // 客户端错误段错误码
        assertThat(GlobalErrorCodeConstants.BAD_REQUEST.code()).isEqualTo(400);
        assertThat(GlobalErrorCodeConstants.UNAUTHORIZED.code()).isEqualTo(401);
        assertThat(GlobalErrorCodeConstants.INVALID_AUTHORIZED.code()).isEqualTo(401);
        assertThat(GlobalErrorCodeConstants.FORBIDDEN.code()).isEqualTo(403);
        assertThat(GlobalErrorCodeConstants.NOT_FOUND.code()).isEqualTo(404);
        assertThat(GlobalErrorCodeConstants.METHOD_NOT_ALLOWED.code()).isEqualTo(405);
        assertThat(GlobalErrorCodeConstants.LOCKED.code()).isEqualTo(423);
        assertThat(GlobalErrorCodeConstants.TOO_MANY_REQUESTS.code()).isEqualTo(429);
        assertThat(GlobalErrorCodeConstants.SOCKET_TIME_OUT.code()).isEqualTo(450);
    }

    @Test
    void serverAndCustomErrorCodes_shouldHaveExpectedValues() {
        // 服务端错误段 + 自定义错误段错误码
        assertThat(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.code()).isEqualTo(500);
        assertThat(GlobalErrorCodeConstants.REPEATED_REQUESTS.code()).isEqualTo(900);
        assertThat(GlobalErrorCodeConstants.UNKNOWN.code()).isEqualTo(999);
    }

    @Test
    void isMatch_shouldMatchCodesWithin200To999() {
        // 边界及区间外错误码的匹配结果
        assertThat(GlobalErrorCodeConstants.isMatch(null)).isFalse();
        assertThat(GlobalErrorCodeConstants.isMatch(199)).isFalse();
        assertThat(GlobalErrorCodeConstants.isMatch(200)).isTrue();
        assertThat(GlobalErrorCodeConstants.isMatch(500)).isTrue();
        assertThat(GlobalErrorCodeConstants.isMatch(999)).isTrue();
        assertThat(GlobalErrorCodeConstants.isMatch(1000)).isFalse();
    }
}
