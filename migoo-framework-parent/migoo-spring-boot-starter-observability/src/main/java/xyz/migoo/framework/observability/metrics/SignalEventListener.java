package xyz.migoo.framework.observability.metrics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.event.EventListener;
import xyz.migoo.framework.common.observability.*;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 信号事件订阅器
 *
 * <p>按事件类型逐个订阅 common 中的信号 record，转发给 {@link SignalMetrics} 计数。
 * 本类（及其委托的 {@link SignalMetrics}）保证不抛异常、不做 IO，以免观测影响业务链路。</p>
 *
 * <p>两种执行模式（{@code migoo.observability.metrics.async}）：</p>
 * <ul>
 *     <li><b>同步（默认）</b>：在发布方线程内直接计数——计数仅一次 Counter 写入，
 *         无锁无 IO，同步执行的开销反而小于线程切换；</li>
 *     <li><b>异步</b>：投递到本类内置的单线程守护线程池（有界队列），业务线程立即返回；
 *         队列满则丢弃事件并告警——观测永不阻塞业务。容器关闭时由 {@link #destroy()}
 *         先排空队列再退出。</li>
 * </ul>
 *
 * @author xiaomi
 */
@Slf4j
public class SignalEventListener implements DisposableBean {

    /**
     * 关闭时等待队列排空的超时时间
     */
    private static final long SHUTDOWN_TIMEOUT_SECONDS = 10L;

    private final SignalMetrics metrics;

    /**
     * 异步执行器；为 null 表示同步模式
     */
    private final ThreadPoolExecutor executor;

    /**
     * 同步模式（默认）
     *
     * @param metrics 信号指标注册中心
     */
    public SignalEventListener(SignalMetrics metrics) {
        this(metrics, false, 0);
    }

    /**
     * 指定执行模式
     *
     * @param metrics        信号指标注册中心
     * @param async          true = 异步计数；false = 发布方线程内同步计数
     * @param queueCapacity  异步队列容量（仅 async = true 时使用）
     */
    public SignalEventListener(SignalMetrics metrics, boolean async, int queueCapacity) {
        this.metrics = metrics;
        this.executor = async ? newExecutor(Math.max(1, queueCapacity)) : null;
    }

    /**
     * 创建单线程守护线程池：1 个 worker + 有界队列，满则丢弃并告警
     */
    private static ThreadPoolExecutor newExecutor(int queueCapacity) {
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger sequence = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable,
                        "migoo-observability-signal-" + sequence.getAndIncrement());
                // 守护线程：应用未显式关闭也不阻止 JVM 退出
                thread.setDaemon(true);
                return thread;
            }
        };
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(queueCapacity), threadFactory);
        executor.setRejectedExecutionHandler((runnable, pool) ->
                log.warn("[SignalEventListener][异步计数队列已满，丢弃信号事件] capacity={}", queueCapacity));
        return executor;
    }

    /**
     * 分发一次计数：同步模式直接执行；异步模式投递（队列满被拒则丢弃）
     */
    private void dispatch(Runnable counting) {
        if (executor == null) {
            runSafely(counting);
            return;
        }
        try {
            executor.execute(() -> runSafely(counting));
        } catch (RejectedExecutionException ex) {
            // 线程池已关闭（容器停机竞态）或队列满：丢弃事件，绝不影响业务
            log.warn("[SignalEventListener][信号计数任务被拒绝，已丢弃] {}", ex.getMessage());
        }
    }

    /**
     * 兜底：计数任务自身异常只记日志，不上抛给发布方
     */
    private static void runSafely(Runnable counting) {
        try {
            counting.run();
        } catch (Throwable ex) {
            log.warn("[SignalEventListener][信号计数异常，已忽略] {}", ex.getMessage(), ex);
        }
    }

    @Override
    public void destroy() {
        if (executor == null) {
            return;
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException ex) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 限流拒绝
     */
    @EventListener
    public void onRateLimitExceeded(RateLimitExceededEvent event) {
        dispatch(() -> metrics.rateLimitExceeded(event.path()));
    }

    /**
     * 登录失败
     */
    @EventListener
    public void onAuthenticationFailed(AuthenticationFailedEvent event) {
        dispatch(() -> metrics.loginFailed(event.reason()));
    }

    /**
     * 账号锁定
     */
    @EventListener
    public void onAccountLocked(AccountLockedEvent event) {
        dispatch(() -> metrics.accountLocked(event.reason()));
    }

    /**
     * 令牌撤销
     */
    @EventListener
    public void onTokenRevoked(TokenRevokedEvent event) {
        dispatch(() -> metrics.tokenRevoked(event.scope()));
    }

    /**
     * 服务端错误（500）
     */
    @EventListener
    public void onServerError(ServerErrorEvent event) {
        dispatch(() -> metrics.serverError(event.path(), event.exceptionType()));
    }

    /**
     * MQ 消息发送
     */
    @EventListener
    public void onMqMessageSent(MqMessageSentEvent event) {
        dispatch(() -> metrics.mqMessageSent(event.stream()));
    }

    /**
     * MQ 消费失败
     */
    @EventListener
    public void onMqConsumeFailed(MqMessageConsumeFailedEvent event) {
        dispatch(() -> metrics.mqConsumeFailed(event.stream(), event.willRetry()));
    }

    /**
     * MQ 死信
     */
    @EventListener
    public void onMqDeadLettered(MqMessageDeadLetteredEvent event) {
        dispatch(() -> metrics.mqDeadLettered(event.stream(), event.reason()));
    }

}
