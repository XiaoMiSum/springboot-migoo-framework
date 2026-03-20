package xyz.migoo.framework.mq.core.interceptor;

import xyz.migoo.framework.mq.core.message.AbstractMessage;

/**
 * {@link AbstractMessage} 消息拦截器
 * 通过拦截器，作为插件机制，实现拓展。
 * 例如说，多租户场景下的 MQ 消息处理
 *
 */
public interface RedisMessageInterceptor {

    /**
     * 消息发送前拦截
     *
     * @param message 消息
     */
    default void sendMessageBefore(AbstractMessage message) {
    }

    /**
     * 消息发送后拦截
     *
     * @param message 消息
     */
    default void sendMessageAfter(AbstractMessage message) {
    }

    /**
     * 消息发送异常拦截
     *
     * @param message   消息
     * @param throwable 异常
     */
    default void sendMessageError(AbstractMessage message, Throwable throwable) {
    }

    /**
     * 消息消费前拦截
     *
     * @param message 消息
     */
    default void consumeMessageBefore(AbstractMessage message) {
    }

    /**
     * 消息消费后拦截
     *
     * @param message 消息
     */
    default void consumeMessageAfter(AbstractMessage message) {
    }

    /**
     * 消息消费异常拦截
     *
     * @param message   消息
     * @param throwable 异常
     */
    default void consumeMessageError(AbstractMessage message, Throwable throwable) {
    }

}