package xyz.migoo.framework.common.id;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link SnowflakeIdGenerator} 单元测试
 *
 * <p>覆盖严格递增唯一性、64 位布局（时间戳/机房/机器/序列）、ID 取值范围校验、
 * 时钟回拨抛错、同毫秒序列递增、字符串形态。</p>
 */
class SnowflakeIdGeneratorTest {

    /**
     * 布局位移（测试侧按文档口径独立编码，不依赖实现常量）
     */
    private static final int TIMESTAMP_SHIFT = 22;
    private static final int DATACENTER_SHIFT = 17;
    private static final int WORKER_SHIFT = 12;
    private static final int SEQUENCE_MASK = 0xFFF;

    @Test
    void generatesStrictlyIncreasingIds() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 2);
        long previous = 0L;

        for (int i = 0; i < 100_000; i++) {
            long id = generator.nextLong();
            assertThat(id).isPositive();
            // 严格递增同时蕴含唯一
            assertThat(id).isGreaterThan(previous);
            previous = id;
        }
    }

    @Test
    void encodesConfiguredIdsIntoBitLayout() {
        long before = System.currentTimeMillis();
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(3, 5);

        long id = generator.nextLong();

        long after = System.currentTimeMillis();
        // [1bit 符号=0][41bit 时间戳][5bit 机房][5bit 机器][12bit 序列]
        assertThat(id).isPositive();
        assertThat(SnowflakeIdGenerator.EPOCH + (id >>> TIMESTAMP_SHIFT)).isBetween(before, after);
        assertThat((id >>> DATACENTER_SHIFT) & 0x1FL).isEqualTo(3L);
        assertThat((id >>> WORKER_SHIFT) & 0x1FL).isEqualTo(5L);
        assertThat(id & SEQUENCE_MASK).isBetween(0L, 4_095L);
    }

    @Test
    void defaultsToZeroDatacenterAndWorker() {
        long id = new SnowflakeIdGenerator().nextLong();

        assertThat((id >>> DATACENTER_SHIFT) & 0x1FL).isZero();
        assertThat((id >>> WORKER_SHIFT) & 0x1FL).isZero();
    }

    @Test
    void rejectsOutOfRangeDatacenterId() {
        assertThatThrownBy(() -> new SnowflakeIdGenerator(32, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("datacenterId");
        assertThatThrownBy(() -> new SnowflakeIdGenerator(-1, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("datacenterId");
    }

    @Test
    void rejectsOutOfRangeWorkerId() {
        assertThatThrownBy(() -> new SnowflakeIdGenerator(0, 32))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("workerId");
        assertThatThrownBy(() -> new SnowflakeIdGenerator(0, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("workerId");
    }

    @Test
    void nextIdReturnsDecimalString() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator();

        String id = generator.nextId();

        assertThat(id).matches("\\d+");
        assertThat(Long.parseLong(id)).isPositive();
    }

    @Test
    void sequenceIncrementsWithinSameMillisecond() {
        AtomicLong clock = new AtomicLong(SnowflakeIdGenerator.EPOCH + 1_000_000L);
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(0, 0, clock::get);

        long first = generator.nextLong();
        long second = generator.nextLong();

        // 同毫秒：时间位不变，序列 +1
        assertThat(first >>> TIMESTAMP_SHIFT).isEqualTo(second >>> TIMESTAMP_SHIFT);
        assertThat(second - first).isEqualTo(1L);
    }

    @Test
    void throwsOnClockRollback() {
        AtomicLong clock = new AtomicLong(SnowflakeIdGenerator.EPOCH + 1_000_000L);
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(0, 0, clock::get);
        generator.nextLong();

        // 时钟回拨：拒绝发号而非静默发出重复/倒退 ID
        clock.set(SnowflakeIdGenerator.EPOCH + 500_000L);
        assertThatThrownBy(generator::nextLong)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("回拨");
    }
}
