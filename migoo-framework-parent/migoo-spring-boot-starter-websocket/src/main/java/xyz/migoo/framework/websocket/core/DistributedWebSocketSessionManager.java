package xyz.migoo.framework.websocket.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

/**
 * 分布式 WebSocket 会话管理器
 * <p>
 * 适用于分布式场景，使用 Redis Pub/Sub 实现跨节点消息推送
 *
 * @author xiaomi
 */
@Slf4j
public class DistributedWebSocketSessionManager extends AbstractWebSocketSessionManager implements MessageListener {

    /**
     * Redis Pub/Sub 频道
     */
    private static final String CHANNEL = "websocket:message";

    /**
     * 当前节点标识
     */
    private final String nodeId = UUID.randomUUID().toString();

    /**
     * Redis 操作模板
     */
    private final RedisTemplate<String, String> redisTemplate;

    public DistributedWebSocketSessionManager(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 初始化 Redis 监听容器
     *
     * @param container Redis 消息监听容器
     */
    public void init(RedisMessageListenerContainer container) {
        container.addMessageListener(this, new ChannelTopic(CHANNEL));
        log.info("[init][分布式 WebSocket 会话管理器初始化完成，节点ID({})]", nodeId);
    }

    @Override
    protected void onSendToUser(String userId, String message) {
        publishMessage(userId, message);
    }

    @Override
    protected void onBroadcast(String message) {
        publishMessage(null, message);
    }

    @Override
    protected void onSendToRoom(String roomId, String message) {
        publishRoomMessage(roomId, message, null);
    }

    @Override
    protected void onSendToRoomExcept(String roomId, String excludeUserId, String message) {
        publishRoomMessage(roomId, message, excludeUserId);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String payload = new String(message.getBody(), StandardCharsets.UTF_8);

            // 解析消息格式
            if (payload.startsWith("ROOM:")) {
                handleRoomMessage(payload);
            } else if (payload.startsWith("ROOM_EXCEPT:")) {
                handleRoomExceptMessage(payload);
            } else {
                handleUserMessage(payload);
            }
        } catch (Exception e) {
            log.error("[onMessage][处理 Redis 消息异常]", e);
        }
    }

    /**
     * 处理用户消息/广播消息
     *
     * @param payload 消息内容
     */
    private void handleUserMessage(String payload) {
        // 解析消息：userId:message
        int index = payload.indexOf(':');
        if (index == -1) {
            log.warn("[handleUserMessage][消息格式错误: {}]", payload);
            return;
        }

        String targetUserId = payload.substring(0, index);
        String msg = payload.substring(index + 1);

        // 如果指定了用户 ID，只发送给该用户；否则广播
        if (!targetUserId.isEmpty()) {
            getUserSessions(targetUserId).forEach(session -> doSendMessage(session, msg));
        } else {
            sessions.values().stream()
                    .filter(session -> session.isOpen())
                    .forEach(session -> doSendMessage(session, msg));
        }
    }

    /**
     * 处理房间消息
     *
     * @param payload 消息内容，格式：ROOM:roomId:message
     */
    private void handleRoomMessage(String payload) {
        // 格式：ROOM:roomId:message
        String[] parts = payload.split(":", 3);
        if (parts.length < 3) {
            log.warn("[handleRoomMessage][消息格式错误: {}]", payload);
            return;
        }

        String roomId = parts[1];
        String msg = parts[2];

        // 在本地查找房间成员并发送
        Set<String> userIds = roomUsers.get(roomId);
        if (userIds != null) {
            for (String userId : userIds) {
                getUserSessions(userId).forEach(session -> doSendMessage(session, msg));
            }
        }
    }

    /**
     * 处理房间消息（排除指定用户）
     *
     * @param payload 消息内容，格式：ROOM_EXCEPT:roomId:excludeUserId:message
     */
    private void handleRoomExceptMessage(String payload) {
        // 格式：ROOM_EXCEPT:roomId:excludeUserId:message
        String[] parts = payload.split(":", 4);
        if (parts.length < 4) {
            log.warn("[handleRoomExceptMessage][消息格式错误: {}]", payload);
            return;
        }

        String roomId = parts[1];
        String excludeUserId = parts[2];
        String msg = parts[3];

        // 在本地查找房间成员并发送（排除指定用户）
        Set<String> userIds = roomUsers.get(roomId);
        if (userIds != null) {
            for (String userId : userIds) {
                if (!userId.equals(excludeUserId)) {
                    getUserSessions(userId).forEach(session -> doSendMessage(session, msg));
                }
            }
        }
    }

    /**
     * 发布消息到 Redis
     *
     * @param targetUserId 目标用户 ID（null 表示广播）
     * @param message      消息内容
     */
    private void publishMessage(String targetUserId, String message) {
        try {
            String payload = (targetUserId != null ? targetUserId : "") + ":" + message;
            redisTemplate.convertAndSend(CHANNEL, payload);
        } catch (Exception e) {
            log.error("[publishMessage][发布 Redis 消息异常]", e);
        }
    }

    /**
     * 发布房间消息到 Redis
     *
     * @param roomId        房间 ID
     * @param message       消息内容
     * @param excludeUserId 排除的用户 ID（null 表示不排除）
     */
    private void publishRoomMessage(String roomId, String message, String excludeUserId) {
        try {
            String payload;
            if (excludeUserId != null) {
                payload = "ROOM_EXCEPT:" + roomId + ":" + excludeUserId + ":" + message;
            } else {
                payload = "ROOM:" + roomId + ":" + message;
            }
            redisTemplate.convertAndSend(CHANNEL, payload);
        } catch (Exception e) {
            log.error("[publishRoomMessage][发布 Redis 房间消息异常]", e);
        }
    }

}
