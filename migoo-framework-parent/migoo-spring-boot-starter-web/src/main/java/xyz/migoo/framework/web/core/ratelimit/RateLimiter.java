package xyz.migoo.framework.web.core.ratelimit;

import java.time.Duration;

/**
 * 限流器接口
 * <p>
 * 固定窗口计数限流。应用可自行实现并注册为 Bean 覆盖默认行为。
 *
 * @author xiaomi
 */
public interface RateLimiter {

    /**
     * 尝试获取一次调用配额
     *
     * @param key    限流 key（调用方保证维度唯一，如 类#方法:IP）
     * @param limit  窗口内允许的最大次数
     * @param window 统计窗口时长
     * @return true-放行，false-超出限制
     */
    boolean tryAcquire(String key, int limit, Duration window);
}
