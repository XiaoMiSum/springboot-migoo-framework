package xyz.migoo.framework.web.config;

import org.apache.coyote.ProtocolHandler;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.tomcat.TomcatProtocolHandlerCustomizer;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * {@link VirtualThreadConfiguration} 单元测试：@Bean 方法可直接脱离容器调用。
 */
class VirtualThreadConfigurationTest {

    @Test
    void customizerSetsVirtualThreadExecutor() {
        TomcatProtocolHandlerCustomizer<ProtocolHandler> customizer =
                cast(new VirtualThreadConfiguration().protocolHandlerVirtualThreadExecutorCustomizer());
        assertThat(customizer).isNotNull();

        ProtocolHandler handler = mock(ProtocolHandler.class);
        customizer.customize(handler);

        ArgumentCaptor<Executor> captor = ArgumentCaptor.forClass(Executor.class);
        verify(handler).setExecutor(captor.capture());
        assertThat(captor.getValue()).isNotNull();
    }

    @Test
    void executorRunsTasksOnVirtualThreads() throws Exception {
        TomcatProtocolHandlerCustomizer<ProtocolHandler> customizer =
                cast(new VirtualThreadConfiguration().protocolHandlerVirtualThreadExecutorCustomizer());
        ProtocolHandler handler = mock(ProtocolHandler.class);
        customizer.customize(handler);

        ArgumentCaptor<Executor> captor = ArgumentCaptor.forClass(Executor.class);
        verify(handler).setExecutor(captor.capture());
        Executor executor = captor.getValue();

        AtomicReference<String> threadName = new AtomicReference<>();
        try (ExecutorService es = (ExecutorService) executor) {
            es.submit(() -> threadName.set(Thread.currentThread().toString())).get();
        }
        // 线程名为 virtual-thread-N，验证虚拟线程生效
        assertThat(threadName.get()).contains("virtual-thread-");
    }

    @SuppressWarnings("unchecked")
    private static TomcatProtocolHandlerCustomizer<ProtocolHandler> cast(
            TomcatProtocolHandlerCustomizer<?> customizer) {
        return (TomcatProtocolHandlerCustomizer<ProtocolHandler>) customizer;
    }
}
