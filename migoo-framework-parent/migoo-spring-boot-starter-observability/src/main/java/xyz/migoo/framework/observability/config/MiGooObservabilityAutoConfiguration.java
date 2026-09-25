package xyz.migoo.framework.observability.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import xyz.migoo.framework.observability.health.ObservabilityHealthConfiguration;

/**
 * MiGoo 可观测性模块自动配置入口
 *
 * <p>承载框架的「能力」侧：Actuator/Prometheus 导出、Micrometer Tracing + OpenTelemetry 桥接、
 * 日志关联与信号事件订阅。现有组件只保留极薄的「信号发出点」（Spring {@code ApplicationEvent}，
 * 零 micrometer 依赖），指标收敛在本模块统一注册。</p>
 *
 * <p>沿用组件既有约定：通过 {@code @Import} 显式导入子配置类，不使用 {@code @ComponentScan}：</p>
 * <ul>
 *     <li>{@link ObservabilityMetricsConfiguration} —— 通用 tag、Prometheus 端点暴露；</li>
 *     <li>{@link ObservabilityTraceConfiguration} —— traceparent 与 MDC 桥接、X-Trace-Id 兼容；</li>
 *     <li>{@link ObservabilitySignalConfiguration} —— 订阅 common 中的信号事件并计数；</li>
 *     <li>{@link ObservabilityHealthConfiguration} —— {@code /actuator/info} 框架版本与模块。</li>
 * </ul>
 *
 * <p>另有 {@link ObservabilityEnvironmentPostProcessor} 注册于 {@code META-INF/spring.factories}
 * （EnvironmentPostProcessor 不走自动配置），负责把 {@code migoo.observability.*} 回写为
 * {@code management.*} / {@code logging.*}。</p>
 *
 * @see MigooObservabilityProperties
 */
@AutoConfiguration
@EnableConfigurationProperties(MigooObservabilityProperties.class)
@ConditionalOnProperty(prefix = "migoo.observability", name = "enabled",
        havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter(name = {
        // Tracer Bean 由 Boot 的 tracing 自动配置提供，TraceIdResolver 依赖其存在
        "org.springframework.boot.micrometer.tracing.autoconfigure.MicrometerTracingAutoConfiguration",
        "org.springframework.boot.micrometer.tracing.opentelemetry.autoconfigure.OpenTelemetryTracingAutoConfiguration"
})
@Import({ObservabilityMetricsConfiguration.class, ObservabilityTraceConfiguration.class,
        ObservabilitySignalConfiguration.class, ObservabilityHealthConfiguration.class})
public class MiGooObservabilityAutoConfiguration {
}
