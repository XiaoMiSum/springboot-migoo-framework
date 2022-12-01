package xyz.migoo.framework.security.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@ConfigurationProperties(prefix = "migoo.security")
@Validated
@Data
public class SecurityProperties {

    /**
     * HTTP 请求时，访问令牌的请求 Header
     */
    @NotEmpty(message = "Token Header 不能为空")
    private String tokenHeader;
    /**
     * Token 过期时间
     */
    @NotNull(message = "Token 过期时间不能为空")
    private Duration tokenTimeout;
    /**
     * Token 秘钥
     */
    @NotEmpty(message = "Token 秘钥不能为空")
    private String tokenSecret;
    /**
     * Session 过期时间
     * <p>
     * 当 User 用户超过当前时间未操作，则 Session 会过期
     */
    @NotNull(message = "Session 过期时间不能为空")
    private Duration sessionTimeout;

}
