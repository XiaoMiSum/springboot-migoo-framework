package xyz.migoo.framework.common.exception;

import xyz.migoo.framework.common.exception.enums.GlobalErrorCodeConstants;
import xyz.migoo.framework.common.exception.enums.ServiceErrorCodeRange;

/**
 * 错误码对象
 * <p>
 * 全局错误码，占用 [0, 999], 参见 {@link GlobalErrorCodeConstants}
 * 业务异常错误码，占用 [1 000 000 000, +∞)，参见 {@link ServiceErrorCodeRange}
 * <p>
 *
 * @param code 错误码
 * @param msg  错误提示
 */
public record ErrorCode(Integer code, String msg) {

    public static ErrorCode of(Integer code, String message) {
        return new ErrorCode(code, message);
    }
}
