package xyz.migoo.framework.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.migoo.framework.apilog.core.ApiErrorLogFrameworkService;
import xyz.migoo.framework.web.core.handler.GlobalExceptionHandler;
import xyz.migoo.framework.web.i18n.I18NMessage;

@Configuration
public class ExceptionHandlingConfiguration {

    @Bean
    public GlobalExceptionHandler globalExceptionHandler(
            @Value("${spring.application.name}") String applicationName,
            ApiErrorLogFrameworkService apiErrorLog,
            I18NMessage i18n) {
        return new GlobalExceptionHandler(applicationName, apiErrorLog, i18n);
    }
}
