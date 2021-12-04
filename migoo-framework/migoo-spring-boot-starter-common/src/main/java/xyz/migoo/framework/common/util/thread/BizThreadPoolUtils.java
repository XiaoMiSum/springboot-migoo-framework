package xyz.migoo.framework.common.util.thread;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import java.util.Collections;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BizThreadPoolUtils {
    private static final Logger logger = LoggerFactory.getLogger(BizThreadPoolUtils.class);
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int MAXIMUM_POOL_SIZE = 50;
    private static final long KEEP_ALIVE_TIME = 120L;
    private static final int CAPACITY = 20000;
    private static volatile ExecutorService executor;
    private static volatile ScheduledExecutorService scheduledExecutor;

    public static Future<?> submit(Runnable task) {
        return getExecutor().submit(task);
    }

    public static <T> Future<T> submit(Callable<T> task) {
        return getExecutor().submit(task);
    }

    public static Future<?> schedule(Runnable task, long time, TimeUnit... timeUnit) {
        return getScheduledExecutor().schedule(task, time, timeUnit != null ? timeUnit[0] : TimeUnit.SECONDS);
    }

    public static <T> Future<T> schedule(Callable<T> task, long time, TimeUnit... timeUnit) {
        return getScheduledExecutor().schedule(task, time, timeUnit != null ? timeUnit[0] : TimeUnit.SECONDS);
    }

    private static ExecutorService getExecutor() {
        if (executor == null) {
            synchronized(BizThreadPoolUtils.class) {
                if (executor == null) {
                    BlockingQueue blockingQueue = new LinkedBlockingQueue(CAPACITY);
                    executor = ThreadPoolUtils.getExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, blockingQueue,
                            "BizThreadPool-", (r, executor) -> logger.error("Task " + r.toString() + " rejected from " + executor.toString()));
                    ExecutorServiceMetrics executorServiceMetrics = new ExecutorServiceMetrics(executor, "tp1", "tp1", Collections.emptyList());
                    executorServiceMetrics.bindTo(Metrics.globalRegistry);
                }
            }
        }
        return executor;
    }

    private static ScheduledExecutorService getScheduledExecutor() {
        if (scheduledExecutor == null) {
            synchronized(BizThreadPoolUtils.class) {
                if (scheduledExecutor == null) {
                    scheduledExecutor = ThreadPoolUtils.getScheduledExecutor("BizThreadPool-",
                            CORE_POOL_SIZE,(r, executor) -> logger.error("Task " + r.toString() + " rejected from " + executor.toString()));
                    ExecutorServiceMetrics executorServiceMetrics = new ExecutorServiceMetrics(executor, "tp1", "tp1", Collections.emptyList());
                    executorServiceMetrics.bindTo(Metrics.globalRegistry);
                }
            }
        }
        return scheduledExecutor;
    }

    private BizThreadPoolUtils() {
    }
}