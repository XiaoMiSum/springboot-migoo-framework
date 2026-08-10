package xyz.migoo.framework.websocket.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import xyz.migoo.framework.security.core.AuthUserDetails;
import xyz.migoo.framework.security.core.authentication.AuthUserDetailsFetcher;
import xyz.migoo.framework.websocket.config.WebSocketProperties;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link WebSocketAuthInterceptor} 单元测试
 */
@ExtendWith(MockitoExtension.class)
class WebSocketAuthInterceptorTest {

    /** 测试用 AuthUserDetails 子类 */
    static class TestUser extends AuthUserDetails<TestUser, Long> {
    }

    private WebSocketProperties properties;
    @Mock
    private AuthUserDetailsFetcher<TestUser> userDetailsFetcher;
    private WebSocketAuthInterceptor interceptor;
    private ServerHttpResponse response;
    private WebSocketHandler wsHandler;
    private Map<String, Object> attributes;

    @BeforeEach
    void setUp() {
        properties = new WebSocketProperties();
        interceptor = new WebSocketAuthInterceptor(properties, userDetailsFetcher);
        response = mock(ServerHttpResponse.class);
        wsHandler = mock(WebSocketHandler.class);
        attributes = new HashMap<>();
    }

    /** 创建带指定 Authorization header 的请求（null 表示无 header） */
    private ServerHttpRequest requestWithAuth(String headerValue) {
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        if (headerValue != null) {
            headers.add("Authorization", headerValue);
        }
        when(request.getHeaders()).thenReturn(headers);
        return request;
    }

    @Test
    void beforeHandshakeAcceptsValidTokenAndStoresUserAttributes() {
        TestUser user = new TestUser().setId(100L).setName("张三");
        when(userDetailsFetcher.verifyToken("abc123")).thenReturn(user);

        boolean result = interceptor.beforeHandshake(requestWithAuth("Bearer abc123"), response, wsHandler, attributes);

        assertThat(result).isTrue();
        assertThat(attributes).containsEntry("USER_DETAILS", user);
        assertThat(attributes).containsEntry("USER_ID", "100");
        assertThat(attributes).containsEntry("USER_NAME", "张三");
        assertThat(attributes).containsEntry("TOKEN", "abc123");
    }

    @Test
    void beforeHandshakeAcceptsTokenWithoutPrefix() {
        TestUser user = new TestUser().setId(1L).setName("李四");
        when(userDetailsFetcher.verifyToken("raw-token")).thenReturn(user);

        boolean result = interceptor.beforeHandshake(requestWithAuth("raw-token"), response, wsHandler, attributes);

        // 无前缀的 token 原样使用
        assertThat(result).isTrue();
        assertThat(attributes).containsEntry("TOKEN", "raw-token");
        assertThat(attributes).containsEntry("USER_ID", "1");
        assertThat(attributes).containsEntry("USER_NAME", "李四");
    }

    @Test
    void beforeHandshakeRejectsMissingToken() {
        boolean result = interceptor.beforeHandshake(requestWithAuth(null), response, wsHandler, attributes);

        assertThat(result).isFalse();
        assertThat(attributes).isEmpty();
    }

    @Test
    void beforeHandshakeRejectsBlankToken() {
        boolean result = interceptor.beforeHandshake(requestWithAuth("   "), response, wsHandler, attributes);

        assertThat(result).isFalse();
        assertThat(attributes).isEmpty();
    }

    @Test
    void beforeHandshakeRejectsInvalidToken() {
        when(userDetailsFetcher.verifyToken("bad-token")).thenReturn(null);

        boolean result = interceptor.beforeHandshake(requestWithAuth("Bearer bad-token"), response, wsHandler, attributes);

        assertThat(result).isFalse();
        assertThat(attributes).isEmpty();
    }

    @Test
    void beforeHandshakeRejectsWhenFetcherThrows() {
        when(userDetailsFetcher.verifyToken("boom")).thenThrow(new RuntimeException("boom"));

        boolean result = interceptor.beforeHandshake(requestWithAuth("Bearer boom"), response, wsHandler, attributes);

        // token 验证异常被捕获并拒绝连接
        assertThat(result).isFalse();
        assertThat(attributes).isEmpty();
    }

    @Test
    void afterHandshakeDoesNothing() {
        // afterHandshake 为空实现，不应抛异常（request 为纯 mock，无需 stub getHeaders）
        interceptor.afterHandshake(mock(ServerHttpRequest.class), response, wsHandler, null);
        assertThat(attributes).isEmpty();
    }

    @Test
    void customTokenHeaderAndPrefixAreRespected() {
        properties.setTokenHeader("X-Auth-Token");
        properties.setTokenPrefix("Token ");
        TestUser user = new TestUser().setId(42L).setName("赵六");
        when(userDetailsFetcher.verifyToken("custom")).thenReturn(user);

        ServerHttpRequest request = mock(ServerHttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Auth-Token", "Token custom");
        when(request.getHeaders()).thenReturn(headers);

        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        assertThat(result).isTrue();
        assertThat(attributes).containsEntry("TOKEN", "custom");
        assertThat(attributes).containsEntry("USER_ID", "42");
        assertThat(attributes).containsEntry("USER_NAME", "赵六");
    }
}
