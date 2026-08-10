package xyz.migoo.framework.websocket.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import xyz.migoo.framework.security.core.AuthUserDetails;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link MiGooWebSocketHandler} 单元测试
 */
class MiGooWebSocketHandlerTest {

    /** 测试用 AuthUserDetails 子类 */
    static class TestUser extends AuthUserDetails<TestUser, Long> {
    }

    private WebSocketSessionManager sessionManager;
    private MiGooWebSocketHandler handler;

    @BeforeEach
    void setUp() {
        sessionManager = mock(WebSocketSessionManager.class);
        handler = new MiGooWebSocketHandler(sessionManager);
    }

    /** 创建带指定 attribute 的会话 mock */
    private WebSocketSession mockSession(String sessionId, Map<String, Object> attributes) {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn(sessionId);
        when(session.getAttributes()).thenReturn(attributes);
        when(session.isOpen()).thenReturn(true);
        return session;
    }

    @Test
    void afterConnectionEstablishedAddsSessionWhenUserIdPresent() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("USER_ID", "100");
        attributes.put("USER_NAME", "张三");
        WebSocketSession session = mockSession("s1", attributes);

        handler.afterConnectionEstablished(session);

        verify(sessionManager).addSession(session, "100");
    }

    @Test
    void afterConnectionEstablishedSkipsSessionWithoutUserId() {
        WebSocketSession session = mockSession("s1", new HashMap<>());

        handler.afterConnectionEstablished(session);

        verify(sessionManager, never()).addSession(any(), any());
    }

    @Test
    void afterConnectionClosedRemovesSession() {
        WebSocketSession session = mockSession("s1", new HashMap<>());
        when(sessionManager.getUserId("s1")).thenReturn("100");

        handler.afterConnectionClosed(session, CloseStatus.NORMAL);

        verify(sessionManager).getUserId("s1");
        verify(sessionManager).removeSession(session);
    }

    @Test
    void handleTransportErrorRemovesSession() {
        WebSocketSession session = mockSession("s1", new HashMap<>());
        when(sessionManager.getUserId("s1")).thenReturn("100");

        handler.handleTransportError(session, new RuntimeException("boom"));

        verify(sessionManager).getUserId("s1");
        verify(sessionManager).removeSession(session);
    }

    @Test
    void handleTextMessageEchoesPayload() throws Exception {
        WebSocketSession session = mockSession("s1", new HashMap<>());
        when(sessionManager.getUserId("s1")).thenReturn("100");

        handler.handleTextMessage(session, new TextMessage("hello"));

        verify(session).sendMessage(new TextMessage("Echo: hello"));
    }

    @Test
    void handleBinaryMessageEchoesPayload() throws Exception {
        WebSocketSession session = mockSession("s1", new HashMap<>());
        when(sessionManager.getUserId("s1")).thenReturn("100");
        byte[] data = new byte[]{1, 2, 3};

        handler.handleBinaryMessage(session, new BinaryMessage(data));

        verify(session).sendMessage(new BinaryMessage(data));
    }

    @Test
    void getUserDetailsReadsUserDetailsAttribute() {
        TestUser user = new TestUser().setId(100L).setName("张三");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("USER_DETAILS", user);
        WebSocketSession session = mockSession("s1", attributes);

        assertThat(handler.getUserDetails(session)).isSameAs(user);
    }

    @Test
    void getUserDetailsReturnsNullWhenAttributeMissing() {
        WebSocketSession session = mockSession("s1", new HashMap<>());

        assertThat(handler.getUserDetails(session)).isNull();
    }

    @Test
    void getUserIdDelegatesToSessionManager() {
        WebSocketSession session = mockSession("s1", new HashMap<>());
        when(sessionManager.getUserId("s1")).thenReturn("100");

        assertThat(handler.getUserId(session)).isEqualTo("100");
    }

    @Test
    void sendMessageToUserDelegatesToSessionManager() {
        handler.sendMessageToUser("u1", "hi");

        verify(sessionManager).sendToUser("u1", "hi");
    }

    @Test
    void broadcastDelegatesToSessionManager() {
        handler.broadcast("all");

        verify(sessionManager).broadcast("all");
    }

    @Test
    void joinRoomDelegatesWhenUserIdPresent() {
        WebSocketSession session = mockSession("s1", new HashMap<>());
        when(sessionManager.getUserId("s1")).thenReturn("100");

        handler.joinRoom(session, "room1");

        verify(sessionManager).joinRoom("room1", "100");
    }

    @Test
    void joinRoomSkipsWhenUserIdMissing() {
        WebSocketSession session = mockSession("s1", new HashMap<>());
        when(sessionManager.getUserId("s1")).thenReturn(null);

        handler.joinRoom(session, "room1");

        verify(sessionManager, never()).joinRoom(any(), any());
    }

    @Test
    void sendBinaryMessageToUserDelegatesToSessionManager() {
        byte[] data = new byte[]{1, 2};
        handler.sendBinaryMessageToUser("u1", data);

        verify(sessionManager).sendBinaryToUser("u1", data);
    }
}
