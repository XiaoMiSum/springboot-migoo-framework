package xyz.migoo.framework.security.core.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.common.util.servlet.ServletUtils;
import xyz.migoo.framework.web.i18n.I18NMessage;

import static xyz.migoo.framework.common.exception.enums.GlobalErrorCodeConstants.UNAUTHORIZED;

/**
 * 访问一个需要认证的 URL 资源，但是此时自己尚未认证（登录）的情况下，返回 {@link GlobalErrorCodeConstants#UNAUTHORIZED} 错误码，从而使前端重定向到登录页
 * <p>
 * 补充：Spring Security 通过 {@link ExceptionTranslationFilter#sendStartAuthentication(HttpServletRequest, HttpServletResponse, FilterChain, AuthenticationException)} 方法，调用当前类
 *
 * @author ruoyi
 */
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("JavadocReference")
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    private final I18NMessage i18n;

    @Override
    public void commence(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull AuthenticationException e) {
        log.debug("[commence][访问 URL({}) 时，没有登录]", request.getRequestURI(), e);
        // 返回 401
        var message = i18n.getMessage(UNAUTHORIZED.msg());
        ServletUtils.writeJSON(response, Result.error(UNAUTHORIZED.code(), message));
    }

}
