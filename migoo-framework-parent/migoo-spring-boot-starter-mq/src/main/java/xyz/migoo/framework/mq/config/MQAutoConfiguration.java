package xyz.migoo.framework.mq.config;

import cn.hutool.system.SystemUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import xyz.migoo.framework.mq.core.pubsub.AbstractChannelMessageListener;
import xyz.migoo.framework.mq.core.stream.AbstractStreamMessageListener;
import xyz.migoo.framework.redis.config.RedisAutoConfiguration;

import java.util.List;

/**
 * @author xiaomi
 * Created on 2021/11/21 14:13
 */
@AutoConfigureAfter(RedisAutoConfiguration.class)
@Slf4j
public class MQAutoConfiguration {

    /**
     * 创建 Redis Pub/Sub 广播消费的容器
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory factory, List<AbstractChannelMessageListener<?>> listeners) {
        // 创建 RedisMessageListenerContainer 对象
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        // 设置 RedisConnection 工厂。
        container.setConnectionFactory(factory);
        // 添加监听器
        listeners.forEach(listener -> {
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
    public StreamMessageListenerContainer<String, ObjectRecord<String, String>> redisStreamMessageListenerContainer(
            RedisTemplate<String, Object> redisTemplate, List<AbstractStreamMessageListener<?>> listeners) {
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
//        String consumerName = "110";
        listeners.forEach(listener -> {
            // 创建 listener 对应的消费者分组
            try {
                redisTemplate.opsForStream().createGroup(listener.getStreamKey(), listener.getGroup());
            } catch (Exception ignore) {
            }
            // 设置 listener 对应的 redisTemplate
            listener.setRedisTemplate(redisTemplate);
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
            log.info("[redisStreamMessageListenerContainer][注册 Stream({}) 对应的监听器({})]",
                    listener.getStreamKey(), listener.getClass().getName());
        });
        return container;
    }

    /**
     * 构建消费者名字，使用本地 IP + 进程编号的方式。
     * 参考自 RocketMQ clientId 的实现
     *
     * @return 消费者名字
     */
    private static String buildConsumerName() {
        return String.format("%s@%d", SystemUtil.getHostInfo().getAddress(), SystemUtil.getCurrentPID());
    }
}
