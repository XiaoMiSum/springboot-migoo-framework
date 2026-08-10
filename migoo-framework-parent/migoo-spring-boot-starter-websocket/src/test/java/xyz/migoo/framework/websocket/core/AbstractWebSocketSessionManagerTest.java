package xyz.migoo.framework.websocket.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AbstractWebSocketSessionManager} 单元测试
 * <p>
 * 使用具体子类 {@link LocalWebSocketSessionManager} 进行测试，会话对象使用 Mockito mock
 */
class AbstractWebSocketSessionManagerTest {

    private LocalWebSocketSessionManager manager;

    @BeforeEach
    void setUp() {
        manager = new LocalWebSocketSessionManager();
    }

    /** 创建指定 id、默认打开的会话 mock */
    private WebSocketSession mockSession(String sessionId) {
        return mockSession(sessionId, true);
    }

    /** 创建指定 id 和打开状态的会话 mock */
    private WebSocketSession mockSession(String sessionId, boolean open) {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn(sessionId);
        when(session.isOpen()).thenReturn(open);
        return session;
    }

    @Test
    void addSessionRegistersSessionAndUser() {
        WebSocketSession session = mockSession("s1");
        manager.addSession(session, "u1");

        assertThat(manager.getSession("s1")).isSameAs(session);
        assertThat(manager.getUserId("s1")).isEqualTo("u1");
        assertThat(manager.getUserSessions("u1")).containsExactly(session);
        assertThat(manager.getOnlineUserCount()).isEqualTo(1);
        assertThat(manager.getOnlineSessionCount()).isEqualTo(1);
        assertThat(manager.getOnlineUserIds()).containsExactly("u1");
    }

    @Test
    void removeSessionUnregistersSessionAndUser() {
        WebSocketSession session = mockSession("s1");
        manager.addSession(session, "u1");

        manager.removeSession(session);

        assertThat(manager.getSession("s1")).isNull();
        assertThat(manager.getUserId("s1")).isNull();
        assertThat(manager.getUserSessions("u1")).isEmpty();
        assertThat(manager.getOnlineUserCount()).isZero();
        assertThat(manager.getOnlineSessionCount()).isZero();
        assertThat(manager.getOnlineUserIds()).isEmpty();
    }

    @Test
    void removeUnknownSessionIsNoop() {
        WebSocketSession session = mockSession("unknown");
        manager.removeSession(session);

        assertThat(manager.getSession("unknown")).isNull();
        assertThat(manager.getOnlineUserCount()).isZero();
        assertThat(manager.getOnlineSessionCount()).isZero();
    }

    @Test
    void sameUserWithMultipleSessionsIsTracked() {
        WebSocketSession session1 = mockSession("s1");
        WebSocketSession session2 = mockSession("s2");
        WebSocketSession session3 = mockSession("s3");
        manager.addSession(session1, "u1");
        manager.addSession(session2, "u1");
        manager.addSession(session3, "u2");

        // 同一用户多会话
        assertThat(manager.getUserSessions("u1")).containsExactlyInAnyOrder(session1, session2);
        assertThat(manager.getOnlineUserCount()).isEqualTo(2);
        assertThat(manager.getOnlineSessionCount()).isEqualTo(3);
        assertThat(manager.getOnlineUserIds()).containsExactlyInAnyOrder("u1", "u2");

        // 移除一个会话后，该用户仍在线
        manager.removeSession(session1);
        assertThat(manager.getUserSessions("u1")).containsExactly(session2);
        assertThat(manager.getOnlineUserCount()).isEqualTo(2);
        assertThat(manager.getOnlineSessionCount()).isEqualTo(2);

        // 移除最后一个会话后，用户下线
        manager.removeSession(session2);
        assertThat(manager.getUserSessions("u1")).isEmpty();
        assertThat(manager.getOnlineUserCount()).isEqualTo(1);
        assertThat(manager.getOnlineUserIds()).containsExactly("u2");
    }

    @Test
    void getUserSessionsFiltersClosedSessions() {
        WebSocketSession session1 = mockSession("s1");
        WebSocketSession session2 = mockSession("s2", false);
        manager.addSession(session1, "u1");
        manager.addSession(session2, "u1");

        // 已关闭的会话不会出现在 getUserSessions 结果中
        assertThat(manager.getUserSessions("u1")).containsExactly(session1);
        // 但通过 getSession 仍能直接拿到（清理由 removeSession 负责）
        assertThat(manager.getSession("s2")).isSameAs(session2);
        assertThat(manager.getOnlineSessionCount()).isEqualTo(2);
    }

    @Test
    void sendToSessionSendsTextMessageToOpenSession() throws Exception {
        WebSocketSession session = mockSession("s1");
        manager.addSession(session, "u1");

        manager.sendToSession("s1", "hello");

        verify(session).sendMessage(new TextMessage("hello"));
    }

    @Test
    void sendToSessionSkipsNonExistentSession() {
        manager.sendToSession("not-exist", "hello");

        // 不存在的会话静默忽略，不影响在线计数
        assertThat(manager.getOnlineSessionCount()).isZero();
        assertThat(manager.getSession("not-exist")).isNull();
    }

