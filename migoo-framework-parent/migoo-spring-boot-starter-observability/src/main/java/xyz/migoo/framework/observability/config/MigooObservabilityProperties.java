package xyz.migoo.framework.observability.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 可观测性模块配置属性
 *
 * <p>本类是框架可观测性的「统一决策单点」：采样率、OTLP 端点、端点暴露等原本属于
 * {@code management.*} 的开关，在此处集中声明；由环境后处理器在 {@code management.*}
 * 未被用户显式设置时写回 Environment，避免两处配置相互覆盖。</p>
 *
 * <p>标准埋点（HTTP/JVM/Redis/JDBC）由 Actuator 自动完成，不在此处重复定义。</p>
 */
@Data
@ConfigurationProperties(prefix = "migoo.observability")
public class MigooObservabilityProperties {

    /**
     * 是否启用可观测性模块
     */
    private boolean enabled = true;

    /**
     * 指标（Metrics）配置
     */
    private Metrics metrics = new Metrics();

    /**
     * 链路追踪（Tracing）配置
     */
    private Tracing tracing = new Tracing();

    /**
     * 日志关联（Logging Correlation）配置
     */
    private Logging logging = new Logging();

    @Data
    public static class Metrics {

        /**
         * 是否启用信号指标（限流/锁定/撤销/MQ/500 等业务信号计数）
         */
        private boolean enabled = true;

        /**
         * 是否自动将 prometheus 端点加入 actuator web 端点暴露清单，
         * 使 /actuator/prometheus 开箱可用
         */
        private boolean prometheusExposure = true;

        /**
         * 所有指标的通用 tag（key -&gt; value），例如 {application: demo, region: cn-north}
         */
        private Map<String, String> commonTags = new LinkedHashMap<>();

        /**
         * 单个信号指标的开关（key 为信号名，见 docs/observability.md 指标清单）；
         * 未列出的信号跟随 {@link #enabled}
         */
        private Map<String, Boolean> signals = new LinkedHashMap<>();
    }

    @Data
    public static class Tracing {

        /**
         * 是否启用链路追踪桥接（traceparent 解析、MDC 关联、X-Trace-Id 兼容）
         */
        private boolean enabled = true;

        /**
         * 采样概率（0.0 - 1.0），写回 management.tracing.sampling.probability；
         * Spring Boot 默认 0.1，即采样 10% 请求
         */
        private Double samplingProbability;

        /**
         * OTLP 上报端点（gRPC 形如 http://localhost:4317，
         * HTTP 形如 http://localhost:4318/v1/traces）；
         * 写回 management.opentelemetry.tracing.export.otlp.endpoint，为空则使用 Spring Boot 默认
         */
        private String otlpEndpoint;

        /**
         * 是否向同一个 OTLP 后端（Collector）上报，写回三处官方开关：
         * management.tracing.export.otlp.enabled（链路）、
         * management.otlp.metrics.export.enabled（指标）、
         * management.logging.export.otlp.enabled（日志），三者 Boot 4.1 默认均为 true。
         * 未部署 Collector 的本地开发环境建议置为 false，避免向本机 :4318 周期性连接失败
         */
        private Boolean otlpEnabled;

        /**
         * 是否在响应头回写 X-Trace-Id（兼容既有客户端），默认回写当前 span 的 traceId
         */
        private boolean propagateXTraceId = true;
    }

    @Data
    public static class Logging {

        /**
         * 是否在日志中输出 traceId/spanId 关联 ID
         */
        private boolean correlation = true;

        /**
         * 关联 ID 的日志格式（写回 logging.pattern.correlation），
         * 留空使用 Spring Boot 默认的 [traceId-spanId]
         */
        private String correlationPattern;
    }
}
