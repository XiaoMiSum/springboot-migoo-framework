package xyz.migoo.framework.websocket.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket 会话管理器抽象基类
 * <p>
 * 封装本地会话管理的公共逻辑，子类只需实现跨节点通信
 *
 * @author xiaomi
 */
@Slf4j
public abstract class AbstractWebSocketSessionManager implements WebSocketSessionManager {

    /**
     * 用户 ID -> 会话 ID 集合
     */
    protected final Map<String, Set<String>> userSessions = new ConcurrentHashMap<>();

    /**
     * 会话 ID -> 会话
     */
    protected final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * 会话 ID -> 用户 ID
     */
    protected final Map<String, String> sessionUser = new ConcurrentHashMap<>();

    /**
     * 房间 ID -> 用户 ID 集合
     */
    protected final Map<String, Set<String>> roomUsers = new ConcurrentHashMap<>();

    /**
     * 用户 ID -> 房间 ID 集合
     */
    protected final Map<String, Set<String>> userRooms = new ConcurrentHashMap<>();

    @Override
    public void addSession(WebSocketSession session, String userId) {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        sessionUser.put(sessionId, userId);
        userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(sessionId);
        onAddSession(session, userId);
        log.info("[addSession][用户({}) 连接成功，会话ID({})]", userId, sessionId);
    }

