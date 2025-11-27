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
     * token 验证用户登录状态
     */
    @NotNull(message = "token Token不能为空")
    private Token token = new Token();

    /**
     * 登出url
     */
    @NotEmpty(message = "passwordSecret 密码加密密钥不能为空")
    private String passwordSecret;

    /**
     * 登出url
     */
    @NotEmpty(message = "logoutUrl 登出url地址不能为空")
    private String logoutUrl;

    /**
     * 用户可以任意访问的url
     */

    @NotNull(message = "permitAllUrls 允许任意访问的url不能为空")
    private List<String> permitAllUrls = Lists.newArrayList();

    @Validated
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Token {

        /**
         * HTTP 请求时，访问令牌的请求 Header
         */
        @NotEmpty(message = "headerName token请求头名称不能为空")
        private String headerName = "Authorization";

        /**
         * Session 过期时间
         * <p>
         * 当 User 用户超过当前时间未操作，则 Session 会过期
         */
        @NotNull(message = "timeout Session过期时间不能为空")
        private Duration timeout = Duration.ofMinutes(30);

        /**
         * Token 秘钥
         */
        @NotEmpty(message = "Token 秘钥不能为空")
        private String secret;

        /**
         * Token 刷新的过期时间
         * <p>
         * 默认 7 天
         */
        @NotNull(message = "refreshTimeout Token刷新的过期时间不能为空")
        private Duration refreshTimeout = Duration.ofDays(7);
        /**
         * Token 刷新的请求 Header
         * <p>
         * 默认：X-Refresh-Token
         */
        @NotEmpty(message = "refreshHeaderName Token刷新的请求Header不能为空")
        private String refreshHeaderName = "X-Refresh-Token";
    }
}
