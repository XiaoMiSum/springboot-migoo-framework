package xyz.migoo.framework.common.observability;

/**
 * MQ 消息死信事件（可观测性信号）
 *
 * <p>发出点：mq 组件 {@code AbstractStreamMessageListener} 重试耗尽、投递死信队列处。</p>
 *
 * @param stream Stream Key（低基数，可作 tag）
 * @param reason 触发原因（异常类型简单名，低基数，可作 tag）
 * @author xiaomi
 */
public record MqMessageDeadLetteredEvent(String stream, String reason) {
}
