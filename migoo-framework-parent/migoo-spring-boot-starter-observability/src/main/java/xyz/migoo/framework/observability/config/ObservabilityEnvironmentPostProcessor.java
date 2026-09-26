package xyz.migoo.framework.observability.config;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 可观测性环境后处理器 —— 「统一决策单点」的落地
 *
 * <p>把 {@code migoo.observability.*} 的集中决策<b>回写</b>为 Spring Boot 官方属性
 * （{@code management.*} / {@code logging.*}），覆盖三类决策：</p>
 * <ol>
 *     <li><b>端点暴露</b>：未显式配置时把 {@code health,info,prometheus} 写入
 *         {@code management.endpoints.web.exposure.include}，使 {@code /actuator/prometheus} 开箱可用；</li>
 *     <li><b>采样与 OTLP</b>：采样率、OTLP 端点（可由 {@code /v1/traces} 推导 metrics/logs 端点）、
 *         三类 OTLP 导出总开关（Boot 4.1 默认全开、指向本机 :4318，无 Collector 须一键关闭）；</li>
 *     <li><b>日志关联</b>：{@code logging.correlation=false} 时关闭 Boot 的关联 ID 期望
 *         （{@code logging.expect-correlation-id}），或自定义 {@code logging.pattern.correlation}；</li>
 *     <li><b>结构化日志</b>：{@code logging.format} 非 OFF 时把 Boot 的 JSON 日志格式
 *         （{@code ecs} / {@code gelf} / {@code logstash}）写入
 *         {@code logging.structured.format.console} 与 {@code .file}——内置格式自动携带
 *         MDC（含 traceId/spanId），无需再配 logback encoder。</li>
 * </ol>
 *
 * <p><b>回写规则</b>：Spring Boot 官方属性是最终事实 —— 用户显式配置过的键一律不写；
 * 仅当键在所有属性源（application.yml、环境变量、命令行参数等）中都未出现时才写入默认值。
 * 因此本处理器的属性源虽 {@code addFirst}（需压过 Boot 后置的 {@code logCorrelation} 默认源），
 * 也不会覆盖任何用户配置。</p>
 *
 * <p>执行时机：注册于 {@code META-INF/spring.factories}，排序在 ConfigData
 * （application.yml 加载，{@code HIGHEST_PRECEDENCE + 10}）之后，确保能读到用户配置。</p>
 *
 * @author xiaomi
 */
