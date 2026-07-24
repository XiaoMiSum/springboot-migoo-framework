package xyz.migoo.framework.websocket.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * WebSocket 配置属性
 *
 * @author xiaomi
 */
@Data
@ConfigurationProperties(prefix = "migoo.websocket")
public class WebSocketProperties {

    /**
     * 是否启用 WebSocket
     */
    private boolean enabled = true;

    /**
     * 是否启用分布式模式（需要 Redis 支持）
     */
    private boolean distributed = false;

    /**
     * WebSocket 端点路径（单端点，兼容旧版本）
     */
    private String endpoint = "/ws";

    /**
     * WebSocket 端点路径列表（多端点，优先级高于 endpoint）
     * <p>
     * 配置示例：
     * <pre>
     * migoo:
     *   websocket:
     *     endpoints:
     *       - /ws/chat
     *       - /ws/notify
     *       - /ws/live
     * </pre>
     */
    private List<String> endpoints = new ArrayList<>();

    /**
     * 允许的来源，多个用逗号分隔
     */
    private String allowedOrigins = "*";

    /**
     * Token Header 名称
     */
    private String tokenHeader = "Authorization";

    /**
     * Token 前缀
     */
    private String tokenPrefix = "Bearer ";

    /**
     * 最大会话超时时间（毫秒），默认 30 分钟
     */
    private long maxSessionTimeout = 1800000;

    /**
     * 获取所有端点路径
     *
     * @return 端点路径列表
     */
    public List<String> getAllEndpoints() {
        if (endpoints != null && !endpoints.isEmpty()) {
            return endpoints;
        }
        return List.of(endpoint);
    }

}
