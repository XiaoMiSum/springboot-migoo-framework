package xyz.migoo.framework.security.core.interceptor;

import lombok.AllArgsConstructor;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import xyz.migoo.framework.security.core.annotations.Authenticator;
import xyz.migoo.framework.security.core.service.SecurityAuthenticatorService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;


/**
 * 身份验证器拦截
 *
 * @author xiaomi
 * Created on 2021/11/21 15:41
 */
@AllArgsConstructor
public class AuthenticatorInterceptor implements HandlerInterceptor {

    private final SecurityAuthenticatorService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            if (Objects.nonNull(((HandlerMethod) handler).getMethod().getAnnotation(Authenticator.class))) {
                String securityCode = this.authService.getSecurityCode(request.getParameter("token"));
                this.authService.verify(securityCode, request.getParameter("_code"));
            }
        }
        return true;
    }
}