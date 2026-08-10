package xyz.migoo.framework.web.i18n;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link I18NLocaleResolver} 单元测试
 *
 * <p>验证从 Accept-Language 请求头解析 Locale，以及 setLocale 的空实现行为。</p>
 */
class I18NLocaleResolverTest {

    private final I18NLocaleResolver resolver = new I18NLocaleResolver();

    @Test
    void resolveLocaleReadsAcceptLanguageHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Accept-Language")).thenReturn("en-US");

        assertThat(resolver.resolveLocale(request)).isEqualTo(Locale.of("en-US"));
    }

    @Test
    void resolveLocaleForChineseHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Accept-Language")).thenReturn("zh-CN");

        assertThat(resolver.resolveLocale(request)).isEqualTo(Locale.of("zh-CN"));
    }

    @Test
    void resolveLocaleFallsBackToDefaultWhenHeaderMissing() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Accept-Language")).thenReturn(null);

        assertThat(resolver.resolveLocale(request)).isEqualTo(Locale.getDefault());
    }

    @Test
    void resolveLocaleFallsBackToDefaultWhenHeaderBlank() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Accept-Language")).thenReturn("  ");

        assertThat(resolver.resolveLocale(request)).isEqualTo(Locale.getDefault());
    }

    @Test
    void setLocaleIsNoOp() {
        // setLocale 当前为空实现，不应抛异常且不依赖 response
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        assertThatCode(() -> resolver.setLocale(request, response, Locale.JAPAN)).doesNotThrowAnyException();
    }
}
