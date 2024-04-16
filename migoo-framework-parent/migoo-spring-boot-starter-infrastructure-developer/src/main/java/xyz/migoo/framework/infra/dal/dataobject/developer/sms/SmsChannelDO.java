package xyz.migoo.framework.infra.dal.dataobject.developer.sms;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import xyz.migoo.framework.common.enums.CommonStatusEnum;
import xyz.migoo.framework.sms.core.enums.SmsChannelEnum;
import xyz.migoo.framework.infra.dal.dataobject.IdEnhanceDO;


@TableName(value = "infra_sms_channel")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsChannelDO extends IdEnhanceDO {

    /**
     * 短信签名
     */
    private String signature;
    /**
     * 渠道编码
     * <p>
     * 枚举 {@link SmsChannelEnum}
     */
    private String code;
    /**
     * 启用状态
     * <p>
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;
    /**
     * 短信 API 的账号
     */
    private String apiKey;
    /**
     * 短信 API 的密钥
     */
    private String apiSecret;
    /**
     * 短信发送回调 URL
     */
    private String callbackUrl;

}
