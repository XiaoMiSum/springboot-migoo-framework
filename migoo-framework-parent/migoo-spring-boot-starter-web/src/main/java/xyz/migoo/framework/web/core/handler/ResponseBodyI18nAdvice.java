package xyz.migoo.framework.web.core.handler;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import xyz.migoo.framework.common.exception.GlobalErrorCodeConstants;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.web.i18n.I18NMessage;

import java.util.Objects;

/**
 * 全局响应结果 I18n 消息解析处理器，对成功响应的 msg 进行国际化解析
 */
@ControllerAdvice
public class ResponseBodyI18nAdvice implements ResponseBodyAdvice<Object> {

    private final I18NMessage i18n;

    public ResponseBodyI18nAdvice(I18NMessage i18n) {
        this.i18n = i18n;
    }

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
        if (Objects.equals(result.getCode(), GlobalErrorCodeConstants.SUCCESS.code())) {
            result.setMsg(i18n.getMessage(result.getMsg()));
        }
        return body;
    }
}
