package xyz.migoo.franework.infra.controller.developer.sms.vo.template;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SmsTemplateUpdateReqVO extends SmsTemplateBaseVO {

    @NotNull(message = "编号不能为空")
    private Long id;

}
