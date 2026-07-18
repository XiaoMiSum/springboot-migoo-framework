package xyz.migoo.framework.web.i18n;

import jakarta.annotation.Resource;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

public class I18NMessage {

    private final MessageSource messageSource;

    public I18NMessage(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String messageKey, String... dynamicValues) {
        return getMessage(messageKey, LocaleContextHolder.getLocale(), dynamicValues);
    }

    public String getMessage(String messageKey, Locale locale, String... dynamicValues) {
        try {
            return messageSource.getMessage(messageKey, dynamicValues, locale);
        } catch (Exception e) {
            return messageKey;
        }
    }
}
