package xyz.migoo.framework.infra.enums;

import xyz.migoo.framework.common.exception.ErrorCode;

public interface DeveloperErrorCodeConstants {

    // ========== 定时任务 1004001000 ==========
    ErrorCode JOB_NOT_EXISTS = new ErrorCode(1004001000, "定时任务不存在");
    ErrorCode JOB_HANDLER_EXISTS = new ErrorCode(1004001001, "定时任务的处理器已经存在");
    ErrorCode JOB_CHANGE_STATUS_INVALID = new ErrorCode(1004001002, "只允许修改为开启或者关闭状态");
    ErrorCode JOB_CHANGE_STATUS_EQUALS = new ErrorCode(1004001003, "定时任务已经处于该状态，无需修改");
    ErrorCode JOB_UPDATE_ONLY_NORMAL_STATUS = new ErrorCode(1004001004, "只有开启状态的任务，才可以修改");
    ErrorCode JOB_CRON_EXPRESSION_VALID = new ErrorCode(1004001005, "CRON 表达式不正确");


    // ========== 短信渠道 1005011000 ==========
    ErrorCode SMS_CHANNEL_NOT_EXISTS = new ErrorCode(1005011000, "短信渠道不存在");
    ErrorCode SMS_CHANNEL_DISABLE = new ErrorCode(1005011001, "短信渠道不处于开启状态，不允许选择");
    ErrorCode SMS_CHANNEL_HAS_CHILDREN = new ErrorCode(1005011002, "无法删除，该短信渠道还有短信模板");

    // ========== 短信模板 1005012000 ==========
    ErrorCode SMS_TEMPLATE_NOT_EXISTS = new ErrorCode(1005012000, "短信模板不存在");
    ErrorCode SMS_TEMPLATE_CODE_DUPLICATE = new ErrorCode(1005012001, "已经存在编码为【{}】的短信模板");

    // ========== 短信发送 1005013000 ==========
    ErrorCode SMS_SEND_MOBILE_NOT_EXISTS = new ErrorCode(1005013000, "手机号不存在");
    ErrorCode SMS_SEND_MOBILE_TEMPLATE_PARAM_MISS = new ErrorCode(1005013001, "模板参数({})缺失");
    ErrorCode SMS_SEND_TEMPLATE_NOT_EXISTS = new ErrorCode(1005013002, "短信模板不存在");

    // ========== 短信验证码 1002014000 ==========
    ErrorCode SMS_CODE_NOT_FOUND = new ErrorCode(1002014000, "验证码不存在");
    ErrorCode SMS_CODE_EXPIRED = new ErrorCode(1002014001, "验证码已过期");
    ErrorCode SMS_CODE_USED = new ErrorCode(1002014002, "验证码已使用");
    ErrorCode SMS_CODE_NOT_CORRECT = new ErrorCode(1002014003, "验证码不正确");
    ErrorCode SMS_CODE_EXCEED_SEND_MAXIMUM_QUANTITY_PER_DAY = new ErrorCode(1002014004, "超过每日短信发送数量");
    ErrorCode SMS_CODE_SEND_TOO_FAST = new ErrorCode(1002014005, "短信发送过于频率");
    ErrorCode SMS_CODE_IS_EXISTS = new ErrorCode(1002014006, "手机号已被使用");
    ErrorCode SMS_CODE_IS_UNUSED = new ErrorCode(1002014007, "验证码未被使用");
    ErrorCode OPTION_ERROR = new ErrorCode(9999, "操作失败，请检查");


    // ========== 字典模块 1006010000 ==========
    ErrorCode DICTIONARY_KEY_NOT_EXISTS = new ErrorCode(1006010000, "字典不存在");
    ErrorCode DICTIONARY_KEY_EXISTS = new ErrorCode(1006010001, "字典已存在");

    // ========== 模块 99999999 ==========
    ErrorCode ERROR = new ErrorCode(99999999, "异常操作");
}