    @Override
    public void removeSession(WebSocketSession session) {
        String sessionId = session.getId();
        String userId = sessionUser.remove(sessionId);
        sessions.remove(sessionId);

        if (userId != null) {
            Set<String> userSessionIds = userSessions.get(userId);
            if (userSessionIds != null) {
                userSessionIds.remove(sessionId);
                if (userSessionIds.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
            onRemoveSession(session, userId);
            log.info("[removeSession][用户({}) 断开连接，会话ID({})]", userId, sessionId);
        }
    }

    @Override
    public Collection<WebSocketSession> getUserSessions(String userId) {
        Set<String> sessionIds = userSessions.get(userId);
        if (sessionIds == null || sessionIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return sessionIds.stream()
                .map(sessions::get)
                .filter(s -> s != null && s.isOpen())
                .toList();
    }

    @Override
    public WebSocketSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    @Override
    public String getUserId(String sessionId) {
        return sessionUser.get(sessionId);
    }

    @Override
    public void sendToSession(String sessionId, String message) {
        WebSocketSession session = sessions.get(sessionId);
        if (session != null && session.isOpen()) {
            doSendMessage(session, message);
        }
    }

    @Override
    public int getOnlineUserCount() {
        return userSessions.size();
    }

    @Override
    public int getOnlineSessionCount() {
        return sessions.size();
    }

    @Override
    public Set<String> getOnlineUserIds() {
        return userSessions.keySet();
    }

    /**
     * 发送消息给用户
     * <p>
     * 子类可重写此方法实现跨节点发送
     *
     * @param userId  用户 ID
     * @param message 消息内容
     */
    @Override
    public void sendToUser(String userId, String message) {
        Collection<WebSocketSession> userSessionList = getUserSessions(userId);
        if (!userSessionList.isEmpty()) {
            for (WebSocketSession session : userSessionList) {
                doSendMessage(session, message);
            }
            return;
        }
        onSendToUser(userId, message);
    }

    /**
     * 广播消息
     * <p>
     * 子类可重写此方法实现跨节点广播
     *
     * @param message 消息内容
     */
    @Override
    public void broadcast(String message) {
        sessions.values().stream()
                .filter(WebSocketSession::isOpen)
                .forEach(session -> doSendMessage(session, message));
        onBroadcast(message);
    }

    // ========== 房间管理实现 ==========

    @Override
    public void joinRoom(String roomId, String userId) {
        roomUsers.computeIfAbsent(roomId, k -> new CopyOnWriteArraySet<>()).add(userId);
        userRooms.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(roomId);
        onJoinRoom(roomId, userId);
        log.info("[joinRoom][用户({}) 加入房间({})]", userId, roomId);
    }

    @Override
    public void leaveRoom(String roomId, String userId) {
        Set<String> users = roomUsers.get(roomId);
        if (users != null) {
            users.remove(userId);
            if (users.isEmpty()) {
                roomUsers.remove(roomId);
            }
        }
        Set<String> rooms = userRooms.get(userId);
        if (rooms != null) {
            rooms.remove(roomId);
            if (rooms.isEmpty()) {
                userRooms.remove(userId);
            }
        }
        onLeaveRoom(roomId, userId);
        log.info("[leaveRoom][用户({}) 离开房间({})]", userId, roomId);
    }

    @Override
    public Set<String> getRoomMembers(String roomId) {
        Set<String> users = roomUsers.get(roomId);
        return users != null ? Set.copyOf(users) : Set.of();
    }

    @Override
    public Set<String> getUserRooms(String userId) {
        Set<String> rooms = userRooms.get(userId);
        return rooms != null ? Set.copyOf(rooms) : Set.of();
    }

    @Override
    public boolean isRoomMember(String roomId, String userId) {
        Set<String> users = roomUsers.get(roomId);
        return users != null && users.contains(userId);
    }

    @Override
    public int getRoomCount() {
        return roomUsers.size();
    }

    @Override
    public int getRoomMemberCount(String roomId) {
        Set<String> users = roomUsers.get(roomId);
        return users != null ? users.size() : 0;
    }

    // ========== 房间消息实现 ==========

    @Override
    public void sendToRoom(String roomId, String message) {
        Set<String> userIds = roomUsers.get(roomId);
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        for (String userId : userIds) {
            sendToUser(userId, message);
        }
        onSendToRoom(roomId, message);
    }

    @Override
    public void sendToRoomExcept(String roomId, String excludeUserId, String message) {
        Set<String> userIds = roomUsers.get(roomId);
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        for (String userId : userIds) {
            if (!userId.equals(excludeUserId)) {
                sendToUser(userId, message);
            }
        }
        onSendToRoomExcept(roomId, excludeUserId, message);
    }

    // ========== 房间相关回调 ==========

    /**
     * 用户加入房间后的回调
     *
     * @param roomId 房间 ID
     * @param userId 用户 ID
     */
    protected void onJoinRoom(String roomId, String userId) {
        // 默认空实现，子类可重写
    }

    /**
     * 用户离开房间后的回调
     *
     * @param roomId 房间 ID
     * @param userId 用户 ID
     */
    protected void onLeaveRoom(String roomId, String userId) {
        // 默认空实现，子类可重写
    }

    /**
     * 房间消息发送后的回调（用于分布式场景）
     *
     * @param roomId  房间 ID
     * @param message 消息内容
     */
    protected void onSendToRoom(String roomId, String message) {
        // 默认空实现，子类可重写
    }

    /**
     * 房间消息发送（排除指定用户）后的回调（用于分布式场景）
     *
     * @param roomId        房间 ID
     * @param excludeUserId 排除的用户 ID
     * @param message       消息内容
     */
    protected void onSendToRoomExcept(String roomId, String excludeUserId, String message) {
        // 默认空实现，子类可重写
    }

    /**
     * 会话添加后的回调
     *
     * @param session WebSocket 会话
     * @param userId  用户 ID
     */
    protected void onAddSession(WebSocketSession session, String userId) {
        // 默认空实现，子类可重写
    }

    /**
     * 会话移除后的回调
     *
     * @param session WebSocket 会话
     * @param userId  用户 ID
     */
    protected void onRemoveSession(WebSocketSession session, String userId) {
        // 默认空实现，子类可重写
    }

    /**
     * 本地没有用户会话时的回调（用于分布式场景）
     *
     * @param userId  用户 ID
     * @param message 消息内容
     */
    protected void onSendToUser(String userId, String message) {
        // 默认空实现，子类可重写
    }

    /**
     * 本地广播后的回调（用于分布式场景）
     *
     * @param message 消息内容
     */
    protected void onBroadcast(String message) {
        // 默认空实现，子类可重写
    }

    /**
     * 发送消息
     *
     * @param session WebSocket 会话
     * @param message 消息内容
     */
    protected void doSendMessage(WebSocketSession session, String message) {
        try {
            if (session.isOpen()) {
                synchronized (session) {
                    session.sendMessage(new TextMessage(message));
                }
            }
        } catch (IOException e) {
            log.error("[doSendMessage][会话ID({}) 发送消息失败]", session.getId(), e);
        }
    }

}
