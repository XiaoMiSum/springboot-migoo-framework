package xyz.migoo.framework.websocket.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import xyz.migoo.framework.websocket.core.DistributedWebSocketSessionManager;
import xyz.migoo.framework.websocket.core.LocalWebSocketSessionManager;
import xyz.migoo.framework.websocket.core.MiGooWebSocketHandler;
import xyz.migoo.framework.websocket.core.WebSocketAuthInterceptor;
import xyz.migoo.framework.websocket.core.WebSocketSessionManager;

/**
 * WebSocket 自动配置类
 *
 * @author xiaomi
 */
@Configuration
@EnableWebSocket
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(name = "org.springframework.web.socket.WebSocketSession")
@ConditionalOnProperty(prefix = "migoo.websocket", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(WebSocketProperties.class)
@Import(WebSocketSecurityConfiguration.class)
public class MiGooWebSocketAutoConfiguration {

    /**
     * 单机会话管理器（默认）
     */
    @Bean
    @ConditionalOnMissingBean(WebSocketSessionManager.class)
    public LocalWebSocketSessionManager localWebSocketSessionManager() {
        return new LocalWebSocketSessionManager();
    }

    /**
     * 分布式会话管理器
     * <p>
     * 当用户配置 migoo.websocket.distributed=true 且 Redis 在 classpath 时启用
     */
    @Bean
    @ConditionalOnProperty(prefix = "migoo.websocket", name = "distributed", havingValue = "true")
    @ConditionalOnClass(name = "org.springframework.data.redis.core.RedisTemplate")
    @ConditionalOnMissingBean(WebSocketSessionManager.class)
    public DistributedWebSocketSessionManager distributedWebSocketSessionManager(
            RedisTemplate<String, String> redisTemplate,
            RedisMessageListenerContainer redisMessageListenerContainer) {
        DistributedWebSocketSessionManager manager = new DistributedWebSocketSessionManager(redisTemplate);
        manager.init(redisMessageListenerContainer);
        return manager;
    }

    /**
     * WebSocket 消息处理器
     */
    @Bean
    @ConditionalOnMissingBean
    public MiGooWebSocketHandler webSocketHandler(WebSocketSessionManager sessionManager) {
        return new MiGooWebSocketHandler(sessionManager);
    }

    /**
     * WebSocket 配置
     */
    @Bean
    public WebSocketConfigurer webSocketConfigurer(WebSocketProperties properties,
                                                    WebSocketAuthInterceptor authInterceptor,
                                                    MiGooWebSocketHandler webSocketHandler) {
        return registry -> {
            registry.addHandler(webSocketHandler, properties.getEndpoint())
                    .addInterceptors(authInterceptor)
                    .setAllowedOrigins(properties.getAllowedOrigins().split(","));
        };
    }

}
