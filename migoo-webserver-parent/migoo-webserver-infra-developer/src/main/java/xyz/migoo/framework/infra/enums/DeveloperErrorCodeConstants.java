package xyz.migoo.framework.infra.enums;

import xyz.migoo.framework.common.exception.ErrorCode;

public class DeveloperErrorCodeConstants {

    // ========== 定时任务 100400100 ==========
    public static final ErrorCode JOB_NOT_EXISTS = ErrorCode.of(100400100, "infra.job.not.exists");
    public static final ErrorCode JOB_HANDLER_EXISTS = ErrorCode.of(100400101, "infra.job.handler.exists");
    public static final ErrorCode JOB_CHANGE_STATUS_INVALID = ErrorCode.of(100400102, "infra.job.staus.invalid");
    public static final ErrorCode JOB_CHANGE_STATUS_EQUALS = ErrorCode.of(100400103, "infra.job.status.equals");
    public static final ErrorCode JOB_CRON_EXPRESSION_INVALID = ErrorCode.of(100400105, "infra.job.corn.expression.invalid");


    // ========== 短信渠道 100501100 ==========
    public static final ErrorCode SMS_CHANNEL_NOT_EXISTS = ErrorCode.of(100501100, "infra.sms.channel.not.exists");
    public static final ErrorCode SMS_CHANNEL_DISABLE = ErrorCode.of(100501101, "infra.sms.channel.disabled");
    public static final ErrorCode SMS_CHANNEL_HAS_CHILDREN = ErrorCode.of(100501102, "infra.sms.channel.has.template");

    // ========== 短信模板 100501200 ==========
    public static final ErrorCode SMS_TEMPLATE_NOT_EXISTS = ErrorCode.of(100501200, "infra.sms.template.not.exists");
    public static final ErrorCode SMS_TEMPLATE_CODE_DUPLICATE = ErrorCode.of(100501201, "infra.sms.template.code.exists");

    // ========== 短信发送 100501300 ==========
    public static final ErrorCode SMS_SEND_MOBILE_NOT_EXISTS = ErrorCode.of(100501300, "infra.sms.send.phone.error");
    public static final ErrorCode SMS_SEND_MOBILE_TEMPLATE_PARAM_MISS = ErrorCode.of(100501301, "infra.sms.send.template.parameter.miss");
    public static final ErrorCode SMS_SEND_TEMPLATE_NOT_EXISTS = ErrorCode.of(100501302, "infra.sms.send.template.error");

}
