package xyz.migoo.framework.security.core.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.common.util.ServletUtils;
import xyz.migoo.framework.security.core.util.SecurityFrameworkUtils;
import xyz.migoo.framework.web.i18n.I18NMessage;

import java.io.IOException;

import static xyz.migoo.framework.common.exception.enums.GlobalErrorCodeConstants.FORBIDDEN;

/**
 * 访问一个需要认证的 URL 资源，已经认证（登录）但是没有权限的情况下，返回 {@link GlobalErrorCodeConstants#FORBIDDEN} 错误码。
 * <p>
 * 补充：Spring Security 通过 {@link ExceptionTranslationFilter#handleAccessDeniedException(HttpServletRequest, HttpServletResponse, FilterChain, AccessDeniedException)} 方法，调用当前类
 *
 * @author xiaomi
 */
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("JavadocReference")
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    private final I18NMessage i18n;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e)
            throws IOException, ServletException {
        // 打印 warn 的原因是，不定期合并 warn，看看有没恶意破坏
        log.warn("[commence][访问 URL({}) 时，用户({}) 权限不够]", request.getRequestURI(),
                SecurityFrameworkUtils.getLoginUserId(), e);
        // 返回 403
        var message = i18n.getMessage(FORBIDDEN.msg());
        ServletUtils.writeJSON(response, Result.error(FORBIDDEN.code(), message));
    }

}
