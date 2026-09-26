package xyz.migoo.framework.mq.core.stream;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * PEL 消息认领与退避重投任务
 * <p>
 * 按 {@code migoo.mq.reclaim.interval} 轮询各 Stream 监听器的消费组 PEL，
 * 把闲置超过 {@code migoo.mq.reclaim.backoff} 的消息认领后重投或转死信
 * （详见 {@link AbstractStreamMessageListener#reclaimPending}），统一收口两类滞留场景：
 * <ul>
 *     <li>消费失败后的退避重试（失败消息不再立即重投，避免失败风暴）；</li>
 *     <li>消费者崩溃/重启后的孤儿消息认领（此前会永久滞留 PEL）。</li>
 * </ul>
 * 实现要点：自持单线程守护调度器（不引入 {@code @EnableScheduling} 全局副作用）；
 * 单个监听器异常不影响其他监听器；{@link #destroy()} 先停调度再等待收尾、可重复调用。
 *
 * @author xiaomi
 */
@Slf4j
public class StreamReclaimTask implements DisposableBean {

    /**
     * 注册的 Stream 监听器
     */
    private final List<AbstractStreamMessageListener<?>> listeners;

    /**
     * 退避时长（PEL 闲置阈值）
     */
    private final Duration backoff;

    /**
     * 轮询间隔
     */
    private final Duration interval;

    /**
     * 调度器（单线程守护）
     */
    private final ScheduledExecutorService executor;

    public StreamReclaimTask(List<AbstractStreamMessageListener<?>> listeners, Duration backoff, Duration interval) {
        this.listeners = listeners;
        this.backoff = backoff;
        this.interval = interval;
        this.executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "migoo-mq-reclaim");
            thread.setDaemon(true);
            return thread;
        });
        long intervalMillis = Math.max(interval.toMillis(), 10L);
        this.executor.scheduleWithFixedDelay(this::reclaimSafely, intervalMillis, intervalMillis, TimeUnit.MILLISECONDS);
        log.info("[StreamReclaimTask][启动 PEL 认领任务] listeners={}, backoff={}, interval={}",
                listeners.size(), backoff, interval);
    }

    /**
     * 逐个监听器执行认领（单个失败不影响其余）
     */
    private void reclaimSafely() {
        for (AbstractStreamMessageListener<?> listener : listeners) {
            try {
                listener.reclaimPending(this.backoff);
            } catch (Exception ex) {
                log.warn("[reclaimSafely][认领失败] listener={}, error={}",
                        listener.getClass().getName(), ex.getMessage());
            }
        }
    }

    /**
     * 停止调度并等待收尾，幂等（重复调用安全）
     */
    @Override
    public void destroy() {
        this.executor.shutdown();
        try {
            if (!this.executor.awaitTermination(5, TimeUnit.SECONDS)) {
                this.executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            this.executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
