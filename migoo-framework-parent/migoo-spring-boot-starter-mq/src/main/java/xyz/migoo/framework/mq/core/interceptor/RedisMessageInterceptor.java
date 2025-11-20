package xyz.migoo.framework.mq.core.interceptor;

import xyz.migoo.framework.mq.core.message.AbstractMessage;

/**
 * {@link AbstractMessage} 消息拦截器
 * 通过拦截器，作为插件机制，实现拓展。
 * 例如说，多租户场景下的 MQ 消息处理
 *
 */
public interface RedisMessageInterceptor {

    default void sendMessageBefore(AbstractMessage message) {
    }

    default void sendMessageAfter(AbstractMessage message) {
    }

    default void consumeMessageBefore(AbstractMessage message) {
    }

    default void consumeMessageAfter(AbstractMessage message) {
    }

}