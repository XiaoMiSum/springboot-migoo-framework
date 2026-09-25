package xyz.migoo.framework.web.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.migoo.framework.web.core.ratelimit.DefaultRateLimiter;
import xyz.migoo.framework.web.core.ratelimit.RateLimitAspect;
import xyz.migoo.framework.web.core.ratelimit.RateLimiter;
import xyz.migoo.framework.web.core.store.InMemoryStateStore;
import xyz.migoo.framework.web.core.store.StateStore;

/**
 * 限流配置
 * <p>
 * 注册计数/状态存储（默认内存实现）、限流器与限流切面。
 * 引入 migoo-spring-boot-starter-redis 时，由 {@link MiGooWebRedisStateStoreAutoConfiguration}
 * 在本配置之后将存储自动切换为 Redis 实现（多实例共享）。
 *
 * @author xiaomi
 */
@Configuration
public class RateLimitConfiguration {

    /**
     * 计数/状态存储 Bean（默认内存实现，单机开箱即用）
     * <p>
     * 应用也可注册自己的 {@link StateStore} Bean 覆盖
     */
    @Bean
    @ConditionalOnMissingBean(StateStore.class)
    public StateStore stateStore() {
        return new InMemoryStateStore();
    }

    /**
     * 限流器 Bean
     */
    @Bean
    @ConditionalOnMissingBean(RateLimiter.class)
    public RateLimiter rateLimiter(StateStore stateStore) {
        return new DefaultRateLimiter(stateStore);
    }

    /**
     * 限流切面 Bean（migoo.web.rate-limit.enabled 控制，默认开启）
     */
    @Bean
    @ConditionalOnMissingBean(RateLimitAspect.class)
    @ConditionalOnProperty(name = "migoo.web.rate-limit.enabled", havingValue = "true", matchIfMissing = true)
    public RateLimitAspect rateLimitAspect(RateLimiter rateLimiter) {
        return new RateLimitAspect(rateLimiter);
    }
}
