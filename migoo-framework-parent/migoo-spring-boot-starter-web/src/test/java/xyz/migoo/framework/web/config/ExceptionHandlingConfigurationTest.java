package xyz.migoo.framework.web.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import xyz.migoo.framework.apilog.core.ApiErrorLogFrameworkService;
import xyz.migoo.framework.common.exception.ErrorCode;
import xyz.migoo.framework.common.exception.ServiceException;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.web.core.handler.GlobalExceptionHandler;
import xyz.migoo.framework.web.i18n.I18NMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link ExceptionHandlingConfiguration} 单元测试：@Bean 方法可直接脱离容器调用。
 */
class ExceptionHandlingConfigurationTest {

    @SuppressWarnings("unchecked")
    private static ObjectProvider<ApiErrorLogFrameworkService> provider(ApiErrorLogFrameworkService service) {
        ObjectProvider<ApiErrorLogFrameworkService> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(service);
        return provider;
    }

    @Test
    void globalExceptionHandlerIsWiredWithDependencies() {
        ApiErrorLogFrameworkService apiErrorLog = mock(ApiErrorLogFrameworkService.class);
        I18NMessage i18n = mock(I18NMessage.class);
        when(i18n.getMessage("库存不足")).thenReturn("库存不足");

        GlobalExceptionHandler handler =
                new ExceptionHandlingConfiguration().globalExceptionHandler(
                        "test-app", provider(apiErrorLog), i18n,
                        mock(org.springframework.context.ApplicationEventPublisher.class));

        assertThat(handler).isInstanceOf(GlobalExceptionHandler.class);
        // 验证依赖注入后可以正常工作
        Result<?> result = handler.serviceExceptionHandler(
                mock(jakarta.servlet.http.HttpServletRequest.class),
                new ServiceException(ErrorCode.of(1002000000, "库存不足")));
        assertThat(result.getCode()).isEqualTo(1002000000);
    }

    @Test
    void globalExceptionHandlerWorksWithoutApiErrorLogService() {
        // ApiErrorLogFrameworkService 为可选扩展点：应用不实现时上下文可正常启动、异常处理可用
        I18NMessage i18n = mock(I18NMessage.class);
        when(i18n.getMessage("库存不足")).thenReturn("库存不足");

        GlobalExceptionHandler handler =
                new ExceptionHandlingConfiguration().globalExceptionHandler(
                        "test-app", provider(null), i18n,
                        mock(org.springframework.context.ApplicationEventPublisher.class));

        Result<?> result = handler.serviceExceptionHandler(
                mock(jakarta.servlet.http.HttpServletRequest.class),
                new ServiceException(ErrorCode.of(1002000000, "库存不足")));
        assertThat(result.getCode()).isEqualTo(1002000000);
    }
}
