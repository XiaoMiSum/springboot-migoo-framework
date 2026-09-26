package xyz.migoo.framework.mq.health;

import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.PendingMessagesSummary;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import xyz.migoo.framework.mq.config.MQProperties;
import xyz.migoo.framework.mq.core.stream.AbstractStreamMessage;
import xyz.migoo.framework.mq.core.stream.AbstractStreamMessageListener;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link MqBacklogHealthIndicator} 单元测试
 *
 * <p>覆盖：阈值内 UP、超阈值 OUT_OF_SERVICE（含等于阈值边界）、同 stream+分组去重、
 * NOGROUP 视为 0 积压、Redis 故障降级 DOWN、无监听器。</p>
 */
@SuppressWarnings({"rawtypes", "unchecked"})
class MqBacklogHealthIndicatorTest {

    private static final String GROUP = "def_group";

    /** 订单消息（streamKey = 类简单名） */
    static class OrderMessage extends AbstractStreamMessage {
    }

    /** 支付消息 */
    static class PayMessage extends AbstractStreamMessage {
    }

    static class OrderListener extends AbstractStreamMessageListener<OrderMessage> {
        OrderListener(MQProperties properties) {
            super(properties);
        }

        @Override
        public void onMessage(OrderMessage message) {
        }
    }

    static class PayListener extends AbstractStreamMessageListener<PayMessage> {
        PayListener(MQProperties properties) {
            super(properties);
        }

        @Override
        public void onMessage(PayMessage message) {
        }
    }

    @Test
    void upWhenBacklogsWithinThreshold() {
        MQProperties properties = new MQProperties();
        StreamOperations streamOps = mock(StreamOperations.class);
        when(streamOps.pending("OrderMessage", GROUP)).thenReturn(pending(5L));
        // 等于阈值不算超限，仍为 UP
        when(streamOps.pending("PayMessage", GROUP)).thenReturn(pending(1000L));
        MqBacklogHealthIndicator indicator =
                new MqBacklogHealthIndicator(templateReturning(streamOps), defaultListeners(properties), 1000L);

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails())
                .containsEntry(MqBacklogHealthIndicator.DETAIL_THRESHOLD, 1000L)
                .containsEntry(MqBacklogHealthIndicator.DETAIL_TOTAL, 1005L)
                .containsEntry("OrderMessage|" + GROUP, 5L)
                .containsEntry("PayMessage|" + GROUP, 1000L)
                .doesNotContainKey(MqBacklogHealthIndicator.DETAIL_CAUSE);
    }

    @Test
    void outOfServiceWhenAnyGroupExceedsThreshold() {
        MQProperties properties = new MQProperties();
        StreamOperations streamOps = mock(StreamOperations.class);
        when(streamOps.pending("OrderMessage", GROUP)).thenReturn(pending(1001L));
        when(streamOps.pending("PayMessage", GROUP)).thenReturn(pending(1L));
        MqBacklogHealthIndicator indicator =
                new MqBacklogHealthIndicator(templateReturning(streamOps), defaultListeners(properties), 1000L);

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.OUT_OF_SERVICE);
        assertThat(health.getDetails())
                .containsEntry(MqBacklogHealthIndicator.DETAIL_TOTAL, 1002L)
                .containsKey(MqBacklogHealthIndicator.DETAIL_CAUSE);
    }

    @Test
    void sameStreamAndGroupQueriedOnce() {
        MQProperties properties = new MQProperties();
        StreamOperations streamOps = mock(StreamOperations.class);
        when(streamOps.pending("OrderMessage", GROUP)).thenReturn(pending(1L));
        // 两个监听器指向同一消息类型 → 同一 stream + 分组，只查询一次
        MqBacklogHealthIndicator indicator = new MqBacklogHealthIndicator(
                templateReturning(streamOps), List.of(new OrderListener(properties), new OrderListener(properties)),
                1000L);

        indicator.health();

        verify(streamOps, times(1)).pending("OrderMessage", GROUP);
    }

    @Test
    void missingGroupTreatedAsEmptyBacklog() {
        MQProperties properties = new MQProperties();
        StreamOperations streamOps = mock(StreamOperations.class);
        // 分组尚未创建（监听容器未启动）→ Redis NOGROUP，视为 0 积压而不是 DOWN
        when(streamOps.pending(anyString(), anyString()))
                .thenThrow(new RuntimeException("NOGROUP No such key with this group name"));
        MqBacklogHealthIndicator indicator =
                new MqBacklogHealthIndicator(templateReturning(streamOps), defaultListeners(properties), 1000L);

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry(MqBacklogHealthIndicator.DETAIL_TOTAL, 0L);
    }

    @Test
    void redisFailureReportsDownWithoutThrowing() {
        MQProperties properties = new MQProperties();
        StreamOperations streamOps = mock(StreamOperations.class);
        when(streamOps.pending(anyString(), anyString())).thenThrow(new RuntimeException("connection refused"));
        MqBacklogHealthIndicator indicator =
                new MqBacklogHealthIndicator(templateReturning(streamOps), defaultListeners(properties), 1000L);

        Health health = indicator.health();

        // 查询失败降级为 DOWN + error 明细，健康检查自身不抛异常
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("error", "RuntimeException: connection refused");
    }

    @Test
    void noListenersReportsUp() {
        StreamOperations streamOps = mock(StreamOperations.class);
        MqBacklogHealthIndicator indicator = new MqBacklogHealthIndicator(templateReturning(streamOps), List.of(), 1000L);

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry(MqBacklogHealthIndicator.DETAIL_TOTAL, 0L);
        verify(streamOps, times(0)).pending(anyString(), anyString());
    }

    // ==================== 夹具 ====================

    private static List<AbstractStreamMessageListener<?>> defaultListeners(MQProperties properties) {
        return List.of(new OrderListener(properties), new PayListener(properties));
    }

    private static PendingMessagesSummary pending(long total) {
        return new PendingMessagesSummary(GROUP, total, Range.unbounded(), Map.of());
    }

    private static StringRedisTemplate templateReturning(StreamOperations streamOps) {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        when(template.opsForStream()).thenReturn(streamOps);
        return template;
    }

}
