package xyz.migoo.framework.common.id;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongSupplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link UuidV7IdGenerator} 单元测试
 *
 * <p>覆盖 RFC 9562 版本/变体、唯一性、字典序即时间序、时钟回拨不倒退、
 * 计数溢出自旋到下一毫秒。</p>
 */
class UuidV7IdGeneratorTest {

    private final UuidV7IdGenerator generator = new UuidV7IdGenerator();

    @Test
    void generatesVersion7AndRfcVariant() {
        for (int i = 0; i < 100; i++) {
            UUID uuid = generator.nextUuid();
            assertThat(uuid.version()).isEqualTo(7);
            // RFC 4122 变体（二进制 10x）
            assertThat(uuid.variant()).isEqualTo(2);
        }
    }

    @Test
    void nextIdIsUuidString() {
        String id = generator.nextId();

        assertThat(id).hasSize(36);
        assertThat(UUID.fromString(id).version()).isEqualTo(7);
    }

    @Test
    void generatesUniqueIds() {
        Set<String> ids = new HashSet<>();

        for (int i = 0; i < 10_000; i++) {
            ids.add(generator.nextId());
        }

        assertThat(ids).hasSize(10_000);
    }

    @Test
    void lexicographicOrderIsTimeOrdered() {
        String previous = null;

        for (int i = 0; i < 10_000; i++) {
            String id = generator.nextId();
            if (previous != null) {
                // 同毫秒计数递增、跨毫秒时间字段递增 → 严格字典序递增
                assertThat(id).isGreaterThan(previous);
            }
            previous = id;
        }
    }

    @Test
    void clockRollbackKeepsUsingLastTimestamp() {
        FakeClock clock = new FakeClock(1_000_000L);
        UuidV7IdGenerator generator = new UuidV7IdGenerator(clock);

        UUID first = generator.nextUuid();
        clock.set(500_000L);
        UUID second = generator.nextUuid();

        // 回拨不倒退：沿用上次时间戳，时间位不变、毫秒内计数递增
        assertThat(second.getMostSignificantBits()).isGreaterThan(first.getMostSignificantBits());
        assertThat(second.getMostSignificantBits() >>> 16)
                .isEqualTo(first.getMostSignificantBits() >>> 16);
        assertThat(second.version()).isEqualTo(7);
    }

    @Test
    void sequenceOverflowSpinsToNextMillis() {
        long base = 1_700_000_000_000L;
        // 前 4101 次读时钟返回 base（发号 4097 次时 12 位计数溢出），
        // 之后推进到 base + 1，自旋等待得以结束
        AtomicInteger reads = new AtomicInteger();
        LongSupplier clock = () -> reads.incrementAndGet() <= 4101 ? base : base + 1;
        UuidV7IdGenerator generator = new UuidV7IdGenerator(clock);

        String lastId = null;
        for (int i = 0; i < 4_097; i++) {
            lastId = generator.nextId();
        }

        UUID uuid = UUID.fromString(lastId);
        // 计数耗尽后等到下一毫秒：时间位推进、计数归零
        assertThat(uuid.getMostSignificantBits() >>> 16).isEqualTo(base + 1);
        assertThat(uuid.version()).isEqualTo(7);
    }

    /**
     * 可控毫秒时钟（测试用）
     */
    private static final class FakeClock implements LongSupplier {

        private long now;

        private FakeClock(long now) {
            this.now = now;
        }

        @Override
        public long getAsLong() {
            return now;
        }

        private void set(long now) {
            this.now = now;
        }
    }
}
