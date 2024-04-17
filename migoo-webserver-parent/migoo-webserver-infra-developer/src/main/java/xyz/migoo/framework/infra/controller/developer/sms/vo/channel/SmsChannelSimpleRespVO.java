package xyz.migoo.framework.infra.controller.developer.sms.vo.channel;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SmsChannelSimpleRespVO {

    @NotNull(message = "编号不能为空")
    private Long id;

    @NotNull(message = "短信签名不能为空")
    private String signature;

    private String code;

}
