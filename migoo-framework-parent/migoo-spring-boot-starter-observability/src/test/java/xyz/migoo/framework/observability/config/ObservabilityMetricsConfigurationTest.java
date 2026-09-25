package xyz.migoo.framework.observability.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.micrometer.metrics.autoconfigure.MeterRegistryCustomizer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ObservabilityMetricsConfiguration} 单元测试
 *
 * <p>直接调用 {@code @Bean} 方法，验证通用 tag 应用到 MeterRegistry 后对新建指标生效。</p>
 */
class ObservabilityMetricsConfigurationTest {

    @Test
    void commonTagsApplyToNewMeters() {
        MigooObservabilityProperties properties = new MigooObservabilityProperties();
        properties.getMetrics().getCommonTags().put("application", "demo");
        properties.getMetrics().getCommonTags().put("env", "test");

        MeterRegistryCustomizer<MeterRegistry> customizer =
                new ObservabilityMetricsConfiguration().migooCommonTagsCustomizer(properties);
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        customizer.customize(registry);

        registry.counter("demo.counter").increment();

        assertThat(registry.get("demo.counter")
                .tags("application", "demo", "env", "test")
                .counter().count()).isEqualTo(1.0);
    }

    @Test
    void emptyCommonTagsIsNoOp() {
        // 未配置通用 tag 时定制器照样注册，但不应影响任何指标
        MeterRegistryCustomizer<MeterRegistry> customizer =
                new ObservabilityMetricsConfiguration().migooCommonTagsCustomizer(new MigooObservabilityProperties());
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        customizer.customize(registry);

        registry.counter("demo.counter").increment();

        assertThat(registry.get("demo.counter").counters()).hasSize(1);
        assertThat(registry.get("demo.counter").counter().getId().getTags()).isEmpty();
    }

}
