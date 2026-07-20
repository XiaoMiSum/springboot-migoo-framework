package xyz.migoo.framework.websocket.core;

import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;
import java.util.Set;

/**
 * WebSocket 会话管理器接口
 * <p>
 * 提供会话管理和消息发送能力
 * <ul>
 *   <li>单机场景：{@link LocalWebSocketSessionManager}</li>
 *   <li>分布式场景：{@link DistributedWebSocketSessionManager}</li>
 * </ul>
 *
 * @author xiaomi
 */
public interface WebSocketSessionManager {

    /**
     * 添加会话
     *
     * @param session WebSocket 会话
     * @param userId  用户 ID
     */
    void addSession(WebSocketSession session, String userId);

    /**
     * 移除会话
     *
     * @param session WebSocket 会话
     */
    void removeSession(WebSocketSession session);

    /**
     * 获取用户的所有会话
     *
     * @param userId 用户 ID
     * @return 会话集合
     */
    Collection<WebSocketSession> getUserSessions(String userId);

    /**
     * 获取会话
     *
     * @param sessionId 会话 ID
     * @return WebSocket 会话
     */
    WebSocketSession getSession(String sessionId);

    /**
     * 获取会话对应的用户 ID
     *
     * @param sessionId 会话 ID
     * @return 用户 ID
     */
    String getUserId(String sessionId);

    /**
     * 发送消息给指定用户
     *
     * @param userId  用户 ID
     * @param message 消息内容
     */
    void sendToUser(String userId, String message);

    /**
     * 发送消息给指定会话
     *
     * @param sessionId 会话 ID
     * @param message   消息内容
     */
    void sendToSession(String sessionId, String message);

    /**
     * 广播消息给所有在线用户
     *
     * @param message 消息内容
     */
    void broadcast(String message);

    /**
     * 获取在线用户数量
     *
     * @return 在线用户数量
     */
    int getOnlineUserCount();

    /**
     * 获取在线会话数量
     *
     * @return 在线会话数量
     */
    int getOnlineSessionCount();

    /**
     * 获取所有在线用户 ID
     *
     * @return 用户 ID 集合
     */
    Set<String> getOnlineUserIds();

}
