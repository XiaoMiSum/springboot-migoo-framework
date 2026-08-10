package xyz.migoo.framework.common.util;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CacheUtils 单元测试
 * <p>
 * 说明：refreshAfterWrite 是异步刷新的、与时间相关，因此本测试不断言任何
 * refresh 行为，只验证首次加载与命中缓存，保证测试确定性与时间安全。
 */
class CacheUtilsTest {

    @Test
    void buildAsyncReloadingCache_returnsWorkingLoadingCache() {
        LoadingCache<String, String> cache = CacheUtils.buildAsyncReloadingCache(
                Duration.ofHours(1), CacheLoader.from(key -> "value-" + key));
        assertThat(cache.getUnchecked("k1")).isEqualTo("value-k1");
        assertThat(cache.getUnchecked("k2")).isEqualTo("value-k2");
    }

    @Test
    void get_loadsOnceAndThenServesFromCache() {
        AtomicInteger loadCount = new AtomicInteger();
        CacheLoader<String, String> loader = new CacheLoader<>() {
            @Override
            public String load(String key) {
                loadCount.incrementAndGet();
                return "loaded-" + key;
            }
        };
        LoadingCache<String, String> cache = CacheUtils.buildAsyncReloadingCache(Duration.ofHours(1), loader);

        assertThat(cache.getUnchecked("key")).isEqualTo("loaded-key");
        assertThat(cache.getUnchecked("key")).isEqualTo("loaded-key");
        // 第二次 get 命中缓存，loader 不应再次被调用
        assertThat(loadCount).hasValue(1);
    }
}
