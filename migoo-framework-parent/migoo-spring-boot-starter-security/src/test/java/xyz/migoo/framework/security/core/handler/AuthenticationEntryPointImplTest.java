package xyz.migoo.framework.security.core.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import xyz.migoo.framework.common.exception.GlobalErrorCodeConstants;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.common.util.JsonUtils;
import xyz.migoo.framework.web.i18n.I18NMessage;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link AuthenticationEntryPointImpl} 单元测试
 */
class AuthenticationEntryPointImplTest {

    private AuthenticationEntryPointImpl entryPoint;

    @BeforeEach
    void setUp() {
        I18NMessage i18n = mock(I18NMessage.class);
        when(i18n.getMessage(GlobalErrorCodeConstants.UNAUTHORIZED.msg())).thenReturn("未登录");
        entryPoint = new AuthenticationEntryPointImpl(i18n);
    }

    @Test
    void commenceWritesUnauthorizedJson() throws UnsupportedEncodingException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/user");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("bad credentials"));

        assertThat(response.getContentType()).isEqualTo("application/json;charset=utf-8");
        Result<?> result = JsonUtils.parseObject(response.getContentAsString(), Result.class);
        assertThat(result.getCode()).isEqualTo(401);
        assertThat(result.getMsg()).isEqualTo("未登录");
        assertThat(result.getData()).isNull();
    }

    @Test
    void commenceUsesI18nMessageKeyWhenUnresolved() throws UnsupportedEncodingException {
        // 消息源无法解析（抛 NoSuchMessageException）时，I18NMessage 回退返回 message key
        MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenThrow(new NoSuchMessageException("missing"));
        AuthenticationEntryPointImpl fallbackEntryPoint = new AuthenticationEntryPointImpl(new I18NMessage(messageSource));
        MockHttpServletResponse response = new MockHttpServletResponse();

        fallbackEntryPoint.commence(new MockHttpServletRequest(), response, new BadCredentialsException("x"));

        Result<?> result = JsonUtils.parseObject(response.getContentAsString(), Result.class);
        assertThat(result.getCode()).isEqualTo(GlobalErrorCodeConstants.UNAUTHORIZED.code());
        assertThat(result.getMsg()).isEqualTo(GlobalErrorCodeConstants.UNAUTHORIZED.msg());
    }
}
