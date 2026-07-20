package xyz.migoo.framework.websocket.core;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import xyz.migoo.framework.security.core.AuthUserDetails;

import java.io.IOException;

/**
 * WebSocket 消息处理器
 * <p>
 * 处理 WebSocket 连接、消息接收、连接关闭等事件
 * <p>
 * 使用方可以通过继承此类并重写方法来自定义处理逻辑
 *
 * @author xiaomi
 */
@Slf4j
public class MiGooWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;

    public MiGooWebSocketHandler(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * 连接建立成功
     *
     * @param session WebSocket 会话
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = (String) session.getAttributes().get("USER_ID");
        String userName = (String) session.getAttributes().get("USER_NAME");

        if (userId == null) {
            log.warn("[afterConnectionEstablished][会话ID({}) 用户ID为空]", session.getId());
            return;
        }

        sessionManager.addSession(session, userId);
        log.info("[afterConnectionEstablished][用户({}:{}) 连接成功]", userId, userName);
    }

    /**
     * 接收文本消息
     *
     * @param session WebSocket 会话
     * @param message 文本消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String userId = sessionManager.getUserId(session.getId());
        String payload = message.getPayload();
        log.info("[handleTextMessage][用户({}) 收到消息: {}]", userId, payload);

        // 默认实现：回声消息（子类可以重写此方法来处理消息）
        sendMessage(session, "Echo: " + payload);
    }

    /**
     * 连接关闭
     *
     * @param session WebSocket 会话
     * @param status  关闭状态
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, @NonNull CloseStatus status) {
        String userId = sessionManager.getUserId(session.getId());
        sessionManager.removeSession(session);
        log.info("[afterConnectionClosed][用户({}) 断开连接, 状态: {}]", userId, status);
    }

    /**
     * 传输错误
     *
     * @param session   WebSocket 会话
     * @param exception 异常
     */
    @Override
    public void handleTransportError(WebSocketSession session, @NonNull Throwable exception) {
        String userId = sessionManager.getUserId(session.getId());
        log.error("[handleTransportError][用户({}) 会话ID({}) 传输错误]", userId, session.getId(), exception);
        sessionManager.removeSession(session);
    }

    /**
     * 获取当前用户信息
     *
     * @param session WebSocket 会话
     * @return 用户信息
     */
    protected AuthUserDetails<?, ?> getUserDetails(WebSocketSession session) {
        return (AuthUserDetails<?, ?>) session.getAttributes().get("USER_DETAILS");
    }

    /**
     * 获取当前用户 ID
     *
     * @param session WebSocket 会话
     * @return 用户 ID
     */
    protected String getUserId(WebSocketSession session) {
        return sessionManager.getUserId(session.getId());
    }

    /**
     * 发送消息
     *
     * @param session WebSocket 会话
     * @param message 消息内容
     */
    protected void sendMessage(WebSocketSession session, String message) {
        try {
            if (session.isOpen()) {
                synchronized (session) {
                    session.sendMessage(new TextMessage(message));
                }
            }
        } catch (IOException e) {
            log.error("[sendMessage][会话ID({}) 发送消息失败]", session.getId(), e);
        }
    }

    /**
     * 发送消息给用户
     *
     * @param userId  用户 ID
     * @param message 消息内容
     */
    protected void sendMessageToUser(String userId, String message) {
        sessionManager.sendToUser(userId, message);
    }

    /**
     * 广播消息给所有在线用户
     *
     * @param message 消息内容
     */
    protected void broadcast(String message) {
        sessionManager.broadcast(message);
    }

}
