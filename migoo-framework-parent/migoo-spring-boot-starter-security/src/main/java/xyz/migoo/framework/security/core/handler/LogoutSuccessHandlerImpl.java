package xyz.migoo.framework.security.core.handler;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.common.util.servlet.ServletUtils;
import xyz.migoo.framework.security.config.SecurityProperties;
import xyz.migoo.framework.security.core.AuthUserDetails;
import xyz.migoo.framework.security.core.service.SecurityAuthFrameworkService;
import xyz.migoo.framework.security.core.util.SecurityFrameworkUtils;


/**
 * 自定义退出处理器
 *
 * @author ruoyi
 */
public record LogoutSuccessHandlerImpl(SecurityProperties securityProperties,
                                       SecurityAuthFrameworkService<? extends AuthUserDetails<?>> securityFrameworkService)
        implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // 执行退出
        String token = SecurityFrameworkUtils.obtainAuthorization(request, securityProperties.getAuthorization().getHeaderName());
        if (StrUtil.isNotBlank(token)) {
            securityFrameworkService.clean(token);
        }
        // 返回成功
        ServletUtils.writeJSON(response, Result.ok());
    }

}
