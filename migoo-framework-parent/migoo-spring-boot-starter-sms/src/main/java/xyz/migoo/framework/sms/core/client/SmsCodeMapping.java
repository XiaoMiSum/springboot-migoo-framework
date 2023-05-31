package xyz.migoo.framework.sms.core.client;

import xyz.migoo.framework.common.exception.ErrorCode;
import xyz.migoo.framework.sms.core.enums.SmsFrameworkErrorCodeConstants;

import java.util.function.Function;

/**
 * 将 API 的错误码，转换为通用的错误码
 *
 * @author xiaomi
 * @see SmsResult
 * @see SmsFrameworkErrorCodeConstants
 */
public interface SmsCodeMapping extends Function<String, ErrorCode> {
}
