package xyz.migoo.framework.infra.controller.developer.sms.vo.channel;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SmsChannelRespVO extends SmsChannelBaseVO {

    private Long id;

    private String code;

    private LocalDateTime createTime;

}
