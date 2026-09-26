package xyz.migoo.framework.mq.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.PendingMessagesSummary;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import xyz.migoo.framework.mq.core.stream.AbstractStreamMessageListener;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MQ 消费组 PEL（Pending Entries List）积压健康检查
 *
 * <p>对每个注册的 Stream 监听器执行 {@code XPENDING stream group}，按
 * {@code streamKey|group} 维度输出积压明细：</p>
 * <ul>
 *     <li>任一消费组积压 <b>超过</b> {@code migoo.mq.health.backlog-threshold} →
 *         {@code OUT_OF_SERVICE}；等于阈值仍为 {@code UP}；</li>
 *     <li>查询 Redis 失败 → {@code DOWN}（带 error 明细，不抛异常）；</li>
 *     <li>消费组尚未创建（Redis {@code NOGROUP}，监听容器未启动）→ 视为 0 积压。</li>
 * </ul>
 *
 * <p>放置在 mq 模块而非 observability：数据就近（监听器与 Redis 均在本模块），
 * 避免 observability 反向依赖 mq/redis；actuator 是 optional 依赖，
 * classpath 无 actuator 时由 {@code MQHealthAutoConfiguration} 整体跳过装配。</p>
 *
 * @author xiaomi
 */
@Slf4j
public class MqBacklogHealthIndicator implements HealthIndicator {

    /**
     * 明细键：积压阈值
     */
    public static final String DETAIL_THRESHOLD = "backlogThreshold";

    /**
     * 明细键：全部消费组积压合计
     */
    public static final String DETAIL_TOTAL = "totalBacklog";

    /**
     * 明细键：超阈值原因
     */
    public static final String DETAIL_CAUSE = "cause";

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 注册的 Stream 监听器（提供 streamKey 与 group 两个维度）
     */
    private final List<AbstractStreamMessageListener<?>> listeners;

    private final long backlogThreshold;

    /**
     * @param stringRedisTemplate Redis 字符串模板（XPENDING 查询）
     * @param listeners           容器中的 Stream 监听器列表
     * @param backlogThreshold     积压阈值（条）
     */
    public MqBacklogHealthIndicator(StringRedisTemplate stringRedisTemplate,
                                    List<AbstractStreamMessageListener<?>> listeners,
                                    long backlogThreshold) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.listeners = listeners == null ? List.of() : listeners;
        this.backlogThreshold = backlogThreshold;
    }

    @Override
    public Health health() {
        try {
            Map<String, Long> pendingByGroup = collectPendingByGroup();

            long total = pendingByGroup.values().stream().mapToLong(Long::longValue).sum();
            boolean exceeded = pendingByGroup.values().stream().anyMatch(count -> count > this.backlogThreshold);

            Health.Builder builder = exceeded ? Health.outOfService() : Health.up();
            builder.withDetail(DETAIL_THRESHOLD, this.backlogThreshold)
                    .withDetail(DETAIL_TOTAL, total);
            pendingByGroup.forEach((group, count) -> builder.withDetail(group, count));
            if (exceeded) {
                builder.withDetail(DETAIL_CAUSE, "consumer group PEL backlog exceeds threshold");
            }
            return builder.build();
        } catch (Exception ex) {
            // 健康检查自身绝不抛异常：查询失败降级为 DOWN + error 明细
            log.warn("[MqBacklogHealthIndicator][查询 PEL 积压失败] {}", ex.getMessage(), ex);
            return Health.down()
                    .withDetail("error", ex.getClass().getSimpleName() + ": " + ex.getMessage())
                    .build();
        }
    }

    /**
     * 逐个消费组查询 XPENDING，按 {@code streamKey|group} 去重
     * （多个监听器可能指向同一 stream 与分组，只查一次）
     */
    private Map<String, Long> collectPendingByGroup() {
        Map<String, Long> pendingByGroup = new LinkedHashMap<>();
        Set<String> visited = new LinkedHashSet<>();
        StreamOperations<String, Object, Object> operations = this.stringRedisTemplate.opsForStream();
        for (AbstractStreamMessageListener<?> listener : this.listeners) {
            String streamKey = listener.getStreamKey();
            String group = listener.getGroup();
            if (!visited.add(streamKey + "|" + group)) {
                continue;
            }
            pendingByGroup.put(streamKey + "|" + group, pendingOf(operations, streamKey, group));
        }
        return pendingByGroup;
    }

    /**
     * 查询单个消费组的积压条数；分组未创建时视为 0，其余异常继续上抛（由调用方降级 DOWN）
     */
    private static long pendingOf(StreamOperations<String, Object, Object> operations,
                                  String streamKey, String group) {
        try {
            PendingMessagesSummary summary = operations.pending(streamKey, group);
            return summary == null ? 0L : summary.getTotalPendingMessages();
        } catch (Exception ex) {
            if (isGroupNotCreated(ex)) {
                return 0L;
            }
            throw ex;
        }
    }

    /**
     * 消费组尚未创建（Redis 返回 NOGROUP / no such key），Lettuce/Jedis 包装层数不一，逐层查找
     */
    private static boolean isGroupNotCreated(Throwable ex) {
        Throwable current = ex;
        while (current != null && current != current.getCause()) {
            String message = current.getMessage();
            if (message != null && (message.contains("NOGROUP") || message.contains("no such key"))) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

}
