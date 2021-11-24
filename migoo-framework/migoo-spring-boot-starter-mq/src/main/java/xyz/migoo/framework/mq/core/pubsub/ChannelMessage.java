package xyz.migoo.framework.mq.core.pubsub;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author xiaomi
 * Created on 2021/11/21 14:16
 */
public interface ChannelMessage {

    /**
     * 获得 Redis Channel
     *
     * @return Channel
     */
    // 避免序列化
    @JsonIgnore
    String getChannel();
}
