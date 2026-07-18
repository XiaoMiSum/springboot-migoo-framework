package xyz.migoo.framework.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.migoo.framework.web.core.handler.ResponseBodyI18nAdvice;
import xyz.migoo.framework.web.core.handler.ResponseBodyStorageAdvice;
import xyz.migoo.framework.web.i18n.I18NMessage;

@Configuration
public class ResponseBodyConfiguration {

    @Bean
    public ResponseBodyStorageAdvice globalResponseBodyStorageAdvice() {
        return new ResponseBodyStorageAdvice();
    }

    @Bean
    public ResponseBodyI18nAdvice globalResponseBodyI18nAdvice(I18NMessage i18n) {
        return new ResponseBodyI18nAdvice(i18n);
    }
}
