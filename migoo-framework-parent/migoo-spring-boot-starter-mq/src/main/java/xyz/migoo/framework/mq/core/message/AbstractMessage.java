package xyz.migoo.framework.mq.core.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Redis 消息抽象基类
 *
 */
@Data
public abstract class AbstractMessage {

    /**
     * 头
     */
    private Map<String, String> headers = new HashMap<>();

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