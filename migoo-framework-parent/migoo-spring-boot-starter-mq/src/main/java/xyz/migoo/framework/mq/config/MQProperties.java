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

    /**
     * 健康检查配置（/actuator/health 的消费组积压指示器）
     */
    private Health health = new Health();

    /**
     * PEL 认领与退避重投配置
     */
    private Reclaim reclaim = new Reclaim();

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

    @Data
    public static class Health {

        /**
         * 是否启用消费组 PEL 积压健康检查（由 MQHealthAutoConfiguration 读取）
         */
        private Boolean enabled = true;

        /**
         * PEL 积压阈值（条）：任一消费组积压超过该值时 /actuator/health 返回 OUT_OF_SERVICE
         */
        private long backlogThreshold = 1000L;
    }

    @Data
    public static class Reclaim {

        /**
         * 是否启用 PEL 消息认领与退避重投（StreamReclaimTask）：
         * 消费失败的消息与消费者崩溃后的孤儿消息，闲置超过退避时长后认领重投
         */
        private Boolean enabled = true;

        /**
         * 退避时长：消息在 PEL 中闲置超过该时长才会被认领重投，
         * 应大于业务最长处理时长（否则处理中的消息可能被重复认领）
         */
        private Duration backoff = Duration.ofSeconds(5);

        /**
         * 认领轮询间隔（实际重投延迟 ≈ backoff ~ backoff + interval）
         */
        private Duration interval = Duration.ofSeconds(5);
    }
}
