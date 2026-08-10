package xyz.migoo.framework.web.i18n;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link I18NMessage} 单元测试
 *
 * <p>验证对 {@link MessageSource#getMessage(String, Object[], Locale)} 的委托、动态参数传递与
 * 默认消息兜底。</p>
 */
@ExtendWith(MockitoExtension.class)
class I18NMessageTest {

    @Mock
    private MessageSource messageSource;

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void getMessageWithLocaleDelegatesToMessageSource() {
        when(messageSource.getMessage(eq("user.not.found"), any(), eq(Locale.US))).thenReturn("User not found");

        String message = new I18NMessage(messageSource).getMessage("user.not.found", Locale.US);

        assertThat(message).isEqualTo("User not found");
        verify(messageSource).getMessage(eq("user.not.found"), any(), eq(Locale.US));
    }

    @Test
    void getMessageWithDynamicValuesPassesArgsToMessageSource() {
        when(messageSource.getMessage("hello", new String[]{"world"}, Locale.CHINA))
                .thenReturn("你好, world");

        String message = new I18NMessage(messageSource).getMessage("hello", Locale.CHINA, "world");

        assertThat(message).isEqualTo("你好, world");
        ArgumentCaptor<Object[]> captor = ArgumentCaptor.forClass(Object[].class);
        verify(messageSource).getMessage(eq("hello"), captor.capture(), eq(Locale.CHINA));
        assertThat(captor.getValue()).containsExactly("world");
    }

    @Test
    void getMessageWithoutLocaleUsesLocaleContextHolder() {
        LocaleContextHolder.setLocale(Locale.FRANCE);
        when(messageSource.getMessage(eq("greeting"), any(), eq(Locale.FRANCE))).thenReturn("Bonjour");

        String message = new I18NMessage(messageSource).getMessage("greeting");

        assertThat(message).isEqualTo("Bonjour");
        verify(messageSource).getMessage(eq("greeting"), any(), eq(Locale.FRANCE));
    }

    @Test
    void getMessageFallsBackToKeyWhenMessageSourceThrows() {
        when(messageSource.getMessage("missing.key", null, Locale.getDefault()))
                .thenThrow(new NoSuchMessageException("missing.key"));

        // MessageSource 抛异常时兜底返回原始 key
        String message = new I18NMessage(messageSource).getMessage("missing.key");

        assertThat(message).isEqualTo("missing.key");
    }

    @Test
    void getMessageFallsBackToKeyOnAnyException() {
        when(messageSource.getMessage("boom.key", null, Locale.getDefault()))
                .thenThrow(new IllegalStateException("boom"));

        assertThat(new I18NMessage(messageSource).getMessage("boom.key")).isEqualTo("boom.key");
    }
}
