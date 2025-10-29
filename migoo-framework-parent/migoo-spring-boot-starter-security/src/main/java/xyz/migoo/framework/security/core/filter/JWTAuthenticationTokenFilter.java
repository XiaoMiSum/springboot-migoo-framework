package xyz.migoo.framework.security.core.filter;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.filter.OncePerRequestFilter;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.common.util.servlet.ServletUtils;
import xyz.migoo.framework.security.config.SecurityProperties;
import xyz.migoo.framework.security.core.LoginUser;
import xyz.migoo.framework.security.core.service.SecurityAuthFrameworkService;
import xyz.migoo.framework.security.core.util.SecurityFrameworkUtils;
import xyz.migoo.framework.web.core.handler.GlobalExceptionHandler;
import xyz.migoo.framework.web.core.util.WebFrameworkUtils;
import xyz.migoo.framework.web.i18n.I18NMessage;

import java.io.IOException;

import static xyz.migoo.framework.common.exception.enums.GlobalErrorCodeConstants.FORBIDDEN;

/**
 * JWT 过滤器，验证 token 的有效性
 * 验证通过后，获得 {@link LoginUser} 信息，并加入到 Spring Security 上下文
 *
 * @author xiaomi
 */
@AllArgsConstructor
@Slf4j
public class JWTAuthenticationTokenFilter extends OncePerRequestFilter {

    private final SecurityProperties securityProperties;

    private final SecurityAuthFrameworkService authService;

    private final GlobalExceptionHandler globalExceptionHandler;

    private final I18NMessage i18NMessage;

    @Override
    @SuppressWarnings("NullableProblems")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = SecurityFrameworkUtils.obtainAuthorization(request, securityProperties.getToken().getHeaderName());
        if (StrUtil.isNotEmpty(token)) {
            try {
                // 验证 token 有效性
                LoginUser loginUser = authService.verifyTokenAndRefresh(token);
                // 设置当前用户
                if (loginUser != null) {
                    SecurityFrameworkUtils.setLoginUser(loginUser, request);
                }
            } catch (Throwable ex) {
                Result<?> result = ex instanceof AccessDeniedException e ? accessDeniedExceptionHandler(request, e)
                        : globalExceptionHandler.allExceptionHandler(request, ex);
                ServletUtils.writeJSON(response, result);
                return;
            }
        }

        // 继续过滤链
        chain.doFilter(request, response);
    }

    /**
     * 处理 Spring Security 权限不足的异常
     * <p>
     * 来源是，使用 @PreAuthorize 注解，AOP 进行权限拦截
     */
    public Result<?> accessDeniedExceptionHandler(HttpServletRequest req, AccessDeniedException ex) {
        log.warn("[accessDeniedExceptionHandler][userId({}) 无法访问 url({})]", WebFrameworkUtils.getLoginUserId(req),
                req.getRequestURL(), ex);
        return Result.getError(FORBIDDEN.getCode(), i18NMessage.getMessage(FORBIDDEN.getMsg()));
    }

}
