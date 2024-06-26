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

    @NotNull(message = "模板编码不能为空")
    private String code;

    @NotNull(message = "模板名称不能为空")
    private String name;

    @NotNull(message = "模板内容不能为空")
    private String content;

    @NotNull(message = "短信 API 的模板编号不能为空")
    private String apiTemplateId;

    @NotNull(message = "短信渠道编号不能为空")
    private Long channelId;

}
