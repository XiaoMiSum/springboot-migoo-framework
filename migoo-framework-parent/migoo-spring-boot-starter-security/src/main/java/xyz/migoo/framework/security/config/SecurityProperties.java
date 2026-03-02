package xyz.migoo.framework.security.config;

import com.google.common.collect.Lists;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "migoo.security")
@Validated
@Data
public class SecurityProperties {

    /**
     * authorization 验证用户登录状态
     */
    @NotNull(message = "authorization 不能为空")
    private Authorization authorization = new Authorization();

    /**
     * 登出url
     */
    @NotEmpty(message = "password-secret 密码加密密钥不能为空")
    private String passwordSecret;

    /**
     * 登出url
     */
    @NotEmpty(message = "logoutU-url 登出url地址不能为空")
    private String logoutUrl;

    /**
     * 用户可以任意访问的url
     */
    @NotNull(message = "permit-all-urls 允许任意访问的url不能为空")
    private List<String> permitAllUrls = Lists.newArrayList();

    @Validated
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Authorization {

        /**
         * HTTP 请求时，访问令牌的请求 Header
         */
        @NotEmpty(message = "headerName 请求头名称不能为空")
        private String headerName = "Authorization";

        /**
         * access token 过期时间
         * <p>
         * 默认 30 分钟
         */
        @NotNull(message = "access-authorization-expires 不能为空")
        private Duration accessTokenExpires = Duration.ofMinutes(30);

        /**
         * token 秘钥
         */
        @NotEmpty(message = "secret-key 不能为空")
        private String secretKey;

        /**
         * Authorization 刷新的过期时间
         * <p>
         * 默认 7 天
         */
        @NotNull(message = "refresh-authorization-expires 不能为空")
        private Duration refreshTokenExpires = Duration.ofDays(7);
        /**
         * Authorization 刷新的请求 Header
         * <p>
         * 默认：X-Refresh-Token
         */
        @NotEmpty(message = "refreshHeaderName Token刷新的请求Header不能为空")
        private String refreshHeaderName = "X-Refresh-Token";
    }
}
