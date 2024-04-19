package xyz.migoo.framework.captcha.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * 验证码配置
 *
 * @author xiaomi
 * Created on 2021/11/21 16:47
 */
@ConfigurationProperties(prefix = "migoo.captcha")
@Validated
@Data
public class CaptchaProperties {

    private Boolean enable;

    /**
     * 验证码的过期时间
     */
    @NotNull(message = "验证码的过期时间不为空")
    private Duration timeout = Duration.ofMinutes(5);
    /**
     * 验证码的高度
     */
    @NotNull(message = "验证码的高度不能为空")
    private int height = 60;
    /**
     * 验证码的宽度
     */
    @NotNull(message = "验证码的宽度不能为空")
    private Integer width = 160;
}
