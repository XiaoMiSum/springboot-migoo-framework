package xyz.migoo.framework.observability.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.migoo.framework.observability.metrics.SignalEventListener;
import xyz.migoo.framework.observability.metrics.SignalMetrics;

/**
 * 信号指标订阅配置
 *
 * <p>订阅 common 中的信号事件（限流/认证/MQ/500）并计数，
 * {@code migoo.observability.metrics.enabled=false} 时整体不装配（此时各组件发布的事件无人消费）。</p>
 *
 * @author xiaomi
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "migoo.observability.metrics", name = "enabled",
        havingValue = "true", matchIfMissing = true)
public class ObservabilitySignalConfiguration {

    /**
     * 信号指标注册中心 Bean
     *
     * @param meterRegistry MeterRegistry（Boot 自动配置，含 Prometheus 注册表）
     * @param properties    可观测性配置（读取 {@code metrics.signals} 单信号开关）
     * @return 信号指标注册中心
     */
    @Bean
    @ConditionalOnMissingBean(SignalMetrics.class)
    public SignalMetrics signalMetrics(MeterRegistry meterRegistry, MigooObservabilityProperties properties) {
        return new SignalMetrics(meterRegistry, properties);
    }

    /**
     * 信号事件订阅器 Bean
     *
     * @param signalMetrics 信号指标注册中心
     * @return 事件订阅器
     */
    @Bean
    @ConditionalOnMissingBean(SignalEventListener.class)
    public SignalEventListener signalEventListener(SignalMetrics signalMetrics) {
        return new SignalEventListener(signalMetrics);
    }

}
