package xyz.migoo.franework.infra.controller.developer.sms.vo.channel;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SmsChannelUpdateReqVO extends SmsChannelBaseVO {

    @NotNull(message = "编号不能为空")
    private Long id;

}
