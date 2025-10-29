package xyz.migoo.framework.web.i18n;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

public class I18NLocaleResolver implements LocaleResolver {

    @Override
    public @NonNull Locale resolveLocale(HttpServletRequest httpServletRequest) {
        //获取请求中的语言参数
        var language = httpServletRequest.getHeader("Accept-Language");
        return StrUtil.isNotBlank(language) ? Locale.of(language) : Locale.getDefault();
    }

    @Override
    public void setLocale(@NonNull HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Locale locale) {

    }
}