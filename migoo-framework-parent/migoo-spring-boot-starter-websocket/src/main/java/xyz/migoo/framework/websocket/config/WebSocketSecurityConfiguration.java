package xyz.migoo.framework.websocket.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.migoo.framework.security.core.AuthUserDetails;
import xyz.migoo.framework.security.core.authentication.AuthUserDetailsFetcher;
import xyz.migoo.framework.websocket.core.WebSocketAuthInterceptor;

/**
 * WebSocket Security 配置
 * <p>
 * 当 security 模块存在时自动配置，复用 AuthUserDetailsFetcher 进行 token 验证
 *
 * @author xiaomi
 */
@Configuration
@ConditionalOnClass(name = "xyz.migoo.framework.security.core.authentication.AuthUserDetailsFetcher")
@ConditionalOnBean(AuthUserDetailsFetcher.class)
public class WebSocketSecurityConfiguration {

    /**
     * WebSocket 认证拦截器（集成 security 模块）
     */
    @Bean
    public WebSocketAuthInterceptor webSocketAuthInterceptor(WebSocketProperties webSocketProperties,
                                                             AuthUserDetailsFetcher<? extends AuthUserDetails<?, ?>> userDetailsFetcher) {
        return new WebSocketAuthInterceptor(webSocketProperties, userDetailsFetcher);
    }

}
