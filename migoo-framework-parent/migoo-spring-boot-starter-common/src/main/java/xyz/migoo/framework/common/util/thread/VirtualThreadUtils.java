package xyz.migoo.framework.common.util.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class VirtualThreadUtils {

    private static final ExecutorService executor;

    static {
        var factory = Thread.ofVirtual().name("biz-virtual-thread-", 1).factory();
        executor = Executors.newThreadPerTaskExecutor(factory);
    }

    private VirtualThreadUtils() {
    }

    public static Future<?> submit(Runnable task) {
        return executor.submit(task);
    }

    public static <T> Future<T> submit(Callable<T> task) {
        return executor.submit(task);
    }
}