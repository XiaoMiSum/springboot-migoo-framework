package xyz.migoo.framework.web.core.store;

import java.time.Duration;

/**
 * 带过期时间的计数/状态存储接口（SPI）
 * <p>
 * 为限流计数、登录失败计数、账号锁定状态等场景提供计数与状态存取能力。
 * <p>
 * 框架默认提供内存实现（单机），检测到 Redis 时自动切换为 Redis 实现（多实例共享），
 * 应用也可自行实现此接口并注册为 Bean 覆盖默认行为。
 *
 * @author xiaomi
 */
public interface StateStore {

    /**
     * 计数自增，并刷新过期时间
     * <p>
     * key 不存在时初始化为 1 并设置过期时间；已存在时自增并重置过期时间。
     * 「每次刷新」的语义用于支持"距最后一次事件超过 ttl 则重新计数"的场景（如连续失败计数）。
     *
     * @param key 键
     * @param ttl 过期时间
     * @return 自增后的计数值
     */
    long increment(String key, Duration ttl);

    /**
     * 读取计数值
     *
     * @param key 键
     * @return 计数值，不存在或已过期返回 0
     */
    long get(String key);

    /**
     * 写入计数值
     *
     * @param key   键
     * @param value 计数值
     * @param ttl   过期时间
     */
    void put(String key, long value, Duration ttl);

    /**
     * 删除键
     *
     * @param key 键
     */
    void delete(String key);
}
