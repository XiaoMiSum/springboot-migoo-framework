package xyz.migoo.franework.infra.controller.developer.sms.vo.channel;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SmsChannelCreateReqVO extends SmsChannelBaseVO {

    @NotNull(message = "渠道编码不能为空")
    private String code;

}