    @Test
    void sendToSessionSkipsClosedSession() throws Exception {
        WebSocketSession session = mockSession("s1");
        manager.addSession(session, "u1");
        // 会话变为关闭状态
        when(session.isOpen()).thenReturn(false);

        manager.sendToSession("s1", "hello");

        verify(session, never()).sendMessage(any());
    }

    @Test
    void sendToUserSendsToAllUserSessions() throws Exception {
        WebSocketSession session1 = mockSession("s1");
        WebSocketSession session2 = mockSession("s2");
        manager.addSession(session1, "u1");
        manager.addSession(session2, "u1");

        manager.sendToUser("u1", "hi");

        verify(session1).sendMessage(new TextMessage("hi"));
        verify(session2).sendMessage(new TextMessage("hi"));
    }

    @Test
    void sendToUserWithNoSessionsIsNoop() {
        manager.sendToUser("ghost", "hi");

        assertThat(manager.getUserSessions("ghost")).isEmpty();
        assertThat(manager.getOnlineSessionCount()).isZero();
    }

    @Test
    void broadcastSendsToAllOpenSessionsOnly() throws Exception {
        WebSocketSession session1 = mockSession("s1");
        WebSocketSession session2 = mockSession("s2", false);
        WebSocketSession session3 = mockSession("s3");
        manager.addSession(session1, "u1");
        manager.addSession(session2, "u2");
        manager.addSession(session3, "u3");

        manager.broadcast("broadcast!");

        verify(session1).sendMessage(new TextMessage("broadcast!"));
        verify(session3).sendMessage(new TextMessage("broadcast!"));
        verify(session2, never()).sendMessage(any());
    }

    @Test
    void sendBinaryToSessionSendsBinaryMessage() throws Exception {
        WebSocketSession session = mockSession("s1");
        manager.addSession(session, "u1");
        byte[] data = new byte[]{1, 2, 3};

        manager.sendBinaryToSession("s1", data);

        verify(session).sendMessage(new BinaryMessage(data));
    }

    @Test
    void sendBinaryToUserSendsToAllUserSessions() throws Exception {
        WebSocketSession session1 = mockSession("s1");
        WebSocketSession session2 = mockSession("s2");
        manager.addSession(session1, "u1");
        manager.addSession(session2, "u1");
        byte[] data = new byte[]{9, 8, 7};

        manager.sendBinaryToUser("u1", data);

        verify(session1).sendMessage(new BinaryMessage(data));
        verify(session2).sendMessage(new BinaryMessage(data));
    }

    @Test
    void roomManagementTracksMembers() {
        manager.joinRoom("room1", "u1");
        manager.joinRoom("room1", "u2");
        manager.joinRoom("room2", "u1");

        assertThat(manager.getRoomMembers("room1")).containsExactlyInAnyOrder("u1", "u2");
        assertThat(manager.getRoomMemberCount("room1")).isEqualTo(2);
        assertThat(manager.getRoomCount()).isEqualTo(2);
        assertThat(manager.isRoomMember("room1", "u1")).isTrue();
        assertThat(manager.isRoomMember("room1", "u3")).isFalse();
        assertThat(manager.getUserRooms("u1")).containsExactlyInAnyOrder("room1", "room2");

        manager.leaveRoom("room1", "u1");

        assertThat(manager.getRoomMembers("room1")).containsExactly("u2");
        assertThat(manager.isRoomMember("room1", "u1")).isFalse();
        assertThat(manager.getRoomMemberCount("room1")).isEqualTo(1);
        assertThat(manager.getUserRooms("u1")).containsExactly("room2");
    }

    @Test
    void sendToRoomSendsMessageToAllRoomMembers() throws Exception {
        WebSocketSession session1 = mockSession("s1");
        WebSocketSession session2 = mockSession("s2");
        WebSocketSession session3 = mockSession("s3");
        manager.addSession(session1, "u1");
        manager.addSession(session2, "u2");
        manager.addSession(session3, "u3");
        manager.joinRoom("room1", "u1");
        manager.joinRoom("room1", "u2");

        manager.sendToRoom("room1", "room-msg");

        verify(session1).sendMessage(new TextMessage("room-msg"));
        verify(session2).sendMessage(new TextMessage("room-msg"));
        verify(session3, never()).sendMessage(any());
    }

    @Test
    void sendToRoomExceptSkipsExcludedUser() throws Exception {
        WebSocketSession session1 = mockSession("s1");
        WebSocketSession session2 = mockSession("s2");
        manager.addSession(session1, "u1");
        manager.addSession(session2, "u2");
        manager.joinRoom("room1", "u1");
        manager.joinRoom("room1", "u2");

        manager.sendToRoomExcept("room1", "u2", "room-msg");

        verify(session1).sendMessage(new TextMessage("room-msg"));
        verify(session2, never()).sendMessage(any());
    }

    @Test
    void sendToEmptyRoomIsNoop() {
        manager.sendToRoom("empty", "msg");
        manager.sendToRoomExcept("empty", "u1", "msg");

        assertThat(manager.getRoomMemberCount("empty")).isZero();
        assertThat(manager.getRoomMembers("empty")).isEmpty();
    }
}
