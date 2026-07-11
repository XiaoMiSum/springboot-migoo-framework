package xyz.migoo.framework.mq.core.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Redis 消息抽象基类
 *
 */
@Data
public abstract class AbstractMessage {

    /**
     * 消息唯一标识，用于幂等性处理
     */
    private String messageId;

    /**
     * 头
     */
    private Map<String, String> headers = new HashMap<>();

    public AbstractMessage() {
        // 自动生成消息ID
        this.messageId = UUID.randomUUID().toString();
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    /**
     * 获得 Redis Channel
     *
     * @return Channel
     */
    @JsonIgnore
    public String getChannel() {
        return getClass().getSimpleName();
    }


}