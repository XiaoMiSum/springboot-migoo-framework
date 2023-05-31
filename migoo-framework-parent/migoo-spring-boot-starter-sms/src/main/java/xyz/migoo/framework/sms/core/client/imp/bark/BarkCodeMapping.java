package xyz.migoo.framework.sms.core.client.imp.bark;

import xyz.migoo.framework.common.exception.ErrorCode;
import xyz.migoo.framework.common.exception.enums.GlobalErrorCodeConstants;
import xyz.migoo.framework.sms.core.client.SmsCodeMapping;
import xyz.migoo.framework.sms.core.enums.SmsFrameworkErrorCodeConstants;

public class BarkCodeMapping implements SmsCodeMapping {

    @Override
    public ErrorCode apply(String apiCode) {
        return switch (apiCode) {
            case "200" -> GlobalErrorCodeConstants.SUCCESS;
            default -> SmsFrameworkErrorCodeConstants.SMS_UNKNOWN;
        };
    }

}
