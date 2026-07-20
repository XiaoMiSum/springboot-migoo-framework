package xyz.migoo.framework.websocket.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

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
     * WebSocket 端点路径
     */
    private String endpoint = "/ws";

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

}
