package xyz.migoo.framework.infra.dal.dataobject.developer.sms;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;
import xyz.migoo.framework.infra.enums.sms.SmsReceiveStatusEnum;
import xyz.migoo.framework.infra.enums.sms.SmsSendStatusEnum;
import xyz.migoo.framework.mybatis.core.dataobject.BaseDO;
import xyz.migoo.framework.sms.core.enums.SmsFrameworkErrorCodeConstants;

import java.time.LocalDateTime;
import java.util.Map;

@TableName(value = "infra_sms_log", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsLogDO extends BaseDO<Long> {

    // ========= 渠道相关字段 =========

    /**
     * 短信渠道编号
     * <p>
     * 关联 {@link SmsChannelDO#getId()}
     */
    private Long channelId;
    /**
     * 短信渠道编码
     * <p>
     * 冗余 {@link SmsChannelDO#getCode()}
     */
    private String channelCode;

    // ========= 模板相关字段 =========

    /**
     * 模板编号
     * <p>
     * 关联 {@link SmsTemplateDO#getId()}
     */
    private Long templateId;
    /**
     * 模板编码
     * <p>
     * 冗余 {@link SmsTemplateDO#getCode()}
     */
    private String templateCode;
    /**
     * 基于 {@link SmsTemplateDO#getContent()} 格式化后的内容
     */
    private String templateContent;
    /**
     * 基于 {@link SmsTemplateDO#getParams()} 输入后的参数
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> templateParams;
    /**
     * 短信 API 的模板编号
     * <p>
     * 冗余 {@link SmsTemplateDO#getApiTemplateId()}
     */
    private String apiTemplateId;

    // ========= 手机相关字段 =========

    /**
     * 手机号
     */
    private String mobile;
    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 用户类型
     */
    private Integer userType;

    // ========= 发送相关字段 =========

    /**
     * 发送状态
     * <p>
     * 枚举 {@link SmsSendStatusEnum}
     */
    private Integer sendStatus;
    /**
     * 发送时间
     */
    private LocalDateTime sendTime;
    /**
     * 发送结果的编码
     * <p>
     * 枚举 {@link SmsFrameworkErrorCodeConstants}
     */
    private Integer sendCode;
    /**
     * 发送结果的提示
     * <p>
     * 一般情况下，使用 {@link SmsFrameworkErrorCodeConstants}
     * 异常情况下，通过格式化 Exception 的提示存储
     */
    private String sendMsg;
    /**
     * 短信 API 发送结果的编码
     * <p>
     * 由于第三方的错误码可能是字符串，所以使用 String 类型
     */
    private String apiSendCode;
    /**
     * 短信 API 发送失败的提示
     */
    private String apiSendMsg;
    /**
     * 短信 API 发送返回的唯一请求 ID
     * <p>
     * 用于和短信 API 进行定位于排错
     */
    private String apiRequestId;
    /**
     * 短信 API 发送返回的序号
     * <p>
     * 用于和短信 API 平台的发送记录关联
     */
    private String apiSerialNo;

    // ========= 接收相关字段 =========

    /**
     * 接收状态
     * <p>
     * 枚举 {@link SmsReceiveStatusEnum}
     */
    private Integer receiveStatus;
    /**
     * 接收时间
     */
    private LocalDateTime receiveTime;
    /**
     * 短信 API 接收结果的编码
     */
    private String apiReceiveCode;
    /**
     * 短信 API 接收结果的提示
     */
    private String apiReceiveMsg;

}
