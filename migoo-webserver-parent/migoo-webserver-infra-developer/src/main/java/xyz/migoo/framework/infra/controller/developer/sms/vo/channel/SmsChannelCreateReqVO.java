package xyz.migoo.framework.infra.controller.developer.sms.vo.channel;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SmsChannelCreateReqVO extends SmsChannelBaseVO {

    @NotNull(message = "{infra.sms.channel.code.empty}")
    private String code;

}
