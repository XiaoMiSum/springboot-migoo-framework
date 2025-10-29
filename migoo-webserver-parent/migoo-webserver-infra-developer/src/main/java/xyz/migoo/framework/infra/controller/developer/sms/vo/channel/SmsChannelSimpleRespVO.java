package xyz.migoo.framework.infra.controller.developer.sms.vo.channel;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SmsChannelSimpleRespVO {

    @NotNull(message = "{infra.sms.channel.id.empty}")
    private Long id;

    @NotNull(message = "{infra.sms.channel.signature.empty}")
    private String signature;

    private String code;

}
