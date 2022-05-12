package xyz.migoo.framework.common.util.thread;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;

import java.util.Collections;
import java.util.concurrent.*;

public class ThreadPoolUtils {
    public ThreadPoolUtils() {
    }

    public static ExecutorService newSingleExecutor(String threadName, BlockingQueue blockingQueue, RejectedExecutionHandler rejectedExecutionHandler) {
        return getExecutor(1, 1, 120L, blockingQueue, threadName, rejectedExecutionHandler);
    }

    public static ExecutorService getExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, BlockingQueue blockingQueue, String threadName, RejectedExecutionHandler rejectedExecutionHandler) {
        if (threadName != null && threadName.trim().length() != 0) {
            if (blockingQueue == null) {
                throw new IllegalArgumentException("blockingQueue不能为空");
            } else if (rejectedExecutionHandler == null) {
                throw new IllegalArgumentException("rejectedExecutionHandler不能为空");
            } else {
                ThreadFactory threadFactory = new TaskThreadFactory(threadName + "-");
                ExecutorService executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, blockingQueue, threadFactory, rejectedExecutionHandler);
                ExecutorServiceMetrics executorServiceMetrics = new ExecutorServiceMetrics(executor, threadName, threadName, Collections.emptyList());
                executorServiceMetrics.bindTo(Metrics.globalRegistry);
                return executor;
            }
        } else {
            throw new IllegalArgumentException("threadName不能为空");
        }
    }

    public static ScheduledExecutorService getScheduledExecutor(String threadName, int corePoolSize, RejectedExecutionHandler rejectedExecutionHandler){
        if (threadName == null || threadName.trim().length() == 0) {
            throw new IllegalArgumentException("threadName不能为空");
        }
        if (rejectedExecutionHandler == null) {
            throw new IllegalArgumentException("rejectedExecutionHandler不能为空");
        }
        return new ScheduledThreadPoolExecutor(corePoolSize, new TaskThreadFactory(threadName + "-"), rejectedExecutionHandler);
    }
}