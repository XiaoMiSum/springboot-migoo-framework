package xyz.migoo.franework.infra.controller.developer.sms.vo.template;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class SmsTemplateSendReqVO {

    @NotNull(message = "手机号不能为空")
    private String mobile;

    @NotNull(message = "模板编码不能为空")
    private String templateCode;

    private Map<String, Object> templateParams;

}
