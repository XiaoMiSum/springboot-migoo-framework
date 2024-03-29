package xyz.migoo.framework.mq.core.stream;

import cn.hutool.core.util.TypeUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import xyz.migoo.framework.common.util.json.JsonUtils;

import java.lang.reflect.Type;

/**
 * @author xiaomi
 * Created on 2021/11/21 14:19
 */
public abstract class AbstractStreamMessageListener<T extends StreamMessage> implements StreamListener<String, ObjectRecord<String, String>> {

    /**
     * 消息类型
     */
    private final Class<T> messageType;
    /**
     * Redis Channel
     */
    @Getter
    private final String streamKey;

    /**
     * Redis 消费者分组，默认使用 migoo.mq.group 名字
     */
    @Value("${migoo.mq.group:def_group}")
    @Getter
    private String group;
    /**
     *
     */
    @Setter
    private RedisTemplate<String, ?> redisTemplate;

    @SneakyThrows
    protected AbstractStreamMessageListener() {
        this.messageType = getMessageClass();
        this.streamKey = messageType.getConstructor().newInstance().getStreamKey();
    }

    @Override
    public void onMessage(ObjectRecord<String, String> message) {
        // 消费消息
        T messageObj = JsonUtils.parseObject(message.getValue(), messageType);
        this.onMessage(messageObj);
        // ack 消息消费完成
        redisTemplate.opsForStream().acknowledge(group, message);
        redisTemplate.opsForStream().delete(message);
        // TODO: 2021/11/21
        // 1. 处理异常的情况
        // 2. 发送日志；以及事务的结合
        // 3. 消费日志；以及通用的幂等性
        // 4. 消费失败的重试，https://zhuanlan.zhihu.com/p/60501638
    }

    /**
     * 处理消息
     *
     * @param message 消息
     */
    public abstract void onMessage(T message);

    /**
     * 通过解析类上的泛型，获得消息类型
     *
     * @return 消息类型
     */
    @SuppressWarnings("unchecked")
    private Class<T> getMessageClass() {
        Type type = TypeUtil.getTypeArgument(getClass(), 0);
        if (type == null) {
            throw new IllegalStateException(String.format("类型(%s) 需要设置消息类型", getClass().getName()));
        }
        return (Class<T>) type;
    }

}
