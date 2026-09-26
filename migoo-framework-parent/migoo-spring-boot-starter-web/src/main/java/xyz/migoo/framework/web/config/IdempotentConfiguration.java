package xyz.migoo.framework.web.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.migoo.framework.web.core.idempotent.IdempotentAspect;
import xyz.migoo.framework.web.core.store.StateStore;

/**
 * 幂等配置
 * <p>
 * 注册幂等切面，存储复用 {@link StateStore}（由 {@link RateLimitConfiguration} 注册，
 * 单机内存实现，检测到 Redis 自动切换为多实例共享）。
 *
 * @author xiaomi
 */
@Configuration
public class IdempotentConfiguration {

    /**
     * 幂等切面 Bean（migoo.web.idempotent.enabled 控制，默认开启）
     */
    @Bean
    @ConditionalOnMissingBean(IdempotentAspect.class)
    @ConditionalOnProperty(name = "migoo.web.idempotent.enabled", havingValue = "true", matchIfMissing = true)
    public IdempotentAspect idempotentAspect(StateStore stateStore) {
        return new IdempotentAspect(stateStore);
    }
}
