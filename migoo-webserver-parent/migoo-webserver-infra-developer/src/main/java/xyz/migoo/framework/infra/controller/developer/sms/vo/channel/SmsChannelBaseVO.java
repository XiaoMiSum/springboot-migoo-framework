package xyz.migoo.framework.infra.controller.developer.sms.vo.channel;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

/**
 * 短信渠道 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class SmsChannelBaseVO {

    @NotNull(message = "短信签名不能为空")
    private String signature;

    private Integer status;

    @NotNull(message = "短信 API 的账号不能为空")
    private String apiKey;

    private String apiSecret;

    @URL(message = "回调 URL 格式不正确")
    private String callbackUrl;

}
