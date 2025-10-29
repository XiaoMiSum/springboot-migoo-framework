package xyz.migoo.framework.infra.controller.developer.sms.vo.template;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class SmsTemplateSendReqVO {

    @NotNull(message = "{infra.sms.send.phone.empty}")
    private String mobile;

    @NotNull(message = "{infra.sms.send.template.code.empty}")
    private String templateCode;

    private Map<String, Object> templateParams;

}
