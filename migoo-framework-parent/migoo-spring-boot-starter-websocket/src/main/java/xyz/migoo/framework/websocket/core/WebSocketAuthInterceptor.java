package xyz.migoo.framework.websocket.core;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import xyz.migoo.framework.security.core.AuthUserDetails;
import xyz.migoo.framework.security.core.authentication.AuthUserDetailsFetcher;
import xyz.migoo.framework.websocket.config.WebSocketProperties;

import java.util.Map;

/**
 * WebSocket 认证拦截器
 * <p>
 * 在 WebSocket 握手前验证 token，复用 security 模块的 AuthUserDetailsFetcher
 *
 * @author xiaomi
 */
@Slf4j
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final WebSocketProperties webSocketProperties;
    private final AuthUserDetailsFetcher<? extends AuthUserDetails<?, ?>> userDetailsFetcher;

    public WebSocketAuthInterceptor(WebSocketProperties webSocketProperties,
                                    AuthUserDetailsFetcher<? extends AuthUserDetails<?, ?>> userDetailsFetcher) {
        this.webSocketProperties = webSocketProperties;
        this.userDetailsFetcher = userDetailsFetcher;
    }

    /**
     * 握手前验证
     *
     * @param request    HTTP 请求
     * @param response   HTTP 响应
     * @param wsHandler  WebSocket 处理器
     * @param attributes 会话属性
     * @return 是否验证通过
     */
    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
                                   @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) {
        // 获取 token
        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            log.warn("[beforeHandshake][token 为空，拒绝连接]");
            return false;
        }

        // 验证 token（复用 security 模块）
        try {
            AuthUserDetails<?, ?> userDetails = userDetailsFetcher.verifyToken(token);
            if (userDetails == null) {
                log.warn("[beforeHandshake][token 验证失败，拒绝连接]");
                return false;
            }
            // 将用户信息和 token 存储到会话属性
            attributes.put("USER_DETAILS", userDetails);
            attributes.put("USER_ID", String.valueOf(userDetails.getId()));
            attributes.put("USER_NAME", userDetails.getName());
            attributes.put("TOKEN", token);
            log.info("[beforeHandshake][用户({}) 连接成功]", userDetails.getName());
            return true;
        } catch (Exception e) {
            log.error("[beforeHandshake][token 验证异常，拒绝连接]", e);
            return false;
        }
    }

    /**
     * 握手后处理
     *
     * @param request   HTTP 请求
     * @param response  HTTP 响应
     * @param wsHandler WebSocket 处理器
     * @param exception 异常
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 握手完成后，无需处理
    }

    /**
     * 从请求中提取 token
     *
     * @param request HTTP 请求
     * @return token
     */
    private String extractToken(ServerHttpRequest request) {
        // 1. 从 Header 中获取
        String header = webSocketProperties.getTokenHeader();
        String token = request.getHeaders().getFirst(header);
        if (StringUtils.hasText(token)) {
            return removePrefix(token);
        }

        // 2. 从查询参数中获取（WebSocket 握手时可以通过 URL 传递 token）
        if (request instanceof ServletServerHttpRequest servletRequest) {
            // 也支持 token 参数名
            token = servletRequest.getServletRequest().getParameter("token");
            if (StringUtils.hasText(token)) {
                return removePrefix(token);
            }
        }

        return null;
    }

    /**
     * 移除 token 前缀
     *
     * @param token token
     * @return 移除前缀后的 token
     */
    private String removePrefix(String token) {
        String prefix = webSocketProperties.getTokenPrefix();
        if (StringUtils.hasText(prefix) && token.startsWith(prefix)) {
            return token.substring(prefix.length()).trim();
        }
        return token.trim();
    }

}
