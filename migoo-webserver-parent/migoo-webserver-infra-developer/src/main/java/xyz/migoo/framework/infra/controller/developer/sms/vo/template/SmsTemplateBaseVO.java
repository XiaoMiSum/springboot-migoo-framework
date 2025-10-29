package xyz.migoo.framework.infra.controller.developer.sms.vo.template;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 短信模板 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class SmsTemplateBaseVO {

    private Integer status;

    @NotNull(message = "{infra.sms.template.code.empty}")
    private String code;

    @NotNull(message = "{infra.sms.template.name.empty}")
    private String name;

    @NotNull(message = "{infra.sms.template.content.empty}")
    private String content;

    @NotNull(message = "{infra.sms.template.id.empty}")
    private String apiTemplateId;

    @NotNull(message = "{infra.sms.channel.id.empty}")
    private Long channelId;

}
