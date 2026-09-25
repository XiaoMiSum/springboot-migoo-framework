package xyz.migoo.framework.common.observability;

/**
 * MQ 消息消费失败事件（可观测性信号）
 *
 * <p>发出点：mq 组件 {@code AbstractStreamMessageListener#handleConsumeError}。</p>
 *
 * @param stream    Stream Key（低基数，可作 tag）
 * @param willRetry 是否还会重试（未达最大重试次数，低基数，可作 tag）
 * @author xiaomi
 */
public record MqMessageConsumeFailedEvent(String stream, boolean willRetry) {
}
