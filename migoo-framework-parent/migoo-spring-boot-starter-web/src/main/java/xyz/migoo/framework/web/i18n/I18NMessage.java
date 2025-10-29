package xyz.migoo.framework.web.i18n;

import jakarta.annotation.Resource;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class I18NMessage {

    @Resource
    private MessageSource messageSource;

    public String getMessage(String messageKey, String... dynamicValues) {
        return messageSource.getMessage(messageKey, dynamicValues, LocaleContextHolder.getLocale());
    }

    public String getMessage(String messageKey, Locale locale, String... dynamicValues) {
        return messageSource.getMessage(messageKey, dynamicValues, locale);
    }
}