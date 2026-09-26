package xyz.migoo.framework.mq.config;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import xyz.migoo.framework.mq.core.stream.AbstractStreamMessageListener;
import xyz.migoo.framework.mq.health.MqBacklogHealthIndicator;

import java.util.List;

/**
 * MQ 健康检查自动配置
 *
 * <p>注册 {@link MqBacklogHealthIndicator}，把消费组 PEL 积压纳入 {@code /actuator/health}。
 * 条件：</p>
 * <ul>
 *     <li>{@code @ConditionalOnClass(name = ...)} —— actuator 不在 classpath 时整体跳过。
 *     本类注册于 {@code AutoConfiguration.imports}，类级条件在<b>类加载前</b>由 ASM 求值；
 *     且这里刻意用 {@code name} 字符串形式而非 class literal，任何元数据读取路径都不会
 *     触发对 actuator 类型的加载；</li>
 *     <li>{@code @ConditionalOnBean(AbstractStreamMessageListener.class)} —— 无 Stream 监听器时不注册
 *     （监听器是用户 Bean，先于自动配置注册，条件求值时已可见）；</li>
 *     <li>{@code @ConditionalOnProperty(migoo.mq.health.enabled)} —— 默认开启，可单独关闭。</li>
 * </ul>
 *
 * @author xiaomi
 * @see MqBacklogHealthIndicator
 */
@AutoConfigureAfter(MQAutoConfiguration.class)
@EnableConfigurationProperties(MQProperties.class)
@ConditionalOnClass(name = "org.springframework.boot.health.contributor.HealthIndicator")
@ConditionalOnBean(AbstractStreamMessageListener.class)
@ConditionalOnProperty(prefix = "migoo.mq.health", name = "enabled",
        havingValue = "true", matchIfMissing = true)
public class MQHealthAutoConfiguration {

    /**
     * MQ 消费组积压健康检查 Bean
     *
     * @param stringRedisTemplate Redis 模板（XPENDING 查询）
     * @param listeners           容器中的 Stream 监听器（提供 streamKey / group 维度）
     * @param properties          MQ 配置（读取 {@code migoo.mq.health.backlog-threshold}）
     * @return 积压健康检查
     */
    @Bean
    @ConditionalOnMissingBean(MqBacklogHealthIndicator.class)
    public MqBacklogHealthIndicator mqBacklogHealthIndicator(StringRedisTemplate stringRedisTemplate,
                                                             List<AbstractStreamMessageListener<?>> listeners,
                                                             MQProperties properties) {
        return new MqBacklogHealthIndicator(stringRedisTemplate, listeners,
                properties.getHealth().getBacklogThreshold());
    }

}
