package xyz.migoo.framework.infra.enums;

import xyz.migoo.framework.common.exception.ErrorCode;

public class DeveloperErrorCodeConstants {

    // ========== 定时任务 100400100 ==========
    public static final ErrorCode JOB_NOT_EXISTS = ErrorCode.of(100400100, "定时任务不存在");
    public static final ErrorCode JOB_HANDLER_EXISTS = ErrorCode.of(100400101, "定时任务的处理器已经存在");
    public static final ErrorCode JOB_CHANGE_STATUS_INVALID = ErrorCode.of(100400102, "只允许修改为开启或者关闭状态");
    public static final ErrorCode JOB_CHANGE_STATUS_EQUALS = ErrorCode.of(100400103, "定时任务已经处于该状态，无需修改");
    public static final ErrorCode JOB_UPDATE_ONLY_NORMAL_STATUS = ErrorCode.of(100400104, "只有开启状态的任务，才可以修改");
    public static final ErrorCode JOB_CRON_EXPRESSION_VALID = ErrorCode.of(100400105, "CRON 表达式不正确");


    // ========== 短信渠道 100501100 ==========
    public static final ErrorCode SMS_CHANNEL_NOT_EXISTS = ErrorCode.of(100501100, "短信渠道不存在");
    public static final ErrorCode SMS_CHANNEL_DISABLE = ErrorCode.of(100501101, "短信渠道不处于开启状态，不允许选择");
    public static final ErrorCode SMS_CHANNEL_HAS_CHILDREN = ErrorCode.of(100501102, "无法删除，该短信渠道还有短信模板");

    // ========== 短信模板 100501200 ==========
    public static final ErrorCode SMS_TEMPLATE_NOT_EXISTS = ErrorCode.of(100501200, "短信模板不存在");
    public static final ErrorCode SMS_TEMPLATE_CODE_DUPLICATE = ErrorCode.of(100501201, "已经存在编码为【{}】的短信模板");

    // ========== 短信发送 100501300 ==========
    public static final ErrorCode SMS_SEND_MOBILE_NOT_EXISTS = ErrorCode.of(100501300, "手机号不存在");
    public static final ErrorCode SMS_SEND_MOBILE_TEMPLATE_PARAM_MISS = ErrorCode.of(100501301, "模板参数({})缺失");
    public static final ErrorCode SMS_SEND_TEMPLATE_NOT_EXISTS = ErrorCode.of(100501302, "短信模板不存在");

    // ========== 短信验证码 100201400 ==========
    public static final ErrorCode SMS_CODE_NOT_FOUND = ErrorCode.of(100201400, "验证码不存在");
    public static final ErrorCode SMS_CODE_EXPIRED = ErrorCode.of(100201401, "验证码已过期");
    public static final ErrorCode SMS_CODE_USED = ErrorCode.of(100201402, "验证码已使用");
    public static final ErrorCode SMS_CODE_NOT_CORRECT = ErrorCode.of(100201403, "验证码不正确");
    public static final ErrorCode SMS_CODE_EXCEED_SEND_MAXIMUM_QUANTITY_PER_DAY = ErrorCode.of(100201404, "超过每日短信发送数量");
    public static final ErrorCode SMS_CODE_SEND_TOO_FAST = ErrorCode.of(100201405, "短信发送过于频率");
    public static final ErrorCode SMS_CODE_IS_EXISTS = ErrorCode.of(100201406, "手机号已被使用");
    public static final ErrorCode SMS_CODE_IS_UNUSED = ErrorCode.of(100201407, "验证码未被使用");


    static {
        ErrorCode.put(DeveloperErrorCodeConstants.class);
    }
}
