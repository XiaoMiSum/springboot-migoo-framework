package xyz.migoo.framework.web.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.LocaleResolver;
import xyz.migoo.framework.web.i18n.I18NLocaleResolver;
import xyz.migoo.framework.web.i18n.I18NMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link I18nConfiguration} 单元测试：@Bean 方法可直接脱离容器调用，使用 Mockito 构造 MessageSource。
 */
class I18nConfigurationTest {

    @Test
    void i18nLocaleResolverReturnsI18NLocaleResolver() {
        LocaleResolver resolver = new I18nConfiguration().i18nLocaleResolver();
        assertThat(resolver).isInstanceOf(I18NLocaleResolver.class);
    }

    @Test
    void i18nMessageWrapsMessageSource() {
        org.springframework.context.MessageSource messageSource = mock(org.springframework.context.MessageSource.class);
        when(messageSource.getMessage(org.mockito.ArgumentMatchers.eq("key"),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(java.util.Locale.US))).thenReturn("translated");

        I18NMessage message = new I18nConfiguration().i18nMessage(messageSource);

        assertThat(message).isNotNull();
        assertThat(message.getMessage("key", java.util.Locale.US)).isEqualTo("translated");
    }
}
