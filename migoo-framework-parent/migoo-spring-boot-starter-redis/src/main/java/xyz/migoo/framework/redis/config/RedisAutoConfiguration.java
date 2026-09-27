package xyz.migoo.framework.redis.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author xiaomi
 * Created on 2021/11/21 14:05
 */
@AutoConfiguration(before = DataRedisAutoConfiguration.class)
@Slf4j
public class RedisAutoConfiguration {


    /**
     * 创建 RedisTemplate Bean，使用 JSON 序列化方式
     * <p>
     * 应用自定义 {@code redisTemplate} Bean 时本默认值不生效（按 Bean 名称回退）
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        // 创建 RedisTemplate 对象
        var template = new RedisTemplate<String, Object>();
        // 设置 RedisConnection 工厂。😈 它就是实现多种 Java Redis 客户端接入的秘密工厂。感兴趣的胖友，可以自己去撸下。
        template.setConnectionFactory(factory);
        // 使用 String 序列化方式，序列化 KEY 。
        template.setKeySerializer(RedisSerializer.string());
        template.setHashKeySerializer(RedisSerializer.string());

        // 使用 String 序列化器，手动通过 Jackson 进行 JSON 序列化
        // 优点：存储的是纯 JSON 字符串，无类型元数据，Redis CLI 可读
        // 缺点：读取时需要手动反序列化（通过 RedisKit/JsonUtils 处理）
        var serializer = new StringRedisSerializer();

        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        template.setStringSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
}