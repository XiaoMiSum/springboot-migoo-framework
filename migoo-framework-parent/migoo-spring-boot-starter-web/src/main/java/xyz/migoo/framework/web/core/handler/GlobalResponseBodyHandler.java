package xyz.migoo.framework.web.core.handler;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.web.core.util.WebFrameworkUtils;
import xyz.migoo.framework.web.i18n.I18NMessage;

/**
 * 全局响应结果（ResponseBody）处理器
 */
@ControllerAdvice
public class GlobalResponseBodyHandler implements ResponseBodyAdvice<Object> {


    private final I18NMessage i18n;

    public GlobalResponseBodyHandler(I18NMessage i18n) {
        this.i18n = i18n;
    }

    @Override
    @SuppressWarnings("NullableProblems") // 避免 IDEA 警告
    public boolean supports(MethodParameter returnType, Class converterType) {
        if (returnType.getMethod() == null) {
            return false;
        }
        // 只拦截返回结果为 CommonResult 类型
        return returnType.getMethod().getReturnType() == Result.class;
    }

    @Override
    @SuppressWarnings("NullableProblems") // 避免 IDEA 警告
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        // 记录 Controller 结果
        Result<?> result = (Result<?>) body;
        WebFrameworkUtils.setResult(((ServletServerHttpRequest) request).getServletRequest(), result);
        result.setMsg(i18n.getMessage(result.getMsg()));
        return body;
    }

}
