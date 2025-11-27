package xyz.migoo.framework.common.exception.enums;

import xyz.migoo.framework.common.exception.ErrorCode;

/**
 * 全局错误码枚举
 * 0-999 系统异常编码保留
 * <p>
 * 一般情况下，使用 HTTP 响应状态码 https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Status
 * 虽然说，HTTP 响应状态码作为业务使用表达能力偏弱，但是使用在系统层面还是非常不错的
 * 比较特殊的是，因为之前一直使用 0 作为成功，就不使用 200 啦。
 *
 * @author xiaomi
 */
public class GlobalErrorCodeConstants {

    public static final ErrorCode SUCCESS = ErrorCode.of(0, "common.success");

    // ========== 客户端错误段 ==========

    public static final ErrorCode BAD_REQUEST = ErrorCode.of(400, "common.request.bad");
    public static final ErrorCode UNAUTHORIZED = ErrorCode.of(401, "common.authentication.failure");
    public static final ErrorCode FORBIDDEN = ErrorCode.of(403, "common.permission.miss");
    public static final ErrorCode NOT_FOUND = ErrorCode.of(404, "common.not.found");
    public static final ErrorCode METHOD_NOT_ALLOWED = ErrorCode.of(405, "common.request.method.bad");
    public static final ErrorCode LOCKED = ErrorCode.of(423, "common.frequent.request");// 并发请求，不允许
    public static final ErrorCode TOO_MANY_REQUESTS = ErrorCode.of(429, "common.too.many.requests");
    public static final ErrorCode SOCKET_TIME_OUT = ErrorCode.of(450, "common.request.timeout");

    // ========== 服务端错误段 ==========

    public static final ErrorCode INTERNAL_SERVER_ERROR = ErrorCode.of(500, "common.system.error");

    // ========== 自定义错误段 ==========
    public static final ErrorCode REPEATED_REQUESTS = ErrorCode.of(900, "common.repeat.request");// 重复请求

    public static final ErrorCode UNKNOWN = ErrorCode.of(999, "common.unknown.error");

    public static boolean isMatch(Integer code) {
        return code != null
                && code >= SUCCESS.code() && code <= UNKNOWN.code();
    }

}
