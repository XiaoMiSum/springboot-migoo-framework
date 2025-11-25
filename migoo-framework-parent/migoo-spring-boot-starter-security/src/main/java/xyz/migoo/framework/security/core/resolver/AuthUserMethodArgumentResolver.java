package xyz.migoo.framework.security.core.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import xyz.migoo.framework.security.core.AuthUserDetails;
import xyz.migoo.framework.security.core.annotation.AuthUser;
import xyz.migoo.framework.security.core.util.SecurityFrameworkUtils;

/**
 * @author xiaomi
 * Created on 2021/11/22 19:33
 */
public class AuthUserMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return AuthUserDetails.class.isAssignableFrom(parameter.getParameterType())
                && parameter.hasParameterAnnotation(AuthUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer container, NativeWebRequest request, WebDataBinderFactory binder) throws Exception {
        var user = SecurityFrameworkUtils.getLoginUser();
        var annotation = parameter.getParameterAnnotation(AuthUser.class);
        if (user != null) {
            return user;
        } else if (annotation != null && !annotation.required()) {
            return null;
        }
        throw new MissingServletRequestPartException("currentUser");
    }
}
