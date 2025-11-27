package xyz.migoo.framework.web.core.handler;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.socket.SocketRuntimeException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import xyz.migoo.framework.apilog.core.ApiErrorLog;
import xyz.migoo.framework.apilog.core.ApiErrorLogFrameworkService;
import xyz.migoo.framework.common.exception.ServiceException;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.common.util.json.JsonUtils;
import xyz.migoo.framework.common.util.servlet.ServletUtils;
import xyz.migoo.framework.web.i18n.I18NMessage;

import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

import static xyz.migoo.framework.common.exception.enums.GlobalErrorCodeConstants.*;

/**
 * 全局异常处理器，将 Exception 翻译成 CommonResult + 对应的异常编号
 *
 * @author xiaomi
 */
@RestControllerAdvice
@AllArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final String applicationName;

    private final ApiErrorLogFrameworkService apiErrorLogFrameworkService;

    private final I18NMessage i18n;


    /**
     * 处理所有异常，主要是提供给 Filter 使用
     * 因为 Filter 不走 SpringMVC 的流程，但是我们又需要兜底处理异常，所以这里提供一个全量的异常处理过程，保持逻辑统一。
     *
     * @param request 请求
     * @param t       异常
     * @return 通用返回
     */
    public Result<?> allExceptionHandler(HttpServletRequest request, Throwable t) {
        return switch (t) {
            case HttpMediaTypeNotSupportedException e -> httpMediaTypeNotSupportedException(request, e);
            case MissingServletRequestParameterException e -> missingServletRequestParameterHandler(request, e);
            case MethodArgumentTypeMismatchException e -> methodArgumentTypeMismatchExceptionHandler(request, e);
            case MethodArgumentNotValidException e -> methodArgumentNotValidExceptionExceptionHandler(request, e);
            case BindException e -> bindExceptionHandler(request, e);
            case ConstraintViolationException e -> constraintViolationExceptionHandler(request, e);
            case ValidationException e -> validationException(request, e);
            case NoHandlerFoundException e -> noHandlerFoundExceptionHandler(request, e);
            case HttpRequestMethodNotSupportedException e -> httpRequestMethodNotSupportedExceptionHandler(request, e);
            case SocketRuntimeException e -> socketRuntimeExceptionHandler(request, e);
            case ServiceException e -> serviceExceptionHandler(request, e);
            default -> defaultExceptionHandler(request, t);
        };
    }

    /**
     * 处理 SpringMVC 请求Content-Type错误
     * <p>
     * 例如说，接口上设置了 consumes= application/x-www-form-urlencoded，结果传递的是 application/json
     */
    @ExceptionHandler(value = HttpMediaTypeNotSupportedException.class)
    @ResponseBody
    public Result<?> httpMediaTypeNotSupportedException(HttpServletRequest request, HttpMediaTypeNotSupportedException t) {
        var message = i18n.getMessage(BAD_REQUEST.msg());
        return Result.getError(BAD_REQUEST.code(), String.format("%s:%s", message, t.getContentType()));
    }

    /**
     * 处理 SpringMVC 请求参数缺失
     * <p>
     * 例如说，接口上设置了 @RequestParam("xx") 参数，结果并未传递 xx 参数
     */
    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public Result<?> missingServletRequestParameterHandler(HttpServletRequest request, MissingServletRequestParameterException ex) {
        var message = i18n.getMessage(BAD_REQUEST.msg());
        return Result.getError(BAD_REQUEST.code(), String.format("%s:%s", message, ex.getParameterName()));
    }

    /**
     * 处理 SpringMVC 请求参数类型错误
     * <p>
     * 例如说，接口上设置了 @RequestParam("xx") 参数为 Integer，结果传递 xx 参数类型为 String
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<?> methodArgumentTypeMismatchExceptionHandler(HttpServletRequest request, MethodArgumentTypeMismatchException ex) {
        var message = i18n.getMessage(BAD_REQUEST.msg());
        return Result.getError(BAD_REQUEST.code(), String.format("%s:%s", message, ex.getMessage()));
    }

    /**
     * 处理 SpringMVC 参数校验不正确
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> methodArgumentNotValidExceptionExceptionHandler(HttpServletRequest request, MethodArgumentNotValidException e) {
        var errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        var message = i18n.getMessage(BAD_REQUEST.msg());
        return Result.getError(BAD_REQUEST.code(), String.format("%s:%s", message, String.join(",", errors)));
    }

    /**
     * 处理 SpringMVC 参数绑定不正确，本质上也是通过 Validator 校验
     */
    @ExceptionHandler(BindException.class)
    public Result<?> bindExceptionHandler(HttpServletRequest request, BindException e) {
        var errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        var message = i18n.getMessage(BAD_REQUEST.msg());
        return Result.getError(BAD_REQUEST.code(), String.format("%s:%s", message, String.join(",", errors)));
    }

    /**
     * 处理 Validator 校验不通过产生的异常
     */
    @ExceptionHandler(value = ConstraintViolationException.class)
    public Result<?> constraintViolationExceptionHandler(HttpServletRequest request, ConstraintViolationException ex) {
        ConstraintViolation<?> constraintViolation = ex.getConstraintViolations().iterator().next();
        var message = i18n.getMessage(BAD_REQUEST.msg());
        return Result.getError(BAD_REQUEST.code(), String.format("%s:%s", message, constraintViolation.getMessage()));
    }

    /**
     * 处理 Dubbo Consumer 本地参数校验时，抛出的 ValidationException 异常
     */
    @ExceptionHandler(value = ValidationException.class)
    public Result<?> validationException(HttpServletRequest request, ValidationException ex) {
        // 无法拼接明细的错误信息，因为 Dubbo Consumer 抛出 ValidationException 异常时，是直接的字符串信息，且人类不可读
        return Result.getError(BAD_REQUEST);
    }

    /**
     * 处理 SpringMVC 请求地址不存在
     * <p>
     * 注意，它需要设置如下两个配置项：
     * 1. spring.mvc.throw-exception-if-no-handler-found 为 true
     * 2. spring.mvc.static-path-pattern 为 /statics/**
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<?> noHandlerFoundExceptionHandler(HttpServletRequest request, NoHandlerFoundException ex) {
        var message = i18n.getMessage(NOT_FOUND.msg());
        return Result.getError(NOT_FOUND.code(), String.format("%s:%s", message, ex.getRequestURL()));
    }

    /**
     * 处理 SpringMVC 请求方法不正确
     * <p>
     * 例如说，A 接口的方法为 GET 方式，结果请求方法为 POST 方式，导致不匹配
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<?> httpRequestMethodNotSupportedExceptionHandler(HttpServletRequest request, HttpRequestMethodNotSupportedException ex) {
        var message = i18n.getMessage(METHOD_NOT_ALLOWED.msg());
        return Result.getError(METHOD_NOT_ALLOWED.code(), String.format("%s:%s", message, ex.getMethod()));
    }

    /**
     * 处理请求超时异常 SocketRuntimeException
     * <p>
     * 例如说，请求连接超时、响应数据读取超时。
     */
    @ExceptionHandler(value = SocketRuntimeException.class)
    public Result<?> socketRuntimeExceptionHandler(HttpServletRequest request, SocketRuntimeException ex) {
        var message = i18n.getMessage(SOCKET_TIME_OUT.msg());
        return Result.getError(SOCKET_TIME_OUT.code(), message);
    }

    /**
     * 处理业务异常 ServiceException
     * <p>
     * 例如说，商品库存不足，用户手机号已存在。
     */
    @ExceptionHandler(value = ServiceException.class)
    public Result<?> serviceExceptionHandler(HttpServletRequest request, ServiceException ex) {
        var message = i18n.getMessage(ex.getMessage());
        return Result.getError(ex.getCode(), message);
    }

    /**
     * 处理系统异常，兜底处理所有的一切
     */
    @ExceptionHandler(value = Exception.class)
    public Result<?> defaultExceptionHandler(HttpServletRequest request, Throwable ex) {
        createExceptionLog(request, ex);
        // 返回 ERROR CommonResult
        log.error(ex.getMessage(), ex);
        var message = i18n.getMessage(INTERNAL_SERVER_ERROR.msg());
        return Result.getError(INTERNAL_SERVER_ERROR.code(), message);
    }

    private void createExceptionLog(HttpServletRequest request, Throwable e) {
        // 插入错误日志
        ApiErrorLog errorLog = new ApiErrorLog();
        try {
            // 初始化 errorLog
            initExceptionLog(errorLog, request, e);
            // 执行插入 errorLog
            apiErrorLogFrameworkService.createApiErrorLog(errorLog);
        } catch (Throwable th) {
            log.error("[createExceptionLog][url({}) log({}) 发生异常]", request.getRequestURI(), JsonUtils.toJsonString(errorLog), th);
        }
    }

    private void initExceptionLog(ApiErrorLog errorLog, HttpServletRequest request, Throwable e) {
        // 设置异常字段
        errorLog.setExceptionName(e.getClass().getName());
        errorLog.setExceptionMessage(ExceptionUtil.getMessage(e));
        errorLog.setExceptionRootCauseMessage(ExceptionUtil.getRootCauseMessage(e));
        errorLog.setExceptionStackTrace(ExceptionUtil.stacktraceToString(e));
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        Assert.notEmpty(stackTraceElements, "异常 stackTraceElements 不能为空");
        StackTraceElement stackTraceElement = stackTraceElements[0];
        errorLog.setExceptionClassName(stackTraceElement.getClassName());
        errorLog.setExceptionFileName(stackTraceElement.getFileName());
        errorLog.setExceptionMethodName(stackTraceElement.getMethodName());
        errorLog.setExceptionLineNumber(stackTraceElement.getLineNumber());
        // 设置其它字段
        errorLog.setApplicationName(applicationName);
        errorLog.setRequestUrl(request.getRequestURI());
        Map<String, Object> requestParams = MapUtil.<String, Object>builder()
                .put("query", ServletUtils.getParamMap(request))
                .put("body", ServletUtils.getBody(request)).build();
        errorLog.setRequestParams(JsonUtils.toJsonString(requestParams));
        errorLog.setRequestMethod(request.getMethod());
        errorLog.setUserIp(ServletUtils.getClientIP(request));
        errorLog.setExceptionTime(new Date());
    }
}
