package xyz.migoo.framework.security.core.resolver;

import jakarta.annotation.Resource;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import xyz.migoo.framework.security.config.SecurityProperties;
import xyz.migoo.framework.security.core.annotation.Token;
import xyz.migoo.framework.security.core.util.SecurityFrameworkUtils;

/**
 * @author xiaomi
 * Created on 2021/11/22 19:33
 */
public class TokenMethodArgumentResolver implements HandlerMethodArgumentResolver {
    @Resource
    private SecurityProperties properties;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return String.class.isAssignableFrom(parameter.getParameterType())
                && parameter.hasParameterAnnotation(Token.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer container, NativeWebRequest request, WebDataBinderFactory binder) throws Exception {
        Object token = SecurityFrameworkUtils.obtainAuthorization(request, properties.getToken().getHeaderName());
        if (token != null) {
            return token;
        }
        throw new MissingServletRequestPartException("token");
    }
}
