package xyz.migoo.framework.web.core.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import xyz.migoo.framework.apilog.core.ApiErrorLog;
import xyz.migoo.framework.apilog.core.ApiErrorLogFrameworkService;
import xyz.migoo.framework.common.exception.ErrorCode;
import xyz.migoo.framework.common.exception.ServiceException;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.web.i18n.I18NMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * {@link GlobalExceptionHandler} 单元测试
 *
 * <p>通过 Mockito 构造 I18NMessage 与 ApiErrorLogFrameworkService，逐一验证各异常处理方法的
 * Result code / message 映射。</p>
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private static final String APPLICATION_NAME = "test-app";

    @Mock
    private ApiErrorLogFrameworkService apiErrorLogService;

    @Mock
    private I18NMessage i18n;

    private GlobalExceptionHandler handler;

    /**
     * 获取无参 request mock（供不需要特殊 stub 的 handler 使用）
     */
    private static HttpServletRequest request() {
        // getHeader 等方法默认返回 null，无需显式 stub
        return mock(HttpServletRequest.class);
    }

    private static MethodParameter parameterOf(String methodName) throws NoSuchMethodException {
        Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod(methodName, String.class);
        return new MethodParameter(method, 0);
    }

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler(APPLICATION_NAME, apiErrorLogService, i18n);
        // lenient：这些 stub 并非每个测试都会用到
        lenient().when(i18n.getMessage("common.request.bad")).thenReturn("请求参数错误");
        lenient().when(i18n.getMessage("common.not.found")).thenReturn("资源不存在");
        lenient().when(i18n.getMessage("common.request.method.bad")).thenReturn("请求方法错误");
        lenient().when(i18n.getMessage("common.request.timeout")).thenReturn("请求超时");
        lenient().when(i18n.getMessage("common.system.error")).thenReturn("系统异常");
    }

    @Test
    void httpMediaTypeNotSupportedReturnsBadRequestWithContentType() {
        HttpMediaTypeNotSupportedException ex =
                new HttpMediaTypeNotSupportedException(MediaType.APPLICATION_JSON, List.of());
        Result<?> result = handler.httpMediaTypeNotSupportedException(request(), ex);
        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMsg()).isEqualTo("请求参数错误:application/json");
    }

    @Test
    void missingServletRequestParameterReturnsBadRequestWithParamName() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("username", "String");
        Result<?> result = handler.missingServletRequestParameterHandler(request(), ex);
        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMsg()).isEqualTo("请求参数错误:username");
    }

    @Test
    void methodArgumentTypeMismatchReturnsBadRequestWithDetail() {
        MethodArgumentTypeMismatchException ex =
                new MethodArgumentTypeMismatchException("abc", Integer.class, "age", null, null);
        Result<?> result = handler.methodArgumentTypeMismatchExceptionHandler(request(), ex);
        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMsg()).startsWith("请求参数错误:");
        assertThat(result.getMsg()).contains("age");
    }

    @Test
    void methodArgumentNotValidReturnsJoinedFieldMessages() throws Exception {
        BindException bindException = new BindException(new Object(), "target");
        bindException.addError(new FieldError("target", "username", "用户名不能为空"));
        bindException.addError(new FieldError("target", "age", "年龄必须大于0"));
        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(parameterOf("sampleEndpoint"), bindException.getBindingResult());

        Result<?> result = handler.methodArgumentNotValidExceptionExceptionHandler(request(), ex);
        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMsg()).isEqualTo("请求参数错误:用户名不能为空,年龄必须大于0");
    }

    @Test
    void bindExceptionReturnsJoinedFieldMessages() {
        BindException ex = new BindException(new Object(), "target");
        ex.addError(new FieldError("target", "name", "姓名不能为空"));
        ex.addError(new FieldError("target", "age", "年龄必须大于0"));

        Result<?> result = handler.bindExceptionHandler(request(), ex);
        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMsg()).isEqualTo("请求参数错误:姓名不能为空,年龄必须大于0");
    }

    @Test
    void constraintViolationReturnsFirstViolationMessage() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("不能为空");
        ConstraintViolationException ex =
                new ConstraintViolationException("验证失败", Set.of(violation));

        Result<?> result = handler.constraintViolationExceptionHandler(request(), ex);
        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMsg()).isEqualTo("请求参数错误:不能为空");
    }

    @Test
    void validationExceptionReturnsRawBadRequestErrorCode() {
        ValidationException ex = new ValidationException("validation failed");
        Result<?> result = handler.validationException(request(), ex);
        assertThat(result.getCode()).isEqualTo(400);
        // 直接使用 BAD_REQUEST 的 msg 原始 key
        assertThat(result.getMsg()).isEqualTo("common.request.bad");
    }

    @Test
    void noHandlerFoundReturnsNotFoundWithUrl() {
        NoHandlerFoundException ex = new NoHandlerFoundException("GET", "/api/user", new HttpHeaders());
        Result<?> result = handler.noHandlerFoundExceptionHandler(request(), ex);
        assertThat(result.getCode()).isEqualTo(404);
        assertThat(result.getMsg()).isEqualTo("资源不存在:/api/user");
    }

    @Test
    void httpRequestMethodNotSupportedReturnsMethodNotAllowed() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("POST");
        Result<?> result = handler.httpRequestMethodNotSupportedExceptionHandler(request(), ex);
        assertThat(result.getCode()).isEqualTo(405);
        assertThat(result.getMsg()).isEqualTo("请求方法错误:POST");
    }

    @Test
    void ioExceptionReturnsSocketTimeOut() {
        IOException ex = new IOException("socket timeout");
        Result<?> result = handler.socketRuntimeExceptionHandler(request(), mock(HttpServletResponse.class), ex);
        assertThat(result.getCode()).isEqualTo(450);
        assertThat(result.getMsg()).isEqualTo("请求超时");
    }

    @Test
    void serviceExceptionUsesErrorCodeAndI18nMessage() {
        ServiceException ex = new ServiceException(ErrorCode.of(1001000000, "用户不存在"));
        lenient().when(i18n.getMessage("用户不存在")).thenReturn("用户不存在");

        Result<?> result = handler.serviceExceptionHandler(request(), ex);
        assertThat(result.getCode()).isEqualTo(1001000000);
        assertThat(result.getMsg()).isEqualTo("用户不存在");
        verify(i18n).getMessage("用户不存在");
    }

    @Test
    void defaultExceptionReturnsInternalServerErrorAndLogs() throws Exception {
        HttpServletRequest request = request();
        when(request.getParameterMap()).thenReturn(new HashMap<>());
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("")));
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        RuntimeException ex = new RuntimeException("boom");
        Result<?> result = handler.defaultExceptionHandler(request, ex);

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMsg()).isEqualTo("系统异常");

        ArgumentCaptor<ApiErrorLog> captor = ArgumentCaptor.forClass(ApiErrorLog.class);
        verify(apiErrorLogService).createApiErrorLog(captor.capture());
        ApiErrorLog log = captor.getValue();
        assertThat(log.getApplicationName()).isEqualTo(APPLICATION_NAME);
        assertThat(log.getRequestUrl()).isEqualTo("/api/test");
        assertThat(log.getRequestMethod()).isEqualTo("GET");
        assertThat(log.getExceptionName()).isEqualTo(RuntimeException.class.getName());
        assertThat(log.getExceptionMessage()).isEqualTo("RuntimeException: boom");
        assertThat(log.getExceptionTime()).isNotNull();
    }

    @Test
    void allExceptionHandlerDispatchesToMatchingHandler() throws Exception {
        // ServiceException 走 serviceExceptionHandler
        ServiceException serviceEx = new ServiceException(ErrorCode.of(1002000000, "库存不足"));
        lenient().when(i18n.getMessage("库存不足")).thenReturn("库存不足");
        Result<?> result = handler.allExceptionHandler(request(), mock(HttpServletResponse.class), serviceEx);
        assertThat(result.getCode()).isEqualTo(1002000000);

        // 未知异常走 defaultExceptionHandler
        HttpServletRequest request = request();
        when(request.getParameterMap()).thenReturn(new HashMap<>());
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("")));
        when(request.getRequestURI()).thenReturn("/api/boom");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");

        Result<?> fallback = handler.allExceptionHandler(request, mock(HttpServletResponse.class),
                new IllegalStateException("unexpected"));
        assertThat(fallback.getCode()).isEqualTo(500);
    }

    /**
     * 供反射构造 MethodParameter 的示例方法
     */
    @SuppressWarnings("unused")
    private void sampleEndpoint(String body) {
    }
}
