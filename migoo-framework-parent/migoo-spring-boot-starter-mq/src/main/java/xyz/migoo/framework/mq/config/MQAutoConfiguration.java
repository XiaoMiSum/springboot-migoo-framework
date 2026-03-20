package xyz.migoo.framework.mq.config;

import cn.hutool.system.SystemUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import xyz.migoo.framework.mq.core.RedisMQTemplate;
import xyz.migoo.framework.mq.core.interceptor.RedisMessageInterceptor;
import xyz.migoo.framework.mq.core.pubsub.AbstractChannelMessageListener;
import xyz.migoo.framework.mq.core.stream.AbstractStreamMessageListener;
import xyz.migoo.framework.redis.config.RedisAutoConfiguration;

import java.util.List;

/**
 * MQ 自动配置类
 * <p>
 * 支持 Redis Pub/Sub（广播模式）和 Redis Stream（集群消费模式）
 */
@AutoConfigureAfter(RedisAutoConfiguration.class)
@Slf4j
public class MQAutoConfiguration {

    /**
     * 构建消费者名字，使用本地 IP + 进程编号的方式。
     * 参考自 RocketMQ clientId 的实现
     *
     * @return 消费者名字
     */
    private static String buildConsumerName() {
        return String.format("%s@%d", SystemUtil.getHostInfo().getAddress(), SystemUtil.getCurrentPID());
    }

    /**
     * 创建 RedisMQTemplate Bean
     *
     * @param redisTemplate Redis 操作模板
     * @param interceptors  消息拦截器列表（可选）
     * @return RedisMQTemplate
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisMQTemplate redisMQTemplate(RedisTemplate<String, ?> redisTemplate,
                                           @org.springframework.beans.factory.annotation.Autowired(required = false) List<RedisMessageInterceptor> interceptors) {
        RedisMQTemplate template = new RedisMQTemplate(redisTemplate);
        if (interceptors != null && !interceptors.isEmpty()) {
            interceptors.forEach(template::addInterceptor);
            log.info("[redisMQTemplate][注册 {} 个消息拦截器]", interceptors.size());
        }
        return template;
    }

    /**
     * 创建 Redis Pub/Sub 广播消费的容器
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnBean(AbstractChannelMessageListener.class)
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory factory,
                                                                       List<AbstractChannelMessageListener<?>> listeners,
                                                                       RedisMQTemplate redisMQTemplate) {
        // 创建 RedisMessageListenerContainer 对象
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        // 设置 RedisConnection 工厂。
        container.setConnectionFactory(factory);
        // 添加监听器
        listeners.forEach(listener -> {
            // 注入 RedisMQTemplate
            listener.setRedisMQTemplate(redisMQTemplate);
            container.addMessageListener(listener, new ChannelTopic(listener.getChannel()));
            log.info("[redisMessageListenerContainer][注册 Channel({}) 对应的监听器({})]",
                    listener.getChannel(), listener.getClass().getName());
        });
        return container;
    }

    /**
     * 创建 Redis Stream 集群消费的容器
     * <p>
     * Redis Stream 的 xreadgroup 命令：https://www.geek-book.com/src/docs/redis/redis/redis.io/commands/xreadgroup.html
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnBean(AbstractStreamMessageListener.class)
    public StreamMessageListenerContainer<String, ObjectRecord<String, String>> redisStreamMessageListenerContainer(
            RedisTemplate<String, Object> redisTemplate,
            List<AbstractStreamMessageListener<?>> listeners,
            RedisMQTemplate redisMQTemplate) {
        // 第一步，创建 StreamMessageListenerContainer 容器
        // 创建 options 配置
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, ObjectRecord<String, String>> containerOptions =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                        // 一次性最多拉取多少条消息
                        .batchSize(10)
                        // 目标类型。统一使用 String，通过自己封装的 AbstractStreamMessageListener 去反序列化
                        .targetType(String.class)
                        .build();
        // 创建 container 对象
        StreamMessageListenerContainer<String, ObjectRecord<String, String>> container = StreamMessageListenerContainer.create(
                redisTemplate.getRequiredConnectionFactory(), containerOptions);

        // 第二步，注册监听器，消费对应的 Stream 主题
        String consumerName = buildConsumerName();
        listeners.forEach(listener -> {
            // 创建 listener 对应的消费者分组
            createConsumerGroup(redisTemplate, listener.getStreamKey(), listener.getGroup());
            // 设置 listener 对应的 redisTemplate 和 redisMQTemplate
            listener.setRedisTemplate(redisTemplate);
            listener.setRedisMQTemplate(redisMQTemplate);
            // 创建 Consumer 对象
            Consumer consumer = Consumer.from(listener.getGroup(), consumerName);
            // 设置 Consumer 消费进度，以最小消费进度为准
            StreamOffset<String> streamOffset = StreamOffset.create(listener.getStreamKey(), ReadOffset.lastConsumed());
            // 设置 Consumer 监听
            StreamMessageListenerContainer.StreamReadRequestBuilder<String> builder = StreamMessageListenerContainer.StreamReadRequest
                    .builder(streamOffset).consumer(consumer)
                    // 不自动 ack
                    .autoAcknowledge(false)
                    // 默认配置，发生异常就取消消费，显然不符合预期；因此，我们设置为 false
                    .cancelOnError(throwable -> false);
            container.register(builder.build(), listener);
            log.info("[redisStreamMessageListenerContainer][注册 Stream({}) 对应的监听器({}), group={}, consumer={}]",
                    listener.getStreamKey(), listener.getClass().getName(), listener.getGroup(), consumerName);
        });
        return container;
    }

    /**
     * 创建消费者分组
     * <p>
     * 如果分组已存在，则忽略异常；如果是其他异常，记录错误日志
     *
     * @param redisTemplate Redis 操作模板
     * @param streamKey     Stream Key
     * @param group         消费者组名
     */
    private void createConsumerGroup(RedisTemplate<String, ?> redisTemplate, String streamKey, String group) {
        try {
            redisTemplate.opsForStream().createGroup(streamKey, group);
            log.info("[createConsumerGroup][创建消费者组成功] stream={}, group={}", streamKey, group);
        } catch (RedisSystemException e) {
            // BUSYGROUP Consumer Group name already exists
            String message = e.getMessage();
            if (message != null && message.contains("BUSYGROUP")) {
                log.debug("[createConsumerGroup][消费者组已存在] stream={}, group={}", streamKey, group);
            } else {
                log.warn("[createConsumerGroup][创建消费者组异常] stream={}, group={}, error={}",
                        streamKey, group, message);
            }
        } catch (Exception e) {
            log.error("[createConsumerGroup][创建消费者组失败] stream={}, group={}", streamKey, group, e);
        }
    }
}
