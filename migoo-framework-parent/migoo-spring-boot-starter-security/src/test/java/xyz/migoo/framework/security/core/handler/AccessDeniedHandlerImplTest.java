package xyz.migoo.framework.security.core.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import xyz.migoo.framework.common.exception.GlobalErrorCodeConstants;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.common.util.JsonUtils;
import xyz.migoo.framework.web.i18n.I18NMessage;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link AccessDeniedHandlerImpl} 单元测试
 */
class AccessDeniedHandlerImplTest {

    private AccessDeniedHandlerImpl handler;

    @BeforeEach
    void setUp() {
        I18NMessage i18n = mock(I18NMessage.class);
        when(i18n.getMessage(GlobalErrorCodeConstants.FORBIDDEN.msg())).thenReturn("没有权限");
        handler = new AccessDeniedHandlerImpl(i18n);
    }

    @Test
    void handleWritesForbiddenJson() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("denied"));

        // 内容类型为 JSON
        assertThat(response.getContentType()).isEqualTo("application/json;charset=utf-8");
        // 响应体为 Result.error(403, 没有权限)
        Result<?> result = JsonUtils.parseObject(response.getContentAsString(), Result.class);
        assertThat(result.getCode()).isEqualTo(403);
        assertThat(result.getMsg()).isEqualTo("没有权限");
        assertThat(result.getData()).isNull();
    }

    @Test
    void handleUsesI18nMessageKey() throws Exception {
        // 消息源无法解析（抛 NoSuchMessageException）时，I18NMessage 回退返回 message key
        MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenThrow(new NoSuchMessageException("missing"));
        AccessDeniedHandlerImpl fallbackHandler = new AccessDeniedHandlerImpl(new I18NMessage(messageSource));
        MockHttpServletResponse response = new MockHttpServletResponse();

        fallbackHandler.handle(new MockHttpServletRequest(), response, new AccessDeniedException("denied"));

        Result<?> result = JsonUtils.parseObject(response.getContentAsString(), Result.class);
        assertThat(result.getCode()).isEqualTo(GlobalErrorCodeConstants.FORBIDDEN.code());
        assertThat(result.getMsg()).isEqualTo(GlobalErrorCodeConstants.FORBIDDEN.msg());
    }
}
