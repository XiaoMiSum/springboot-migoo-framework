package xyz.migoo.framework.web.core.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
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
import xyz.migoo.framework.common.observability.ServerErrorEvent;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.common.util.JsonUtils;
import xyz.migoo.framework.common.util.object.ExceptionUtils;
import xyz.migoo.framework.web.core.util.ServletUtils;
import xyz.migoo.framework.web.i18n.I18NMessage;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static xyz.migoo.framework.common.exception.GlobalErrorCodeConstants.*;

/**
 * 全局异常处理器，将 Exception 翻译成 CommonResult + 对应的异常编号
 *
 * @author xiaomi
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * spring-security 访问拒绝异常全限定名（web 组件不依赖 security，按名识别，见 {@link #isAccessDenied}）
     */
    private static final String ACCESS_DENIED_TYPE = "org.springframework.security.access.AccessDeniedException";

    private final String applicationName;

    private final ApiErrorLogFrameworkService apiErrorLogFrameworkService;

    private final I18NMessage i18n;

    /**
     * 事件发布器（可观测性信号，可空：直连构造时允许不发布）
     */
    private final ApplicationEventPublisher eventPublisher;

    public GlobalExceptionHandler(String applicationName, ApiErrorLogFrameworkService apiErrorLogFrameworkService,
                                  I18NMessage i18n) {
        this(applicationName, apiErrorLogFrameworkService, i18n, null);
    }

    public GlobalExceptionHandler(String applicationName, ApiErrorLogFrameworkService apiErrorLogFrameworkService,
                                  I18NMessage i18n, ApplicationEventPublisher eventPublisher) {
        this.applicationName = applicationName;
        this.apiErrorLogFrameworkService = apiErrorLogFrameworkService;
        this.i18n = i18n;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 处理所有异常，主要是提供给 Filter 使用
     * 因为 Filter 不走 SpringMVC 的流程，但是我们又需要兜底处理异常，所以这里提供一个全量的异常处理过程，保持逻辑统一。
     *
     * @param request 请求
     * @param t       异常
     * @return 通用返回
     */
    public Result<?> allExceptionHandler(HttpServletRequest request, HttpServletResponse response, Throwable t) {
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
            case IOException e -> socketRuntimeExceptionHandler(request, response, e);
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
        return Result.error(BAD_REQUEST.code(), String.format("%s:%s", message, t.getContentType()));
    }

    /**
     * 处理 SpringMVC 请求参数缺失
     * <p>
     * 例如说，接口上设置了 @RequestParam("xx") 参数，结果并未传递 xx 参数
     */
    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public Result<?> missingServletRequestParameterHandler(HttpServletRequest request, MissingServletRequestParameterException ex) {
        var message = i18n.getMessage(BAD_REQUEST.msg());
        return Result.error(BAD_REQUEST.code(), String.format("%s:%s", message, ex.getParameterName()));
    }

    /**
     * 处理 SpringMVC 请求参数类型错误
     * <p>
     * 例如说，接口上设置了 @RequestParam("xx") 参数为 Integer，结果传递 xx 参数类型为 String
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<?> methodArgumentTypeMismatchExceptionHandler(HttpServletRequest request, MethodArgumentTypeMismatchException ex) {
        var message = i18n.getMessage(BAD_REQUEST.msg());
        return Result.error(BAD_REQUEST.code(), String.format("%s:%s", message, ex.getMessage()));
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
        return Result.error(BAD_REQUEST.code(), String.format("%s:%s", message, String.join(",", errors)));
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
        return Result.error(BAD_REQUEST.code(), String.format("%s:%s", message, String.join(",", errors)));
    }

    /**
     * 处理 Validator 校验不通过产生的异常
     */
    @ExceptionHandler(value = ConstraintViolationException.class)
    public Result<?> constraintViolationExceptionHandler(HttpServletRequest request, ConstraintViolationException ex) {
        ConstraintViolation<?> constraintViolation = ex.getConstraintViolations().iterator().next();
        var message = i18n.getMessage(BAD_REQUEST.msg());
        return Result.error(BAD_REQUEST.code(), String.format("%s:%s", message, constraintViolation.getMessage()));
    }

    /**
     * 处理 Dubbo Consumer 本地参数校验时，抛出的 ValidationException 异常
     */
    @ExceptionHandler(value = ValidationException.class)
    public Result<?> validationException(HttpServletRequest request, ValidationException ex) {
        // 无法拼接明细的错误信息，因为 Dubbo Consumer 抛出 ValidationException 异常时，是直接的字符串信息，且人类不可读
        return Result.error(BAD_REQUEST);
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
        return Result.error(NOT_FOUND.code(), String.format("%s:%s", message, ex.getRequestURL()));
    }

    /**
     * 处理 SpringMVC 请求方法不正确
     * <p>
     * 例如说，A 接口的方法为 GET 方式，结果请求方法为 POST 方式，导致不匹配
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<?> httpRequestMethodNotSupportedExceptionHandler(HttpServletRequest request, HttpRequestMethodNotSupportedException ex) {
        var message = i18n.getMessage(METHOD_NOT_ALLOWED.msg());
        return Result.error(METHOD_NOT_ALLOWED.code(), String.format("%s:%s", message, ex.getMethod()));
    }

    /**
     * 处理请求超时异常 SocketRuntimeException
     * <p>
     * 例如说，请求连接超时、响应数据读取超时。
     * <p>
     * 注意：SSE（text/event-stream）等异步流式响应已提交后，无法再写 JSON 结果——
     * 此时返回 {@code null} 让 SpringMVC 结束处理，避免 {@code HttpMessageNotWritableException}
     * （无 converter 能把 Result 写成 text/event-stream）掩盖真实异常。
     */
    @ExceptionHandler(value = IOException.class)
    public Result<?> socketRuntimeExceptionHandler(HttpServletRequest request, HttpServletResponse response, IOException ex) {
        // SSE 等流式响应已提交（响应头已下发）→ 不能再写 JSON，直接结束请求
        if (response.isCommitted()) {
            return null;
        }
        var message = i18n.getMessage(SOCKET_TIME_OUT.msg());
        return Result.error(SOCKET_TIME_OUT.code(), message);
    }

    /**
     * 处理业务异常 ServiceException
     * <p>
     * 例如说，商品库存不足，用户手机号已存在。
     */
    @ExceptionHandler(value = ServiceException.class)
    public Result<?> serviceExceptionHandler(HttpServletRequest request, ServiceException ex) {
        var message = i18n.getMessage(ex.getMessage());
        return Result.error(ex.getCode(), message);
    }

    /**
     * 处理系统异常，兜底处理所有的一切
     */
    @ExceptionHandler(value = Exception.class)
    public Result<?> defaultExceptionHandler(HttpServletRequest request, Throwable ex) {
        // 访问拒绝（方法级 @PreAuthorize/@Secured 越权等）→ 403，属预期的权限失败而非服务端错误：
        // 不落 API 错误日志、不发布 SERVER_ERROR 事件，与过滤器层 AccessDeniedHandler 保持同一语义
        if (isAccessDenied(ex)) {
            log.warn("[defaultExceptionHandler][访问({}) 权限不足]", ServletUtils.getRoutePattern(request), ex);
            return Result.error(FORBIDDEN.code(), i18n.getMessage(FORBIDDEN.msg()));
        }
        createExceptionLog(request, ex);
        publishServerError(request, ex);
        // 返回 ERROR CommonResult
        log.error(ex.getMessage(), ex);
        var message = i18n.getMessage(INTERNAL_SERVER_ERROR.msg());
        return Result.error(INTERNAL_SERVER_ERROR.code(), message);
    }

    /**
     * 判定是否为「访问拒绝」类异常
     * <p>
     * web 组件不依赖 spring-security，无法直接 instanceof，故沿 cause 链与类层次按全限定名识别
     * {@code org.springframework.security.access.AccessDeniedException}（及其子类
     * {@code AuthorizationDeniedException}），使方法级权限校验失败返回 403 而非被兜底成 500。
     * 类层次经 {@link Class#getSuperclass()} 逐级取名，类缺失时不触发加载，web-only 应用不受影响。
     */
    private static boolean isAccessDenied(Throwable ex) {
        Throwable current = ex;
        // depth 上限防御 cause 环，正常异常链远短于此
        for (int depth = 0; current != null && depth < 10; depth++) {
            for (Class<?> type = current.getClass(); type != null; type = type.getSuperclass()) {
                if (ACCESS_DENIED_TYPE.equals(type.getName())) {
                    return true;
                }
            }
            current = current.getCause() == current ? null : current.getCause();
        }
        return false;
    }

    /**
     * 发布服务端错误事件（可观测性信号，观测不得影响业务：发布异常只记日志）
     */
    private void publishServerError(HttpServletRequest request, Throwable ex) {
        if (eventPublisher == null) {
            return;
        }
        try {
            eventPublisher.publishEvent(new ServerErrorEvent(
                    ServletUtils.getRoutePattern(request),
                    request != null ? request.getMethod() : null,
                    ex.getClass().getSimpleName(),
                    StringUtils.truncate(ExceptionUtils.getMessage(ex), 200)));
        } catch (Exception ex2) {
            log.warn("[publishServerError][发布服务端错误事件失败]", ex2);
        }
    }

    private void createExceptionLog(HttpServletRequest request, Throwable e) {
        // 未接入 ApiErrorLogFrameworkService（可选扩展点）时跳过落库
        if (apiErrorLogFrameworkService == null) {
            return;
        }
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
        errorLog.setExceptionMessage(ExceptionUtils.getMessage(e));
        errorLog.setExceptionRootCauseMessage(ExceptionUtils.getRootCauseMessage(e));
        errorLog.setExceptionStackTrace(ExceptionUtils.stacktraceToString(e));
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
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("query", ServletUtils.getParamMap(request));
        requestParams.put("body", ServletUtils.getBody(request));
        errorLog.setRequestParams(JsonUtils.toJsonString(requestParams));
        errorLog.setRequestMethod(request.getMethod());
        errorLog.setUserIp(ServletUtils.getClientIP(request));
        errorLog.setExceptionTime(new Date());
    }
}
