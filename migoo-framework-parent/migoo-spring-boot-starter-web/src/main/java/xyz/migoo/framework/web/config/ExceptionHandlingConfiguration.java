package xyz.migoo.framework.web.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.migoo.framework.apilog.core.ApiErrorLogFrameworkService;
import xyz.migoo.framework.web.core.handler.GlobalExceptionHandler;
import xyz.migoo.framework.web.i18n.I18NMessage;

@Configuration
public class ExceptionHandlingConfiguration {

    @Bean
    public GlobalExceptionHandler globalExceptionHandler(
            // 未配置 spring.application.name 时降级为 unknown，不阻断启动
            @Value("${spring.application.name:unknown}") String applicationName,
            // 可选扩展点：应用未实现 ApiErrorLogFrameworkService 时错误日志仅输出（文档约定的"可选"语义）
            ObjectProvider<ApiErrorLogFrameworkService> apiErrorLog,
            I18NMessage i18n,
            ApplicationEventPublisher eventPublisher) {
        return new GlobalExceptionHandler(applicationName, apiErrorLog.getIfAvailable(), i18n, eventPublisher);
    }
}
