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

    // ========== 房间管理 ==========

    /**
     * 用户加入房间
     *
     * @param roomId 房间 ID
     * @param userId 用户 ID
     */
    void joinRoom(String roomId, String userId);

    /**
     * 用户离开房间
     *
     * @param roomId 房间 ID
     * @param userId 用户 ID
     */
    void leaveRoom(String roomId, String userId);

    /**
     * 获取房间内所有用户 ID
     *
     * @param roomId 房间 ID
     * @return 用户 ID 集合
     */
    Set<String> getRoomMembers(String roomId);

    /**
     * 获取用户加入的所有房间
     *
     * @param userId 用户 ID
     * @return 房间 ID 集合
     */
    Set<String> getUserRooms(String userId);

    /**
     * 判断用户是否在房间内
     *
     * @param roomId 房间 ID
     * @param userId 用户 ID
     * @return 是否在房间内
     */
    boolean isRoomMember(String roomId, String userId);

    /**
     * 获取房间数量
     *
     * @return 房间数量
     */
    int getRoomCount();

    /**
     * 获取房间内在线用户数量
     *
     * @param roomId 房间 ID
     * @return 在线用户数量
     */
    int getRoomMemberCount(String roomId);

    // ========== 房间消息 ==========

    /**
     * 发送消息给房间内所有用户
     *
     * @param roomId  房间 ID
     * @param message 消息内容
     */
    void sendToRoom(String roomId, String message);

    /**
     * 发送消息给房间内所有用户（排除指定用户）
     *
     * @param roomId        房间 ID
     * @param excludeUserId 排除的用户 ID
     * @param message       消息内容
     */
    void sendToRoomExcept(String roomId, String excludeUserId, String message);

}
