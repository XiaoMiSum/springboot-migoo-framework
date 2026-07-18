package xyz.migoo.framework.web.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import xyz.migoo.framework.web.i18n.I18NLocaleResolver;
import xyz.migoo.framework.web.i18n.I18NMessage;

@Configuration
public class I18nConfiguration {

    @Bean
    public LocaleResolver i18nLocaleResolver() {
        return new I18NLocaleResolver();
    }

    @Bean
    public I18NMessage i18nMessage(MessageSource messageSource) {
        return new I18NMessage(messageSource);
    }
}
