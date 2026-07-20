package xyz.migoo.framework.websocket.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.nio.charset.StandardCharsets;
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
    public void onMessage(Message message, byte[] pattern) {
        try {
            String payload = new String(message.getBody(), StandardCharsets.UTF_8);

            // 解析消息：userId:message
            int index = payload.indexOf(':');
            if (index == -1) {
                log.warn("[onMessage][消息格式错误: {}]", payload);
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
        } catch (Exception e) {
            log.error("[onMessage][处理 Redis 消息异常]", e);
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

}
