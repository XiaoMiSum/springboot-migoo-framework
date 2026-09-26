package xyz.migoo.framework.web.core.store;

import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 内存计数/状态存储（默认实现）
 * <p>
 * 适用于单实例部署；多实例部署请使用 Redis 实现（引入 migoo-spring-boot-starter-redis 后自动装配）。
 * <p>
 * 过期采用惰性清理（读写时判断）+ 周期性全量清理（每 {@value #CLEANUP_INTERVAL} 次写操作触发一次），
 * 避免长期运行时已过期条目堆积。
 *
 * @author xiaomi
 */
public class InMemoryStateStore implements StateStore {

    /**
     * 每写操作多少次触发一次全量过期清理
     */
    private static final long CLEANUP_INTERVAL = 1024;

    private final Map<String, Entry> entries = new ConcurrentHashMap<>();

    private final AtomicLong writeCounter = new AtomicLong();

    @Override
    public long increment(String key, Duration ttl) {
        long now = System.currentTimeMillis();
        long expireAt = now + ttl.toMillis();
        Entry entry = entries.compute(key, (k, old) -> {
            if (old == null || old.isExpired(now)) {
                return new Entry(1, expireAt);
            }
            old.value++;
            old.expireAt = expireAt;
            return old;
        });
        maybeCleanup();
        return entry.value;
    }

    @Override
    public long get(String key) {
        Entry entry = entries.get(key);
        if (entry == null) {
            return 0;
        }
        if (entry.isExpired(System.currentTimeMillis())) {
            entries.remove(key, entry);
            return 0;
        }
        return entry.value;
    }

    @Override
    public void put(String key, long value, Duration ttl) {
        entries.put(key, new Entry(value, System.currentTimeMillis() + ttl.toMillis()));
        maybeCleanup();
    }

    @Override
    public boolean setIfAbsent(String key, Duration ttl) {
        long now = System.currentTimeMillis();
        boolean[] created = new boolean[1];
        // compute 按键原子执行：不存在/已过期才占位
        entries.compute(key, (k, old) -> {
            if (old == null || old.isExpired(now)) {
                created[0] = true;
                return new Entry(1, now + ttl.toMillis());
            }
            return old;
        });
        maybeCleanup();
        return created[0];
    }

    @Override
    public void delete(String key) {
        entries.remove(key);
    }

    private void maybeCleanup() {
        if (writeCounter.incrementAndGet() % CLEANUP_INTERVAL != 0) {
            return;
        }
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, Entry>> iterator = entries.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue().isExpired(now)) {
                iterator.remove();
            }
        }
    }

    /**
     * 存储条目
     */
    private static final class Entry {

        private volatile long value;

        private volatile long expireAt;

        private Entry(long value, long expireAt) {
            this.value = value;
            this.expireAt = expireAt;
        }

        private boolean isExpired(long now) {
            return expireAt <= now;
        }
    }
}
