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

    public static final ErrorCode SUCCESS = ErrorCode.of(0, "操作成功");

    // ========== 客户端错误段 ==========

    public static final ErrorCode BAD_REQUEST = ErrorCode.of(400, "请求参数不正确");
    public static final ErrorCode UNAUTHORIZED = ErrorCode.of(401, "认证失败");
    public static final ErrorCode FORBIDDEN = ErrorCode.of(403, "没有该操作权限");
    public static final ErrorCode NOT_FOUND = ErrorCode.of(404, "找不到请求地址");
    public static final ErrorCode METHOD_NOT_ALLOWED = ErrorCode.of(405, "请求方法错误");
    public static final ErrorCode LOCKED = ErrorCode.of(423, "请求失败，请稍后重试");// 并发请求，不允许
    public static final ErrorCode TOO_MANY_REQUESTS = ErrorCode.of(429, "请求过于频繁，请稍后重试");
    public static final ErrorCode SOCKET_TIME_OUT = ErrorCode.of(450, "请求超时，请稍后重试");

    // ========== 服务端错误段 ==========

    public static final ErrorCode INTERNAL_SERVER_ERROR = ErrorCode.of(500, "系统内部异常");

    // ========== 自定义错误段 ==========
    public static final ErrorCode REPEATED_REQUESTS = ErrorCode.of(900, "重复请求，请稍后重试");// 重复请求

    public static final ErrorCode DEMO_DENY = ErrorCode.of(900, "演示模式，禁止写操作");

    public static final ErrorCode UNKNOWN = ErrorCode.of(999, "未知错误");

    public static boolean isMatch(Integer code) {
        return code != null
                && code >= SUCCESS.getCode() && code <= UNKNOWN.getCode();
    }

}
