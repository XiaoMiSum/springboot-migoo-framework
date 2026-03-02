package xyz.migoo.framework.security.core.resolver;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import xyz.migoo.framework.security.config.SecurityProperties;
import xyz.migoo.framework.security.core.AuthUserDetails;
import xyz.migoo.framework.security.core.annotation.AuthUser;
import xyz.migoo.framework.security.core.service.SecurityAuthFrameworkService;
import xyz.migoo.framework.security.core.util.SecurityFrameworkUtils;

import java.util.Objects;

/**
 * @author xiaomi
 * Created on 2021/11/22 19:33
 */
public class AuthUserMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Resource
    private SecurityProperties properties;
    @Resource
    private SecurityAuthFrameworkService<? extends AuthUserDetails<?>> authService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return AuthUserDetails.class.isAssignableFrom(parameter.getParameterType())
                && parameter.hasParameterAnnotation(AuthUser.class);
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter, ModelAndViewContainer container, @NonNull NativeWebRequest request,
                                  WebDataBinderFactory binder) throws Exception {
        var user = SecurityFrameworkUtils.getLoginUser();
        if (Objects.nonNull(user)) {
            return user;
        }
        // 兼容无需登录的 url 也可以从 token中获取 user
        var token = SecurityFrameworkUtils.obtainAuthorization(request, properties.getAuthorization().getHeaderName());
        if (StrUtil.isNotBlank(token)) {
            var authUserDetails = authService.verifyToken(token);
            if (Objects.nonNull(authUserDetails)) {
                return authUserDetails;
            }
        }
        // 如果允许返回null, 则返回 null
        var annotation = parameter.getParameterAnnotation(AuthUser.class);
        if (annotation != null && !annotation.required()) {
            return null;
        }
        throw new MissingServletRequestPartException("currentUser");
    }
}
