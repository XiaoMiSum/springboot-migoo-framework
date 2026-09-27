package xyz.migoo.framework.web.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import xyz.migoo.framework.web.core.store.RedisStateStore;
import xyz.migoo.framework.web.core.store.StateStore;

/**
 * Redis 计数/状态存储自动配置
 * <p>
 * 检测到 Redis（spring-data-redis）时，将默认的内存 {@link StateStore} 替换为
 * {@link RedisStateStore}，使限流计数、登录失败计数、账号锁定状态在多实例间共享。
 * <p>
 * 需在 {@link MiGooWebAutoConfiguration} 之后装配，以保证内存默认值先生效、本配置按需覆盖；
 * 应用自定义 {@link StateStore} Bean 时两者均不生效。
 *
 * @author xiaomi
 */
@AutoConfiguration
@AutoConfigureAfter(MiGooWebAutoConfiguration.class)
@ConditionalOnClass(RedisConnectionFactory.class)
public class MiGooWebRedisStateStoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(StateStore.class)
    public StateStore redisStateStore(RedisConnectionFactory connectionFactory) {
        return new RedisStateStore(connectionFactory);
    }
}
