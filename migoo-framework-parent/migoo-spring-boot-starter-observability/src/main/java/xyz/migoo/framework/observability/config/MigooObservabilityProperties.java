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

    /**
     * 生命周期配置（K8s / 容器部署）：探针组与优雅停机
     */
    private Lifecycle lifecycle = new Lifecycle();

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

        /**
         * 信号事件是否异步计数。
         *
         * <p>false（默认）= 在发布方线程同步计数（计数本身仅一次 Map/Counter 写，开销可忽略）；
         * true = 投递到模块内置的单线程守护线程池，业务线程立即返回，队列满则丢弃事件
         * （观测永不阻塞业务）。切换后计数存在毫秒级延迟。</p>
         */
        private boolean async = false;

        /**
         * 异步计数的队列容量（{@link #async} = true 时生效），满则丢弃并记录告警
         */
        private int asyncQueueCapacity = 8192;
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

        /**
         * 结构化日志（JSON）输出格式，写回 logging.structured.format.console / .file；
         * {@link Format#OFF}（默认）= 沿用 Spring Boot 纯文本日志
         *
         * <p>三种内置格式（ecs / gelf / logstash）都会把 MDC 的全部键值对
         * （含 traceId/spanId）写入 JSON，故开启后无需额外 encoder 配置。</p>
         */
        private Format format = Format.OFF;
    }

    @Data
    public static class Lifecycle {

        /**
         * 是否默认启用 K8s 存活/就绪探针组（写回 management.endpoint.health.probes.enabled，
         * 使 /actuator/health/liveness 与 /actuator/health/readiness 开箱可用，
         * 供 K8s livenessProbe / readinessProbe 指向）
         */
        private boolean probes = true;

        /**
         * 是否默认启用优雅停机（写回 server.shutdown=graceful：停止接收新请求、
         * 等待在途请求处理完成后再关闭；超时由 spring.lifecycle.timeout-per-shutdown-phase
         * 控制，Spring Boot 默认 30s）。发布期内的滚动更新可避免 502
         */
        private boolean gracefulShutdown = true;
    }

    /**
     * 结构化日志格式（Spring Boot 内置的三种 JSON 格式）
     */
    public enum Format {

        /**
         * 不启用，沿用 Spring Boot 默认的纯文本日志
         */
        OFF,

        /**
         * Elastic Common Schema（ECS）JSON
         */
        ECS,

        /**
         * Graylog Extended Log Format JSON
         */
        GELF,

        /**
         * Logstash JSON（ELK 通用）
         */
        LOGSTASH
    }
}
