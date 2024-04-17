package xyz.migoo.framework.infra.controller.developer.sms.vo.template;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SmsTemplateRespVO extends SmsTemplateBaseVO {

    private Long id;

    private String channelCode;

    private List<String> params;

    private LocalDateTime createTime;

}