public class ObservabilityEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    /**
     * 本处理器写入的属性源名称
     */
    static final String SOURCE_NAME = "migooObservability";

    /**
     * Boot 自身的日志关联默认源（LogCorrelationEnvironmentPostProcessor），不算用户配置
     */
    static final String BOOT_LOG_CORRELATION_SOURCE = "logCorrelation";

    /**
     * Boot 的配置属性聚合源，不算用户配置
     */
    static final String CONFIGURATION_PROPERTIES_SOURCE = "configurationProperties";

    /**
     * 排序值：ConfigData（application.yml 加载）为 {@code HIGHEST_PRECEDENCE + 10}，此处晚于它即可
     */
    static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 100;

    private static final String PREFIX = "migoo.observability";

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        MigooObservabilityProperties properties = Binder.get(environment)
                .bind(PREFIX, Bindable.of(MigooObservabilityProperties.class))
                .orElseGet(MigooObservabilityProperties::new);
        // 模块总开关关闭 → 不做任何回写
        if (!properties.isEnabled()) {
            return;
        }
        Map<String, Object> defaults = new LinkedHashMap<>();
        collectDefaults(environment, properties, defaults);
        if (!defaults.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource(SOURCE_NAME, defaults));
        }
    }

    /**
     * 收集「用户未显式配置」的键的默认值
     */
    private void collectDefaults(ConfigurableEnvironment environment,
                                 MigooObservabilityProperties properties,
                                 Map<String, Object> defaults) {
        MigooObservabilityProperties.Metrics metrics = properties.getMetrics();
        MigooObservabilityProperties.Tracing tracing = properties.getTracing();
        MigooObservabilityProperties.Logging logging = properties.getLogging();

        // ① Actuator 端点暴露：默认 health,info,prometheus（info 对应框架 InfoContributor）
        if (metrics.isPrometheusExposure()
                && !isUserConfigured(environment, "management.endpoints.web.exposure.include")) {
            defaults.put("management.endpoints.web.exposure.include", "health,info,prometheus");
        }

        // ② 采样概率
        if (tracing.getSamplingProbability() != null) {
            putDefault(environment, defaults, "management.tracing.sampling.probability",
                    String.valueOf(tracing.getSamplingProbability()));
        }

        // ③ OTLP 端点：HTTP 端点含 /v1/traces 时顺带推导 metrics / logs 端点
        String endpoint = tracing.getOtlpEndpoint();
        if (endpoint != null && !endpoint.isBlank()) {
            putDefault(environment, defaults, "management.opentelemetry.tracing.export.otlp.endpoint", endpoint);
            int index = endpoint.indexOf("/v1/traces");
            if (index > 0) {
                String base = endpoint.substring(0, index);
                putDefault(environment, defaults, "management.otlp.metrics.export.url", base + "/v1/metrics");
                putDefault(environment, defaults, "management.opentelemetry.logging.export.otlp.endpoint",
                        base + "/v1/logs");
            }
        }

        // ④ 三类 OTLP 导出总开关（链路 / 指标 / 日志）
        if (tracing.getOtlpEnabled() != null) {
            String value = String.valueOf(tracing.getOtlpEnabled());
            putDefault(environment, defaults, "management.tracing.export.otlp.enabled", value);
            putDefault(environment, defaults, "management.otlp.metrics.export.enabled", value);
            putDefault(environment, defaults, "management.logging.export.otlp.enabled", value);
        }

        // ⑤ 日志关联：关闭时压过 Boot 的 logCorrelation 默认源；开启且给了自定义 pattern 时回写
        if (!logging.isCorrelation()) {
            putDefault(environment, defaults, "logging.expect-correlation-id", false);
        } else if (logging.getCorrelationPattern() != null && !logging.getCorrelationPattern().isBlank()) {
            putDefault(environment, defaults, "logging.pattern.correlation", logging.getCorrelationPattern());
        }

        // ⑥ 结构化日志（JSON）：Boot 内置格式会把 MDC（含 traceId/spanId）全量写入 JSON，
        //    因此开启后无需再配置 logback encoder；console 与 file 两处端点同时回写
        if (logging.getFormat() != MigooObservabilityProperties.Format.OFF) {
            String value = logging.getFormat().name().toLowerCase(Locale.ROOT);
            putDefault(environment, defaults, "logging.structured.format.console", value);
            putDefault(environment, defaults, "logging.structured.format.file", value);
        }
    }

    /**
     * 键未被用户显式配置时写入默认值
     */
    private static void putDefault(ConfigurableEnvironment environment, Map<String, Object> defaults,
                                   String key, Object value) {
        if (!isUserConfigured(environment, key)) {
            defaults.put(key, value);
        }
    }

    /**
     * 判断键是否已被用户显式配置（遍历属性源，排除本处理器与 Boot 的默认源）
     *
     * @param environment 环境
     * @param key         属性键
     * @return true = 用户已配置（此时回写让位）
     */
    static boolean isUserConfigured(ConfigurableEnvironment environment, String key) {
        for (PropertySource<?> source : environment.getPropertySources()) {
            String name = source.getName();
            if (SOURCE_NAME.equals(name) || BOOT_LOG_CORRELATION_SOURCE.equals(name)
                    || CONFIGURATION_PROPERTIES_SOURCE.equals(name)) {
                continue;
            }
            if (source.containsProperty(key)) {
                return true;
            }
        }
        return false;
    }

}
