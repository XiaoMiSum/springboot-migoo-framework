package xyz.migoo.framework.mq.core.interceptor;

import xyz.migoo.framework.mq.core.message.AbstractMessage;

import java.util.List;
import java.util.Objects;

/**
 * Redis 消息拦截器工具类
 * <p>
 * 提供拦截器链的统一调用逻辑，供 Channel 和 Stream 监听器复用
 */
public final class RedisMessageInterceptorUtils {

    private RedisMessageInterceptorUtils() {
    }

    /**
     * 调用消费前拦截器（正序）
     *
     * @param interceptors 拦截器列表
     * @param message      消息
     */
    public static void consumeMessageBefore(List<RedisMessageInterceptor> interceptors, AbstractMessage message) {
        interceptors.forEach(interceptor -> interceptor.consumeMessageBefore(message));
    }

    /**
     * 调用消费后拦截器（倒序）
     *
     * @param interceptors 拦截器列表
     * @param message      消息
     */
    public static void consumeMessageAfter(List<RedisMessageInterceptor> interceptors, AbstractMessage message) {
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            interceptors.get(i).consumeMessageAfter(message);
        }
    }

    /**
     * 调用消费异常拦截器（倒序）
     *
     * @param interceptors 拦截器列表
     * @param message      消息
     * @param throwable    异常
     */
    public static void consumeMessageError(List<RedisMessageInterceptor> interceptors, AbstractMessage message, Throwable throwable) {
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            interceptors.get(i).consumeMessageError(message, throwable);
        }
    }
}
