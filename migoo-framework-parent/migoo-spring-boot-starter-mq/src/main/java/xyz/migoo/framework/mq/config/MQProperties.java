package xyz.migoo.framework.mq.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * MQ 配置属性
 */
@Data
@ConfigurationProperties(prefix = "migoo.mq")
public class MQProperties {

    /**
     * 消费者组名称
     */
    private String group = "def_group";

    /**
     * 最大重试次数
     */
    private Integer maxRetry = 3;

    /**
     * 是否启用死信队列
     */
    private Boolean deadLetterEnabled = true;

    /**
     * 消费成功后是否删除 Stream 消息（保留用于审计追踪）
     */
    private Boolean deleteAfterAck = false;

    /**
     * 幂等性配置
     */
    private Idempotent idempotent = new Idempotent();

    @Data
    public static class Idempotent {

        /**
         * 是否启用幂等性检查
         */
        private Boolean enabled = true;

        /**
         * 幂等键过期时间，默认24小时
         * <p>
         * 过期后相同 messageId 的消息可以被重新消费
         */
        private Duration expireTime = Duration.ofHours(24);
    }
}
