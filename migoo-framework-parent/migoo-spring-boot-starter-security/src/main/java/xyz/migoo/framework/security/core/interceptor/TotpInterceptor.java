package xyz.migoo.framework.security.core.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import xyz.migoo.framework.security.core.annotation.RequiresTotp;
import xyz.migoo.framework.security.core.service.TotpService;

import java.util.Objects;


/**
 * TOTP 二次验证拦截器
 * <p>
 * 检查带有 {@link RequiresTotp} 注解的方法，执行 TOTP 验证
 *
 * @author xiaomi
 */
@AllArgsConstructor
public class TotpInterceptor implements HandlerInterceptor {

    private final TotpService totpService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        if (handler instanceof HandlerMethod handlerMethod) {
            if (Objects.nonNull(handlerMethod.getMethod().getAnnotation(RequiresTotp.class))) {
                this.totpService.verify(request.getParameter("_code"));
            }
        }
        return true;
    }
}
