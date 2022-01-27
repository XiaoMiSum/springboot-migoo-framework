package xyz.migoo.framework.mq.core.stream;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author xiaomi
 * Created on 2021/11/21 14:21
 */
public interface StreamMessage {

    /**
     * 获得 Redis Stream Key
     *
     * @return Channel
     */
    @JsonIgnore
    String getStreamKey();
}
