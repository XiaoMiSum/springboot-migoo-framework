package xyz.migoo.framework.common.id;

import java.util.function.LongSupplier;

/**
 * 雪花（Snowflake）分布式 ID 生成器
 *
 * <p>位布局（共 64 位，最高位恒为 0）：</p>
 * <pre>
 * | 1bit 符号 | 41bit 毫秒时间戳 | 5bit 机房 | 5bit 机器 | 12bit 序列 |
 * </pre>
 *
 * <p>时间戳纪元为 <b>2024-01-01T00:00:00Z</b>（41 位可用约 69 年）。
 * 多机部署须为每台机器分配唯一 {@code datacenterId}(0~31) 与 {@code workerId}(0~31)，
 * 框架不内置协调（避免反向引入 redis/zk 依赖），由使用方静态配置或部署环境注入。</p>
 *
 * <p>时钟回拨直接抛 {@link IllegalStateException}（回拨窗口内拒绝发号，
 * 由调用方重试；这是雪花方案的固有约束，优于静默发出重复/倒退 ID）。</p>
 *
 * <p>线程安全：{@code synchronized}。</p>
 *
 * @author xiaomi
 */
public class SnowflakeIdGenerator implements IdGenerator {

    /**
     * 自定义纪元：2024-01-01T00:00:00Z
     */
    static final long EPOCH = 1704067200000L;

    /**
     * 时间戳位数
     */
    private static final int TIMESTAMP_BITS = 41;

    /**
     * 机房位数
     */
    private static final int DATACENTER_BITS = 5;

    /**
     * 机器位数
     */
    private static final int WORKER_BITS = 5;

    /**
     * 序列位数
     */
    private static final int SEQUENCE_BITS = 12;

    /**
     * 机房 ID 上限（0 ~ 31）
     */
    private static final long MAX_DATACENTER_ID = (1L << DATACENTER_BITS) - 1;

    /**
     * 机器 ID 上限（0 ~ 31）
     */
    private static final long MAX_WORKER_ID = (1L << WORKER_BITS) - 1;

    /**
     * 序列上限（0 ~ 4095），溢出归零
     */
    private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1;

    /**
     * 机器 ID 位移
     */
    private static final int WORKER_SHIFT = SEQUENCE_BITS;

    /**
     * 机房 ID 位移
     */
    private static final int DATACENTER_SHIFT = SEQUENCE_BITS + WORKER_BITS;

    /**
     * 时间戳位移
     */
    private static final int TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_BITS + DATACENTER_BITS;

    /**
     * 机房 ID
     */
    private final long datacenterId;

    /**
     * 机器 ID
     */
    private final long workerId;

    /**
     * 时钟源（测试可注入）
     */
    private final LongSupplier timeSource;

    /**
     * 同毫秒内的序列
     */
    private long sequence = 0L;

    /**
     * 上次使用的时间戳（毫秒）
     */
    private long lastTimestamp = -1L;

    /**
     * 单机/由外部统一发号场景：机房与机器均取 0
     */
    public SnowflakeIdGenerator() {
        this(0L, 0L);
    }

    /**
     * @param datacenterId 机房 ID（0 ~ 31）
     * @param workerId     机器 ID（0 ~ 31），多机部署须唯一
     */
    public SnowflakeIdGenerator(long datacenterId, long workerId) {
        this(datacenterId, workerId, System::currentTimeMillis);
    }

    /**
     * 指定时钟源的构造器（包级可见，测试用）
     *
     * @param datacenterId 机房 ID
     * @param workerId     机器 ID
     * @param timeSource   毫秒时间源
     */
    SnowflakeIdGenerator(long datacenterId, long workerId, LongSupplier timeSource) {
        if (datacenterId < 0 || datacenterId > MAX_DATACENTER_ID) {
            throw new IllegalArgumentException(
                    String.format("datacenterId 取值须在 0 ~ %d 之间，实际: %d", MAX_DATACENTER_ID, datacenterId));
        }
        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw new IllegalArgumentException(
                    String.format("workerId 取值须在 0 ~ %d 之间，实际: %d", MAX_WORKER_ID, workerId));
        }
        this.datacenterId = datacenterId;
        this.workerId = workerId;
        this.timeSource = timeSource;
    }

    /**
     * 生成下一个雪花 ID（纯数字，可作 BIGINT 主键）
     *
     * @return 下一个 ID
     * @throws IllegalStateException 发号期间检测到时钟回拨（拒绝发号，避免重复/倒退）
     */
    public synchronized long nextLong() {
        long timestamp = timeSource.getAsLong();
        if (timestamp < lastTimestamp) {
            throw new IllegalStateException(
                    String.format("时钟回拨 %dms，拒绝生成雪花 ID（请等待时钟追平后重试）", lastTimestamp - timestamp));
        }

        // 时间位边界：时钟早于纪元会算出负值污染符号位；超出 41 位则无法再发号
        long elapsed = timestamp - EPOCH;
        if (elapsed < 0) {
            throw new IllegalStateException("系统时钟早于雪花纪元 2024-01-01，无法生成雪花 ID");
        }
        if (elapsed >= (1L << TIMESTAMP_BITS)) {
            throw new IllegalStateException("雪花 ID 时间位已耗尽（41 位，纪元 2024-01-01 起约 69 年）");
        }

        if (timestamp == lastTimestamp) {
            // 同毫秒：序列递增；序列耗尽则自旋到下一毫秒
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }
        lastTimestamp = timestamp;

        return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (datacenterId << DATACENTER_SHIFT)
                | (workerId << WORKER_SHIFT)
                | sequence;
    }

    @Override
    public String nextId() {
        return Long.toString(nextLong());
    }

    /**
     * 自旋等待时间戳超过 {@code last}
     *
     * @param last 已使用的最大时间戳
     * @return 新的时间戳
     */
    private long waitNextMillis(long last) {
        long timestamp = timeSource.getAsLong();
        while (timestamp <= last) {
            Thread.onSpinWait();
            timestamp = timeSource.getAsLong();
        }
        return timestamp;
    }
}
