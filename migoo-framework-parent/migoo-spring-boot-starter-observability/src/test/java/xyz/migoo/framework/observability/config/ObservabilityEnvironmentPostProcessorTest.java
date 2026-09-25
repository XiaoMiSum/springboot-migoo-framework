package xyz.migoo.framework.observability.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ObservabilityEnvironmentPostProcessor} 单元测试
 *
 * <p>用 {@link StandardEnvironment} 模拟用户配置（application.yml 等），逐一验证回写规则：
 * 未配置 → 补默认；用户显式配置 → 让位；单点开关 → 多键联动。</p>
 */
class ObservabilityEnvironmentPostProcessorTest {

    private static final String EXPOSURE_KEY = "management.endpoints.web.exposure.include";

    private final ObservabilityEnvironmentPostProcessor processor = new ObservabilityEnvironmentPostProcessor();

    // ==================== 端点暴露 ====================

    @Test
    void addsHealthInfoPrometheusWhenNotConfigured() {
        StandardEnvironment environment = new StandardEnvironment();

        processor.postProcessEnvironment(environment, null);

        assertThat(environment.getProperty(EXPOSURE_KEY)).isEqualTo("health,info,prometheus");
    }

    @Test
    void keepsUserExposureConfig() {
        StandardEnvironment environment = new StandardEnvironment();
        userConfig(environment, EXPOSURE_KEY, "health,metrics");

        processor.postProcessEnvironment(environment, null);

        assertThat(environment.getProperty(EXPOSURE_KEY)).isEqualTo("health,metrics");
    }

    @Test
    void skipsExposureWhenPrometheusExposureDisabled() {
        StandardEnvironment environment = new StandardEnvironment();
        userConfig(environment, "migoo.observability.metrics.prometheus-exposure", "false");

        processor.postProcessEnvironment(environment, null);

        assertThat(environment.getProperty(EXPOSURE_KEY)).isNull();
    }

    // ==================== 采样与 OTLP ====================

    @Test
    void writesSamplingProbability() {
        StandardEnvironment environment = new StandardEnvironment();
        userConfig(environment, "migoo.observability.tracing.sampling-probability", "1.0");

        processor.postProcessEnvironment(environment, null);

        assertThat(environment.getProperty("management.tracing.sampling.probability")).isEqualTo("1.0");
    }

    @Test
    void keepsUserSamplingProbability() {
        StandardEnvironment environment = new StandardEnvironment();
        userConfig(environment, "management.tracing.sampling.probability", "0.5");
        userConfig(environment, "migoo.observability.tracing.sampling-probability", "1.0");

        processor.postProcessEnvironment(environment, null);

        assertThat(environment.getProperty("management.tracing.sampling.probability")).isEqualTo("0.5");
    }

    @Test
    void otlpEnabledWritesAllThreeExportSwitches() {
        StandardEnvironment environment = new StandardEnvironment();
        userConfig(environment, "migoo.observability.tracing.otlp-enabled", "false");

        processor.postProcessEnvironment(environment, null);

        assertThat(environment.getProperty("management.tracing.export.otlp.enabled")).isEqualTo("false");
        assertThat(environment.getProperty("management.otlp.metrics.export.enabled")).isEqualTo("false");
        assertThat(environment.getProperty("management.logging.export.otlp.enabled")).isEqualTo("false");
    }

    @Test
    void keepsExplicitlyConfiguredOtlpSwitch() {
        StandardEnvironment environment = new StandardEnvironment();
        userConfig(environment, "management.tracing.export.otlp.enabled", "true");
        userConfig(environment, "migoo.observability.tracing.otlp-enabled", "false");

        processor.postProcessEnvironment(environment, null);

        assertThat(environment.getProperty("management.tracing.export.otlp.enabled")).isEqualTo("true");
        // 另外两处仍按单点决策回写
        assertThat(environment.getProperty("management.otlp.metrics.export.enabled")).isEqualTo("false");
        assertThat(environment.getProperty("management.logging.export.otlp.enabled")).isEqualTo("false");
    }

    @Test
    void derivesMetricsAndLogsEndpointsFromTracesEndpoint() {
        StandardEnvironment environment = new StandardEnvironment();
        userConfig(environment, "migoo.observability.tracing.otlp-endpoint", "http://collector:4318/v1/traces");

        processor.postProcessEnvironment(environment, null);

        assertThat(environment.getProperty("management.opentelemetry.tracing.export.otlp.endpoint"))
                .isEqualTo("http://collector:4318/v1/traces");
        assertThat(environment.getProperty("management.otlp.metrics.export.url"))
                .isEqualTo("http://collector:4318/v1/metrics");
        assertThat(environment.getProperty("management.opentelemetry.logging.export.otlp.endpoint"))
                .isEqualTo("http://collector:4318/v1/logs");
    }

    // ==================== 日志关联 ====================

    @Test
    void correlationDisabledOverridesBootLogCorrelationSource() {
        StandardEnvironment environment = new StandardEnvironment();
        // 模拟 Boot 的 LogCorrelationEnvironmentPostProcessor 默认源（默认给 true）
        environment.getPropertySources().addLast(
                new MapPropertySource("logCorrelation", Map.of("logging.expect-correlation-id", "true")));
        userConfig(environment, "migoo.observability.logging.correlation", "false");

        processor.postProcessEnvironment(environment, null);

        // 本处理器 addFirst，压过后置的 Boot 默认源
        assertThat(environment.getProperty("logging.expect-correlation-id")).isEqualTo("false");
    }

    @Test
    void writesCorrelationPatternWhenProvided() {
        StandardEnvironment environment = new StandardEnvironment();
        userConfig(environment, "migoo.observability.logging.correlation-pattern", "[%X{traceId:-}] ");

        processor.postProcessEnvironment(environment, null);

        assertThat(environment.getProperty("logging.pattern.correlation")).isEqualTo("[%X{traceId:-}] ");
    }

    // ==================== 总开关 ====================

    @Test
    void doesNothingWhenModuleDisabled() {
        StandardEnvironment environment = new StandardEnvironment();
        userConfig(environment, "migoo.observability.enabled", "false");

        processor.postProcessEnvironment(environment, null);

        assertThat(environment.getProperty(EXPOSURE_KEY)).isNull();
        assertThat(environment.getPropertySources().get(ObservabilityEnvironmentPostProcessor.SOURCE_NAME)).isNull();
    }

    // ==================== 夹具 ====================

    /**
     * 模拟用户显式配置（application.yml / 环境变量 / 命令行参数均等价）
     * <p>属性源名带上 key，避免同名属性源相互替换。</p>
     */
    private static void userConfig(StandardEnvironment environment, String key, String value) {
        environment.getPropertySources().addFirst(new MapPropertySource("userConfig:" + key, Map.of(key, value)));
    }

}
