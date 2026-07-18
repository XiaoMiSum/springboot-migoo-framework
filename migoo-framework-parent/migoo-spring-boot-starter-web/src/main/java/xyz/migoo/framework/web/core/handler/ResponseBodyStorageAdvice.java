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

/**
 * 全局响应结果存储处理器，将 Result 存入 RequestAttribute 供下游模块使用
 */
@ControllerAdvice
public class ResponseBodyStorageAdvice implements ResponseBodyAdvice<Object> {

    @Override
    @SuppressWarnings("NullableProblems")
    public boolean supports(MethodParameter returnType, Class converterType) {
        if (returnType.getMethod() == null) {
            return false;
        }
        return returnType.getMethod().getReturnType() == Result.class;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        Result<?> result = (Result<?>) body;
        WebFrameworkUtils.setResult(((ServletServerHttpRequest) request).getServletRequest(), result);
        return body;
    }
}
