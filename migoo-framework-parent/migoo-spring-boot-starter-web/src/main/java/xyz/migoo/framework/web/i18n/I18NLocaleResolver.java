package xyz.migoo.framework.web.i18n;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

public class I18NLocaleResolver implements LocaleResolver {

    @Override
    public @NullMarked Locale resolveLocale(HttpServletRequest request) {
        //获取请求中的语言参数
        var language = request.getHeader("Accept-Language");
        return StringUtils.hasText(language) ? Locale.of(language) : Locale.getDefault();
    }

    @Override
    public void setLocale(@NonNull HttpServletRequest request, HttpServletResponse response, Locale locale) {

    }
}