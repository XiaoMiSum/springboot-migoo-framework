package xyz.migoo.framework.common.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * VirtualThreadUtils 单元测试
 */
class VirtualThreadUtilsTest {

    @Test
    void submit_runnableCompletesAndSideEffectExecuted() throws Exception {
        AtomicBoolean executed = new AtomicBoolean(false);
        // 块体 lambda 只匹配 Runnable 重载，避免与 Callable 重载产生歧义
        Future<?> future = VirtualThreadUtils.submit(() -> {
            executed.set(true);
        });
        // Runnable 任务 get() 返回 null，且副作用已执行
        assertThat(future.get()).isNull();
        assertThat(executed).isTrue();
    }

    @Test
    void submit_callableReturnsValue() throws Exception {
        Future<String> future = VirtualThreadUtils.submit(() -> "hello");
        assertThat(future.get()).isEqualTo("hello");
    }

    @Test
    void submit_callableThrowsWrappedInExecutionException() {
        Future<String> future = VirtualThreadUtils.submit((Callable<String>) () -> {
            throw new IllegalStateException("boom");
        });
        // 原始异常被包装为 ExecutionException
        assertThatThrownBy(future::get)
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(IllegalStateException.class);
    }
}
