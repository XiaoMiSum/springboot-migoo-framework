package xyz.migoo.framework.common.id;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.LongSupplier;

/**
 * UUIDv7 分布式 ID 生成器（RFC 9562），零协调、零外部依赖
 *
 * <p>布局：{@code [48bit 毫秒时间戳][4bit 版本=7][12bit 毫秒内计数][2bit 变体=10][62bit 随机]}，
 * 时间字段在前 → <b>字典序即时间序</b>（B-Tree 主键插入友好）。</p>
 *
 * <p>有序性策略（RFC 9562 §5.7 方法 3 的简化变体）：</p>
 * <ul>
 *     <li>同毫秒内：12 位计数器递增，严格单调；计数溢出自旋到下一毫秒；</li>
 *     <li>时钟回拨：沿用上次时间戳继续递增计数，不倒退、不重复；</li>
 *     <li>不同毫秒：时间字段天然递增，计数器归零。</li>
 * </ul>
 *
 * <p>线程安全：{@code synchronized}，ID 生成的临界区极短，锁竞争可忽略。</p>
 *
 * @author xiaomi
 */
public class UuidV7IdGenerator implements IdGenerator {

    /**
     * 版本号 7：位于 msb 的第 16..12 位之上的 4 位（即第 15..12 位）
     */
    private static final long VERSION_7 = 0x7L << 12;

    /**
     * 毫秒内 12 位计数器上限（0 ~ 4095）
     */
    private static final long MAX_COUNTER = 0xFFF;

    /**
     * 时钟源（测试可注入）
     */
    private final LongSupplier timeSource;

    /**
     * 上次使用的时间戳（毫秒）
     */
    private long lastTimestamp = -1L;

    /**
     * 毫秒内计数器
     */
    private long counter;

    public UuidV7IdGenerator() {
        this(System::currentTimeMillis);
    }

    /**
     * 指定时钟源的构造器（包级可见，测试用）
     *
     * @param timeSource 毫秒时间源
     */
    UuidV7IdGenerator(LongSupplier timeSource) {
        this.timeSource = timeSource;
    }

    /**
     * 生成下一个 UUIDv7
     *
     * @return UUIDv7（version=7、variant=RFC-4122）
     */
    public synchronized UUID nextUuid() {
        long now = timeSource.getAsLong();
        if (now <= lastTimestamp) {
            // 同毫秒或时钟回拨：沿用上次时间戳，计数器递增（不倒退、不重复）
            if (++counter > MAX_COUNTER) {
                now = waitNextMillis(lastTimestamp);
                counter = 0;
            } else {
                now = lastTimestamp;
            }
        } else {
            counter = 0;
        }
        lastTimestamp = now;

        // [48bit 时间戳][4bit 版本=7][12bit 计数]
        long mostSignificantBits = (now << 16) | VERSION_7 | counter;
        // [2bit 变体=10][62bit 随机]
        long leastSignificantBits = (ThreadLocalRandom.current().nextLong() & ~(3L << 62)) | (2L << 62);
        return new UUID(mostSignificantBits, leastSignificantBits);
    }

    @Override
    public String nextId() {
        return nextUuid().toString();
    }

    /**
     * 自旋等待时间戳超过已使用的最大时间戳
     * <p>同时覆盖时钟回拨：等到时间重新超过 {@code last} 才继续，避免复用已发出的时间位。</p>
     *
     * @param last 已使用的最大时间戳
     * @return 新的时间戳
     */
    private long waitNextMillis(long last) {
        long now = timeSource.getAsLong();
        while (now <= last) {
            Thread.onSpinWait();
            now = timeSource.getAsLong();
        }
        return now;
    }
}
