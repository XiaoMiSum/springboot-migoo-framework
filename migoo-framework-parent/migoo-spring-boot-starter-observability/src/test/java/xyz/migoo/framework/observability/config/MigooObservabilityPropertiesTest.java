package xyz.migoo.framework.observability.config;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link MigooObservabilityProperties} 单元测试
 *
 * <p>验证 Metrics / Tracing / Logging 子配置的默认值以及 setter 绑定。</p>
 */
class MigooObservabilityPropertiesTest {

    @Test
    void defaultsAreApplied() {
        MigooObservabilityProperties properties = new MigooObservabilityProperties();

        assertThat(properties.isEnabled()).isTrue();

        MigooObservabilityProperties.Metrics metrics = properties.getMetrics();
        assertThat(metrics).isNotNull();
        assertThat(metrics.isEnabled()).isTrue();
        assertThat(metrics.isPrometheusExposure()).isTrue();
        assertThat(metrics.getCommonTags()).isEmpty();
        assertThat(metrics.getSignals()).isEmpty();

        MigooObservabilityProperties.Tracing tracing = properties.getTracing();
        assertThat(tracing).isNotNull();
        assertThat(tracing.isEnabled()).isTrue();
        // 采样率 / OTLP 端点留空，沿用 Spring Boot management.* 默认值
        assertThat(tracing.getSamplingProbability()).isNull();
        assertThat(tracing.getOtlpEndpoint()).isNull();
        assertThat(tracing.getOtlpEnabled()).isNull();
        assertThat(tracing.isPropagateXTraceId()).isTrue();

        MigooObservabilityProperties.Logging logging = properties.getLogging();
        assertThat(logging).isNotNull();
        assertThat(logging.isCorrelation()).isTrue();
        assertThat(logging.getCorrelationPattern()).isNull();
    }

    @Test
    void settersBindValues() {
        MigooObservabilityProperties properties = new MigooObservabilityProperties();

        properties.setEnabled(false);

        MigooObservabilityProperties.Metrics metrics = properties.getMetrics();
        metrics.setEnabled(false);
        metrics.setPrometheusExposure(false);
        metrics.setCommonTags(Map.of("application", "demo"));
        metrics.setSignals(Map.of("ratelimit.rejected", false));

        MigooObservabilityProperties.Tracing tracing = properties.getTracing();
        tracing.setEnabled(false);
        tracing.setSamplingProbability(1.0D);
        tracing.setOtlpEndpoint("http://localhost:4318/v1/traces");
        tracing.setOtlpEnabled(false);
        tracing.setPropagateXTraceId(false);

        MigooObservabilityProperties.Logging logging = properties.getLogging();
        logging.setCorrelation(false);
        logging.setCorrelationPattern("[%X{traceId:-}] ");

        assertThat(properties.isEnabled()).isFalse();
        assertThat(metrics.isEnabled()).isFalse();
        assertThat(metrics.isPrometheusExposure()).isFalse();
        assertThat(metrics.getCommonTags()).containsEntry("application", "demo");
        assertThat(metrics.getSignals()).containsEntry("ratelimit.rejected", false);
        assertThat(tracing.isEnabled()).isFalse();
        assertThat(tracing.getSamplingProbability()).isEqualTo(1.0D);
        assertThat(tracing.getOtlpEndpoint()).isEqualTo("http://localhost:4318/v1/traces");
        assertThat(tracing.getOtlpEnabled()).isFalse();
        assertThat(tracing.isPropagateXTraceId()).isFalse();
        assertThat(logging.isCorrelation()).isFalse();
        assertThat(logging.getCorrelationPattern()).isEqualTo("[%X{traceId:-}] ");
    }

    @Test
    void subConfigsAreIndependentInstancesPerProperties() {
        MigooObservabilityProperties first = new MigooObservabilityProperties();
        MigooObservabilityProperties second = new MigooObservabilityProperties();

        assertThat(first.getMetrics()).isNotSameAs(second.getMetrics());
        assertThat(first.getTracing()).isNotSameAs(second.getTracing());
        assertThat(first.getLogging()).isNotSameAs(second.getLogging());
    }
}
