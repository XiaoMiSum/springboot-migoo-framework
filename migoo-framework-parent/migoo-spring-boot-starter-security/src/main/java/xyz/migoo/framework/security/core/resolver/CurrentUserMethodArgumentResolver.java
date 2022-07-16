package xyz.migoo.framework.security.core.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import xyz.migoo.framework.security.config.SecurityProperties;
import xyz.migoo.framework.security.core.LoginUser;
import xyz.migoo.framework.security.core.annotations.CurrentUser;
import xyz.migoo.framework.security.core.service.SecuritySessionAuthService;
import xyz.migoo.framework.security.core.util.SecurityFrameworkUtils;

import javax.annotation.Resource;

/**
 * @author xiaomi
 * Created on 2021/11/22 19:33
 */
public class CurrentUserMethodArgumentResolver implements HandlerMethodArgumentResolver {


    @Resource
    private SecuritySessionAuthService loginUserService;
    @Resource
    private SecurityProperties properties;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().isAssignableFrom(LoginUser.class)
                && parameter.hasParameterAnnotation(CurrentUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer container, NativeWebRequest request, WebDataBinderFactory binder) throws Exception {
        String token = SecurityFrameworkUtils.obtainAuthorization(request, properties.getTokenHeader());
        Object user = loginUserService.getLoginUser(token);
        if (user != null) {
            return user;
        }
        throw new MissingServletRequestPartException("currentUser");
    }
}
