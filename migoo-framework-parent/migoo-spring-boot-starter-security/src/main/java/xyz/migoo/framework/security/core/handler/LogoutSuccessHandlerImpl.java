package xyz.migoo.framework.security.core.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.util.StringUtils;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.web.core.util.ServletUtils;
import xyz.migoo.framework.security.config.SecurityProperties;
import xyz.migoo.framework.security.core.AuthUserDetails;
import xyz.migoo.framework.security.core.authentication.AuthUserDetailsFetcher;
import xyz.migoo.framework.security.core.util.SecurityFrameworkUtils;


/**
 * 自定义退出处理器
 *
 * @author ruoyi
 */
public record LogoutSuccessHandlerImpl(SecurityProperties securityProperties,
                                       AuthUserDetailsFetcher<? extends AuthUserDetails<?, ?>> userDetailsFetcher)
        implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, Authentication authentication) {
        // 执行退出
        String token = SecurityFrameworkUtils.obtainAuthorization(request, securityProperties.getJwt().getHeaderName());
        if (StringUtils.hasText(token)) {
            userDetailsFetcher.clean(token);
        }
        // 返回成功
        ServletUtils.writeJSON(response, Result.ok());
    }

}
