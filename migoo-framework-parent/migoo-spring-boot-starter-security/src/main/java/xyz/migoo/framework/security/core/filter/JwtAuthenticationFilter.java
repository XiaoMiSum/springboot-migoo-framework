package xyz.migoo.framework.security.core.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import xyz.migoo.framework.common.exception.ServiceExceptionUtil;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.common.util.ServletUtils;
import xyz.migoo.framework.security.config.SecurityProperties;
import xyz.migoo.framework.security.core.AuthUserDetails;
import xyz.migoo.framework.security.core.authentication.AuthUserDetailsFetcher;
import xyz.migoo.framework.security.core.util.SecurityFrameworkUtils;
import xyz.migoo.framework.web.core.handler.GlobalExceptionHandler;
import xyz.migoo.framework.web.core.util.WebFrameworkUtils;
import xyz.migoo.framework.web.i18n.I18NMessage;

import java.io.IOException;

import static xyz.migoo.framework.common.exception.enums.GlobalErrorCodeConstants.FORBIDDEN;
import static xyz.migoo.framework.common.exception.enums.GlobalErrorCodeConstants.UNAUTHORIZED;

/**
 * JWT 认证过滤器，验证 token 的有效性
 * 验证通过后，获得 {@link AuthUserDetails} 信息，并加入到 Spring Security 上下文
 *
 * @author xiaomi
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SecurityProperties securityProperties;

    private final AuthUserDetailsFetcher<? extends AuthUserDetails<?, ?>> userDetailsFetcher;

    private final GlobalExceptionHandler globalExceptionHandler;

    private final I18NMessage i18NMessage;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return securityProperties.getPermitAllUrls().stream()
                .anyMatch(pattern -> PathPatternRequestMatcher.withDefaults().matcher(pattern).matches(request));
    }

    @Override
    @SuppressWarnings("NullableProblems")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            String token = SecurityFrameworkUtils.obtainAuthorization(request, securityProperties.getJwt().getHeaderName());
            if (!StringUtils.hasText(token)) {
                throw ServiceExceptionUtil.get(UNAUTHORIZED);
            }
            var authUserDetails = userDetailsFetcher.verifyToken(token);
            if (authUserDetails != null) {
                SecurityFrameworkUtils.setLoginUser(authUserDetails, request);
            }
        } catch (Exception ex) {
            Result<?> result = ex instanceof AccessDeniedException e ? accessDeniedExceptionHandler(request, e)
                    : globalExceptionHandler.allExceptionHandler(request, ex);
            ServletUtils.writeJSON(response, result);
            return;
        }
        chain.doFilter(request, response);
    }

    public Result<?> accessDeniedExceptionHandler(HttpServletRequest req, AccessDeniedException ex) {
        log.warn("[accessDeniedExceptionHandler][userId({}) 无法访问 url({})]", WebFrameworkUtils.getLoginUserId(req),
                req.getRequestURL(), ex);
        return Result.error(FORBIDDEN.code(), i18NMessage.getMessage(FORBIDDEN.msg()));
    }
}
