package xyz.migoo.framework.websocket.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.connection.DefaultMessage;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link DistributedWebSocketSessionManager} 单元测试
 * <p>
 * RedisTemplate 使用 Mockito mock，仅验证消息转发/发布逻辑，不依赖真实 Redis
 */
class DistributedWebSocketSessionManagerTest {

    /** Redis Pub/Sub 频道（与源码 CHANNEL 常量保持一致） */
    private static final String CHANNEL = "websocket:message";

    private RedisTemplate<String, String> redisTemplate;
    private DistributedWebSocketSessionManager manager;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        manager = new DistributedWebSocketSessionManager(redisTemplate);
    }

    /** 创建默认打开的会话 mock */
    private WebSocketSession mockSession(String sessionId) {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn(sessionId);
        when(session.isOpen()).thenReturn(true);
        return session;
    }

    /** 构造 Redis 消息 */
    private Message message(String payload) {
        return new DefaultMessage(CHANNEL.getBytes(StandardCharsets.UTF_8),
                payload.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void initRegistersListenerOnChannel() {
        RedisMessageListenerContainer container = mock(RedisMessageListenerContainer.class);

        manager.init(container);

        ArgumentCaptor<ChannelTopic> topicCaptor = ArgumentCaptor.forClass(ChannelTopic.class);
        verify(container).addMessageListener(eq(manager), topicCaptor.capture());
        assertThat(topicCaptor.getValue().getTopic()).isEqualTo(CHANNEL);
    }

    @Test
    void onMessageSendsToTargetUserSessions() throws Exception {
        WebSocketSession session = mockSession("s1");
        manager.addSession(session, "u1");

        manager.onMessage(message("u1:hello"), new byte[0]);

        verify(session).sendMessage(new TextMessage("hello"));
    }

    @Test
    void onMessageBroadcastsWhenUserIdEmpty() throws Exception {
        WebSocketSession session1 = mockSession("s1");
        WebSocketSession session2 = mockSession("s2");
        manager.addSession(session1, "u1");
        manager.addSession(session2, "u2");

        manager.onMessage(message(":hello-all"), new byte[0]);

        // 用户 ID 为空 -> 广播给所有本地会话
        verify(session1).sendMessage(new TextMessage("hello-all"));
        verify(session2).sendMessage(new TextMessage("hello-all"));
    }

    @Test
    void onMessageHandlesRoomMessage() throws Exception {
        WebSocketSession session1 = mockSession("s1");
        WebSocketSession session2 = mockSession("s2");
        manager.addSession(session1, "u1");
        manager.addSession(session2, "u2");
        manager.joinRoom("room1", "u1");
        manager.joinRoom("room1", "u2");

        manager.onMessage(message("ROOM:room1:room-msg"), new byte[0]);

        verify(session1).sendMessage(new TextMessage("room-msg"));
        verify(session2).sendMessage(new TextMessage("room-msg"));
    }

    @Test
    void onMessageHandlesRoomExceptMessage() throws Exception {
        WebSocketSession session1 = mockSession("s1");
        WebSocketSession session2 = mockSession("s2");
        manager.addSession(session1, "u1");
        manager.addSession(session2, "u2");
        manager.joinRoom("room1", "u1");
        manager.joinRoom("room1", "u2");

        manager.onMessage(message("ROOM_EXCEPT:room1:u2:room-msg"), new byte[0]);

        verify(session1).sendMessage(new TextMessage("room-msg"));
        verify(session2, never()).sendMessage(any());
    }

    @Test
    void onMessageIgnoresMalformedPayload() {
        // 格式错误的消息不应抛异常，静默忽略
        manager.onMessage(message("malformed-no-colon"), new byte[0]);
        manager.onMessage(message("ROOM:only-two-parts"), new byte[0]);

        assertThat(manager.getOnlineSessionCount()).isZero();
    }

    @Test
    void onMessageHandlesBinaryPayload() throws Exception {
        WebSocketSession session = mockSession("s1");
        manager.addSession(session, "u1");

        manager.onMessage(message("BINARY:u1:AQID"), new byte[0]);

        verify(session).sendMessage(new BinaryMessage(new byte[]{1, 2, 3}));
    }

    @Test
    void sendToUserWithoutLocalSessionsPublishesToRedis() {
        manager.sendToUser("u1", "hello");

        // 本地无该用户会话 -> 发布到 Redis，其他节点转发
        verify(redisTemplate).convertAndSend(CHANNEL, "u1:hello");
    }

    @Test
    void sendToUserWithLocalSessionsSendsLocallyWithoutPublishing() throws Exception {
        WebSocketSession session = mockSession("s1");
        manager.addSession(session, "u1");

        manager.sendToUser("u1", "hello");

        verify(session).sendMessage(new TextMessage("hello"));
        verify(redisTemplate, never()).convertAndSend(any(String.class), any(String.class));
    }

    @Test
    void broadcastWithoutLocalSessionsPublishesEmptyUserId() {
        manager.broadcast("all");

        verify(redisTemplate).convertAndSend(CHANNEL, ":all");
    }

    @Test
    void sendBinaryToUserWithoutLocalSessionsPublishesBase64Payload() {
        manager.sendBinaryToUser("u1", new byte[]{1, 2, 3});

        verify(redisTemplate).convertAndSend(CHANNEL, "BINARY:u1:AQID");
    }

    @Test
    void sendToRoomWithoutLocalSessionsPublishesRoomMessage() {
        manager.joinRoom("room1", "u1");
        // 本地没有 room1 成员会话（u1 未连接本节点）
        manager.sendToRoom("room1", "room-msg");

        verify(redisTemplate).convertAndSend(CHANNEL, "ROOM:room1:room-msg");
    }

    @Test
    void sendToRoomExceptWithoutLocalSessionsPublishesRoomExceptMessage() {
        manager.joinRoom("room1", "u1");
        manager.joinRoom("room1", "u2");
        manager.sendToRoomExcept("room1", "u1", "room-msg");

        verify(redisTemplate).convertAndSend(CHANNEL, "ROOM_EXCEPT:room1:u1:room-msg");
    }
}
