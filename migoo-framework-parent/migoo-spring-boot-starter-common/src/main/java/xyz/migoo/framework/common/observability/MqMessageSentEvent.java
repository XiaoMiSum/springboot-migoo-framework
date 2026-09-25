package xyz.migoo.framework.common.observability;

/**
 * MQ 消息发送事件（可观测性信号）
 *
 * <p>发出点：mq 组件 {@code RedisMQTemplate#send}（Stream 与 Pub/Sub 两个重载均发出）。</p>
 *
 * @param stream Stream Key 或 Pub/Sub Channel（低基数，可作 tag）
 * @author xiaomi
 */
public record MqMessageSentEvent(String stream) {
}
