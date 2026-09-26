# 可观测性组件（migoo-spring-boot-starter-observability）

> 对应能力评估文档 `capability-assessment.md` 附录「可观测性建设方向」的落地方案。
> 本文既是设计方案（架构、类设计、落地顺序），也是该组件的使用文档（配置项、指标清单）。

## 1. 定位与结论

**一句话：能力收敛到新组件，信号留在原组件。**

| 角色 | 归属 | 依赖 |
|------|------|------|
| **能力**：Actuator/Prometheus 导出、Micrometer Tracing + OTel 桥接、日志关联、信号订阅与计数、健康探针 | `migoo-spring-boot-starter-observability`（新建） | actuator、micrometer-registry-prometheus、spring-boot-starter-opentelemetry |
| **信号**：限流命中、登录失败/锁定/撤销、MQ 失败/死信、500 错误 | 各业务组件（web/security/mq），只发 Spring `ApplicationEvent` | **零 micrometer 依赖** |
| **信号契约**：事件 record、`TraceIdResolver` SPI | `migoo-spring-boot-starter-common` | 纯 POJO，**零 Spring 依赖** |
| **标准埋点**：HTTP/JVM/Tomcat/Logback 等 | Spring Boot Actuator 自动完成 | 零代码 |

三条硬约束（评估结论）：

1. actuator/micrometer/OTel/导出链**不得污染**各业务 starter —— 信号只用 Spring 事件；
2. **统一决策单点**：采样率、OTLP 端点、端点暴露等集中在 `migoo.observability.*`；
3. 现有组件保留：`TraceIdFilter` 本体、业务日志与 `ApiErrorLog` SPI。

**事件契约为什么放 common（而不是 observability 或各组件）**：

- 放 observability → 组件在未引入该 starter 时发布会 `NoClassDefFoundError`，运行期脆弱；
- 放各组件 → observability 必须 optional 反向依赖 web/security/mq/…，POM 随信号源无限增长（Spring Boot Actuator 是这个模式，但代价是几十条 optional 依赖）；
- 放 common → observability 只依赖 common 一条且永不增长，任何组件（含未来 mybatis/websocket/redis 信号）发信号零成本，common 仍是「共享契约 + 工具」定位，且事件是普通 record，**common 不需要引入 spring-context**。

事件采用普通 record 而非 `ApplicationEvent` 子类：Spring 自 4.2 起 `publishEvent(Object)` 接受任意对象，`@EventListener` 按参数类型匹配，无需继承。

## 2. 总体架构

```
┌────────────────────────── 业务应用 ──────────────────────────┐
│  引入 migoo-spring-boot-starter-observability                │
│                                                              │
│  ┌── 信号发出点（各组件，零 micrometer） ──────────────────┐  │
│  │ web: RateLimitAspect → RateLimitExceededEvent          │  │
│  │ web: GlobalExceptionHandler → ServerErrorEvent         │  │
│  │ security: DefaultJwtAuthenticator → Auth*Event          │  │
│  │ mq: StreamListener/RedisMQTemplate → Mq*Event           │  │
│  └───────────────────────┬─────────────────────────────────┘  │
│                          │ ApplicationEventPublisher（同步）  │
│                          ▼                                    │
│  ┌── 能力层（observability starter）───────────────────────┐  │
│  │ ObservabilitySignalConfiguration   事件 → Counter/Timer │  │
│  │ ObservabilityMetricsConfiguration  通用 tag / 端点暴露   │  │
│  │ ObservabilityTraceConfiguration    TraceIdResolver SPI   │  │
│  │ EnvironmentPostProcessor           management.* 单点回写 │  │
│  └───────────┬───────────────────────────────┬──────────────┘  │
│              ▼                               ▼                 │
│   Micrometer MeterRegistry          Micrometer Tracing        │
│   (Prometheus + OTLP)               (OTel bridge + OTLP)      │
│              │                               │                 │
│   Actuator 自动埋点：http.server.requests、   │ traceparent/   │
│   jvm.*、tomcat.*、logback.events、           │ baggage 传播、 │
│   jdbc.*、spring.data.redis.*                 │ MDC 关联日志   │
└───────────────┬───────────────────────────────┬────────────────┘
                ▼                               ▼
     GET /actuator/prometheus          OTLP Collector（可选）
     GET /actuator/health/*            Jaeger / Tempo / Grafana
```

**依赖方向**（全部正向，无反向可选依赖）：

```
observability ──► common ◄── web ──► security
      │              ▲
      └──────────────┴──► mq / mybatis / websocket / redis
```

## 3. 模块与依赖设计

`migoo-framework-parent/migoo-spring-boot-starter-observability/pom.xml`：

| 依赖 | scope | 作用 | 版本来源 |
|------|-------|------|----------|
| `xyz.migoo.springboot:migoo-spring-boot-starter-common` | compile | 信号事件契约 + `TraceIdResolver` SPI 所在，运行期必需 | 仓库 BOM |
| `org.springframework.boot:spring-boot-starter-actuator` | compile | 健康探针、指标端点、自动埋点骨架 | spring-boot-dependencies 4.1.0 |
| `io.micrometer:micrometer-registry-prometheus` | compile | classpath 存在即自动配置 `/actuator/prometheus` | micrometer-bom 1.17.0 |
| `org.springframework.boot:spring-boot-starter-opentelemetry` | compile | Micrometer Tracing 的 OTel 桥接 + OTLP 导出器 | spring-boot-dependencies 4.1.0 |
| `org.springframework.boot:spring-boot-configuration-processor` | optional | 生成 `migoo.observability.*` 配置元数据 | spring-boot-dependencies |
| junit / assertj / mockito | test | 纯单元测试（仓库约定，无 `@SpringBootTest`） | 各 BOM |

`spring-boot-starter-opentelemetry`（Boot 4.1）传递内容已核实：`spring-boot-starter-micrometer-metrics`、`spring-boot-micrometer-tracing-opentelemetry`、`spring-boot-opentelemetry`、`micrometer-tracing-bridge-otel`（runtime）、`opentelemetry-exporter-otlp`（runtime）、`micrometer-registry-otlp`（runtime）—— **Micrometer Tracing + OTel 桥接一步到位**，符合既定选型（非纯 W3C 桥接）。

Boot 4.1 BOM 管理版本：micrometer `1.17.0`、micrometer-tracing `1.7.0`、opentelemetry `1.62.0`、prometheus-client `1.5.1`，**均无需在本仓库写版本号**。

> 注意：`micrometer-registry-otlp` 同时在 classpath，Boot 4.1 默认开启三类 OTLP 导出（链路/指标/日志，默认 `true`、端点指向本机 `:4318`）；无 Collector 时须关闭，见 §5.6 与 §11。

## 4. 类设计

### 4.1 已落地（骨架 + 能力层）

```
migoo-spring-boot-starter-observability/
├── pom.xml                                       # 含资源过滤：build-version.properties 注入框架版本
└── src/main/
    ├── java/xyz/migoo/framework/observability/
    │   ├── config/
    │   │   ├── MiGooObservabilityAutoConfiguration.java    # @AutoConfiguration + @Import 子配置 + @AutoConfigureAfter(tracing)
    │   │   ├── MigooObservabilityProperties.java           # @ConfigurationProperties("migoo.observability")
    │   │   ├── ObservabilityMetricsConfiguration.java      # ② 通用 tag
    │   │   ├── ObservabilityEnvironmentPostProcessor.java  # ② management.*/logging.* 回写
    │   │   ├── ObservabilityTraceConfiguration.java        # ③ TraceIdResolver 装配
    │   │   ├── ObservabilitySignalConfiguration.java       # ④ 信号订阅装配
    │   │   └── ObservabilityHealthConfiguration.java       # ⑤ /actuator/info
    │   ├── trace/MicrometerTraceIdResolver.java            # ③ SPI 实现（依赖 Tracer）
    │   ├── metrics/{SignalMetrics,SignalEventListener}.java # ④ 8 指标 + 事件订阅
    │   └── health/MigooFrameworkInfoContributor.java       # ⑤ 版本 + 模块探测
    └── resources/
        ├── META-INF/spring/
        │   └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
        ├── META-INF/spring.factories                       # EnvironmentPostProcessor 注册（不走自动配置）
        └── xyz/migoo/framework/observability/build-version.properties
```

- `MiGooObservabilityAutoConfiguration`：入口，`@EnableConfigurationProperties(MigooObservabilityProperties.class)`；沿用仓库约定「`@Import` 子配置、不用 `@ComponentScan`」，四个子配置一次性 `@Import`；`@AutoConfigureAfter` 指向 Boot 的 tracing 自动配置（`Tracer` Bean 先于 `TraceIdResolver` 注册条件求值）。
- `MigooObservabilityProperties`：见 §7.1，含 `metrics` / `tracing` / `logging` 三个子配置。
- `ObservabilityEnvironmentPostProcessor`：`EnvironmentPostProcessor` 不参与自动配置，故用 `META-INF/spring.factories`（key `org.springframework.boot.EnvironmentPostProcessor`）注册，`order = HIGHEST_PRECEDENCE + 100`（Boot 的 `ConfigDataEnvironmentPostProcessor.ORDER = HIGHEST_PRECEDENCE + 10`，必须晚于它才能读到 application.yml）。

### 4.2 类清单（✅ 已落地）

| 阶段 | 类 | 职责 | 关键条件 |
|------|----|------|----------|
| ② | `observability.config.ObservabilityMetricsConfiguration` | `MeterRegistryCustomizer<MeterRegistry>` 注入通用 tag（Boot 4.1 该接口位于 `org.springframework.boot.micrometer.metrics.autoconfigure`） | `@ConditionalOnMissingBean` 保护自定义 |
| ② | `observability.config.ObservabilityEnvironmentPostProcessor` | 把 `migoo.observability.*` **回写** `management.*`（端点暴露、采样率、OTLP 端点与三开关）、`logging.*`（关联 pattern / expect-correlation-id），用户显式配置的官方键一律不写 | `EnvironmentPostProcessor` + `spring.factories`，`Binder` relaxed binding，属性源 `addFirst` 且**只含用户未配置的键** |
| ③ | `observability.trace.MicrometerTraceIdResolver` | 取 `tracer.currentSpan().context().traceId()`；无 span / Tracer 异常 → `null`（SPI 契约不抛异常） | 实现 common 的 `TraceIdResolver` |
| ③ | `observability.config.ObservabilityTraceConfiguration` | 注册上行实现 | `@ConditionalOnBean(Tracer.class)` + `@ConditionalOnMissingBean(TraceIdResolver.class)` + `tracing.enabled` |
| ④ | `observability.config.ObservabilitySignalConfiguration` | 装配 `SignalMetrics` 与 `SignalEventListener` | `@ConditionalOnProperty(migoo.observability.metrics.enabled)` |
| ④ | `observability.metrics.SignalMetrics` | 指标名常量 + 懒创建 `Counter`；单信号开关 + 异常只记日志 | 每信号一个开关（§7.1 `metrics.signals`） |
| ④ | `observability.metrics.SignalEventListener` | 8 个 `@EventListener` 按事件类型分方法 → 计数；可选异步（`metrics.async`，内置单线程池） | 同上 |
| ⑤ | `observability.health.ObservabilityHealthConfiguration` | 装配 InfoContributor | `@ConditionalOnClass(InfoContributor.class)` |
| ⑤ | `observability.health.MigooFrameworkInfoContributor` | `/actuator/info` 输出 `version`（资源过滤注入）与 `modules`（按标记类探测 classpath） | 同上 |
| ⑤ | `observability.health.MqBacklogHealthIndicator`（可选，未实现） | 消费组 PEL 积压超阈值 → `OUT_OF_SERVICE` | mq 在 classpath |

### 4.3 信号契约（common，✅ 已落地）

包 `xyz.migoo.framework.common.observability`（全部为普通 record + 1 个接口，零 Spring/micrometer import）：

| 事件 record | 字段（最终实现） | 发布点 | 阶段 |
|-------------|------------------|--------|------|
| `RateLimitExceededEvent` | `path`、`keyType`、`limit` | `web/core/ratelimit/RateLimitAspect#publishRateLimitExceeded`（429 抛出前） | ④ |
| `ServerErrorEvent` | `path`、`method`、`exceptionType`、`message`(截断 200) | `web/core/handler/GlobalExceptionHandler#publishServerError`（500 兜底分支） | ④ |
| `AuthenticationFailedEvent` | `reason`（认证异常简单类名，如 `BadCredentialsException`） | `security/.../DefaultJwtAuthenticator#authenticate` 失败分支 | ④ |
| `AccountLockedEvent` | `reason`（`already_locked` 已锁定拦截 / `failure_threshold` 连续失败达阈值） | 同上（两处锁定分支） | ④ |
| `TokenRevokedEvent` | `scope`（`token` 登出撤销当前令牌 / `user` 踢出用户撤销全部） | security `clean` 与 `revokeUserTokens` | ④ |
| `MqMessageSentEvent` | `stream`（Stream Key 或 Pub/Sub Channel） | `mq/core/RedisMQTemplate#send` 两个重载 | ④ |
| `MqMessageConsumeFailedEvent` | `stream`、`willRetry` | `AbstractStreamMessageListener#handleConsumeError`（ACK 后、重试/死信分支前） | ④ |
| `MqMessageDeadLetteredEvent` | `stream`、`reason`（异常简单类名） | 同类 `sendToDeadLetterQueue`（死信写入成功后） | ④ |
| `TraceIdResolver`（接口） | `String resolve()` 返回当前 traceId，可空、不得抛异常 | 由 observability 实现 | ③ |

发布点统一形态：各信号点持 `ApplicationEventPublisher`（构造注入或 setter，装配类/`MQAutoConfiguration` 传入），私有 `publishEvent/publishQuietly` 方法做 **null 判空 + try/catch 只记日志**，保证「观测不得影响业务」——`RateLimitAspect`、`GlobalExceptionHandler`、`DefaultJwtAuthenticator`、`AbstractStreamMessageListener`、`RedisMQTemplate` 五处一致（各有单测覆盖发布失败不改业务行为）。

**字段基数约束**：事件 tag 只允许低基数字段（`path`/`reason`/`stream`/`exceptionType`/`scope`/`will_retry`），**禁止 `username`、`message` 全文、`clientIp` 进入 tag 语义**——IP 进 tag 会造成时序爆炸；`clientIp` 干脆不进事件（限流处既有 `log.warn` 的 key 已含 IP 维度，登录侧有 `ApiErrorLog`），`message` 只随事件流转不作 tag。

## 5. Tracing 设计

### 5.1 技术选型

Micrometer Tracing + OpenTelemetry 桥接（`spring-boot-starter-opentelemetry`），**不是**自解析 `traceparent` 的纯 W3C 桥接：后者只能传递 trace id、无法产生 span/采样/上报，且要自己实现上下游上下文，得不偿失。Boot 官方支持两种 tracer（OTel+OTLP、Brave+Zipkin），本仓库取 OTel。

### 5.2 现状与冲突

现有 `web/core/filter/TraceIdFilter`：

- 取请求头 `X-Trace-Id`，否则自造 UUID（去横线）；
- `MDC.put("traceId", …)`，响应头回写 `X-Trace-Id`，finally 清理。

引入 Micrometer Tracing 后，**Boot 会默认把 `traceId`/`spanId` 写入 MDC 并在日志中输出关联 ID**（官方文档确认），与 Filter 的 `traceId` 键**同名双写**，结果取决于 filter 顺序，属于不确定行为。

### 5.3 桥接方案（阶段 ③，✅ 已落地）

采用 **SPI + 不覆盖策略**，既解决冲突，又不给 web 引入 micrometer 依赖：

```
common:   TraceIdResolver                    ← 接口（纯 Java）
web:      TraceIdFilter ──ObjectProvider──► TraceIdResolver   （FilterConfiguration 注入，取不到回退现行为）
observability: MicrometerTraceIdResolver implements TraceIdResolver（唯一实现，依赖 Tracer）
```

`TraceIdFilter` 每请求按固定顺序取值（顺序无关的确定性行为）：

1. **MDC 已有**（tracing 已写入 `traceId`）→ 直接沿用，**不覆盖、也不在 finally 清理**（清理归 tracing 的 scope 管理），本过滤器只负责响应头回写；
2. **SPI 解析** → 取当前 span 的 traceId（即 `traceparent` 的真实 trace）；
3. **上游仅给 `X-Trace-Id`** → 沿用，维持旧契约（无法并入真实 trace，保留生成回写用于日志关联）；
4. **都没有** → 自造 UUID（去横线），与引入可观测性组件前的行为一致。

配套规则：

- 自己写入 MDC 的才在 `finally` 清理；链路执行期间 tracing 补写了新值 → 响应头**补写最终生效值**（响应未提交时），保证「响应头 = 最终 MDC 值」；
- `migoo.observability.tracing.propagate-x-trace-id=false` 时只做 MDC 关联、不回写响应头（该开关由 `FilterConfiguration` 的 `@Value` 读取，web 无需依赖 observability 的类）；
- 上游同时给 `X-Trace-Id` 与 `traceparent` → 以 `traceparent` 为准（OTel traceId 是 16 字节 hex，无法把任意字符串升格为 traceId）。

### 5.4 日志关联

- Boot 在 Micrometer Tracing 存在时**默认**输出关联 ID，格式 `[traceId-spanId]`（其 `LogCorrelationEnvironmentPostProcessor` 以后置默认源提供 `logging.expect-correlation-id = management.tracing.export.enabled`，默认 `true`）；
- 自定义用 `logging.pattern.correlation`，例如带应用名：`${spring.application.name:}[%X{traceId:-}] `（尾随空格与 logger 名分隔）；`migoo.observability.logging.correlation-pattern` 即回写该键；
- `migoo.observability.logging.correlation=false` 时环境后处理器回写 `logging.expect-correlation-id=false`（其属性源 `addFirst`，压过 Boot 后置的 `logCorrelation` 默认源，且仍让位于用户显式配置）；
- **结构化（JSON）日志**：`migoo.observability.logging.format` 设为 `ecs` / `gelf` / `logstash`，环境后处理器回写 `logging.structured.format.console` 与 `.file`（用户已配置官方键则让位），默认 `off` 沿用纯文本。Boot 4.1 内置的三种 JSON 格式都会把 **MDC 的全部键值对**（含 `traceId` / `spanId`）写进 JSON，因此**无需再配 logback encoder**；需要对齐 ECS 命名时用 Boot 的 `logging.structured.json.rename.traceId=trace.id` 即可，不再依赖 MDC pattern；
- logback 无需额外 appender（纯文本与 JSON 两种模式均开箱即用）。

### 5.5 传播与采样

| 配置 | 默认 | 说明 |
|------|------|------|
| `management.tracing.sampling.probability` | `0.1`（10%） | 生产建议按 QPS 与后端容量调整 |
| `management.opentelemetry.tracing.sampler` | `parent-based-trace-id-ratio` | 子 span 跟随父采样 |
| 传播格式 | 消费 `[W3C, B3, B3_MULTI]`，生产 `[W3C]` | 键 `management.tracing.propagation.consume` / `.produce`（默认值经 Boot 4.1 元数据实测）；兼容旧服务 B3 头，出站统一 `traceparent` |
| 自动传播 | 仅限自动装配的 `RestTemplateBuilder` / `RestClient.Builder` / `WebClient.Builder` | 手 `new` 的客户端不带链路 |
| `management.tracing.exemplars.include` | `sampled-traces` | Prometheus 指标挂 trace 样本，Grafana 一键跳转 |

### 5.6 OTLP 上报（键与默认值来自 Boot 4.1 `spring-configuration-metadata` 实测）

三类信号各自有开关，**Boot 4.1 默认全部为 `true`，端点未配置时指向本机 `:4318`**：

| 信号 | 开关（默认 `true`） | 端点键 |
|------|---------------------|--------|
| 链路 span | `management.tracing.export.otlp.enabled` | `management.opentelemetry.tracing.export.otlp.endpoint` |
| 指标 | `management.otlp.metrics.export.enabled` | `management.otlp.metrics.export.url` |
| 日志 | `management.logging.export.otlp.enabled` | `management.opentelemetry.logging.export.otlp.endpoint` |

- 本组件 `migoo.observability.tracing.otlp-endpoint` / `otlp-enabled` 单点回写以上开关（✅ 已实现）：`otlp-enabled` 一键覆盖三个开关；`otlp-endpoint` 写链路端点，且当其路径含 `/v1/traces` 时自动推导同主机的 `/v1/metrics`（写 `management.otlp.metrics.export.url`）与 `/v1/logs`（写日志端点），否则只写链路端点、避免猜错 gRPC/HTTP 语义；
- **未部署 Collector 的环境务必 `otlp-enabled: false`**，否则向本机 `http://localhost:4318` 周期性连接失败刷日志；
- 本地开发推荐：只用 Prometheus 拉取 + 日志关联，`otlp-enabled: false`；
- 其他可调项：`management.opentelemetry.tracing.export.*`（批量队列 `max-queue-size=2048`、`schedule-delay=5s`、压缩、超时）。

## 6. Metrics 设计

### 6.1 Prometheus 开箱可用

`micrometer-registry-prometheus` 在 classpath 时 Boot 自动配置抓取端点 `GET /actuator/prometheus`（OpenMetrics/Prometheus 文本格式，支持 Exemplar）。唯一障碍是 Actuator 在 Boot 4.1 中**默认只暴露 `health`**（官方端点文档确认），因此阶段 ② 由 `ObservabilityEnvironmentPostProcessor` 在用户**未显式配置** `management.endpoints.web.exposure.include` 时补入 **`health,info,prometheus`**（`info` 与 §⑤ 的框架 `InfoContributor` 配套）；`migoo.observability.metrics.prometheus-exposure=false` 时不补，回落 Boot 默认 `health`。

### 6.2 自动获得的标准指标（零代码）

引入组件即有：`http.server.requests`（URI/状态码/方法/异常计时）、`jvm.memory.*`、`jvm.gc.*`、`jvm.threads.*`、`jvm.buffer.*`、`process.cpu.*`、`process.uptime`、`tomcat.threads.*`、`logback.events`（按 level 计数）。引入相应组件后出现：`jdbc.connections.*`（Hikari）、`spring.data.redis.command.*`（Lettuce）。以实际 `/actuator/metrics` 输出为准。

### 6.3 框架信号指标清单（阶段 ④ 注册）

Micrometer 名 → Prometheus 导出名（`_total`/单位后缀由注册表自动追加）：

| 信号 | Micrometer 指标名 | 类型 | tags | 来源事件 |
|------|-------------------|------|------|----------|
| 限流拒绝 | `migoo.ratelimit.rejected` | Counter | `path` | `RateLimitExceededEvent` |
| 登录失败 | `migoo.security.login.failed` | Counter | `reason` | `AuthenticationFailedEvent` |
| 账号锁定 | `migoo.security.account.locked` | Counter | `reason` | `AccountLockedEvent` |
| 令牌撤销 | `migoo.security.token.revoked` | Counter | `scope` | `TokenRevokedEvent` |
| 服务端错误 | `migoo.server.error` | Counter | `path`、`exception` | `ServerErrorEvent` |
| MQ 发送 | `migoo.mq.message.sent` | Counter | `stream` | `MqMessageSentEvent` |
| MQ 消费失败 | `migoo.mq.message.consume.failed` | Counter | `stream`、`will_retry` | `MqMessageConsumeFailedEvent` |
| MQ 死信 | `migoo.mq.message.dead.lettered` | Counter | `stream`、`reason` | `MqMessageDeadLetteredEvent` |

通用规则：

- 指标名统一 `migoo.<模块>.<动作>`，点分小写；Prometheus 端自动转下划线；
- `migoo.observability.metrics.signals.<指标名>=false` 可单个关闭（key 即上表的 Micrometer 指标名，如 `migoo.ratelimit.rejected`；未列出的信号跟随 `metrics.enabled`）；
- `migoo.observability.metrics.common-tags` 注入所有指标（建议 `application`、`env`、`region`）；
- **tag 基数护栏**：`path` 用路由模板（`/user/{id}`）而非真实 URL；`clientIp`、`username`、异常 message **不进 tag**；
- 高频 Timer（如需）用 `DistributionSummary`/`Timer` 并配 `sla`，避免无界直方图。

### 6.4 与日志、链路的联动

- Exemplars（§5.5）让 `http_server_requests` 指标携带 traceId，Grafana 从指标直跳 Trace；
- 500 事件计数 + `logback.events{level=error}` + `ServerErrorEvent` 三方对账：指标涨、日志有、链路可查。

## 7. 配置项

### 7.1 `migoo.observability.*`（本组件属性）

| 属性 | 默认 | 说明 |
|------|------|------|
| `migoo.observability.enabled` | `true` | 模块总开关（`@ConditionalOnProperty`） |
| `migoo.observability.metrics.enabled` | `true` | 信号指标订阅开关（关掉后组件不订阅事件，信号点发布的事件无人消费） |
| `migoo.observability.metrics.prometheus-exposure` | `true` | 自动把 `health,info,prometheus` 加入 actuator web 端点暴露清单 |
| `migoo.observability.metrics.common-tags` | `{}` | 全指标通用 tag |
| `migoo.observability.metrics.signals.<指标名>` | 未设置=跟随上级 | 单信号开关，key 为 Micrometer 指标名（如 `migoo.ratelimit.rejected`），见 §6.3 |
| `migoo.observability.metrics.async` | `false` | 信号计数异步化：投递到内置单线程守护线程池（有界队列，满则丢弃并告警，`destroy` 先排空队列）。默认同步——一次 Counter 写入的开销小于线程切换，见 §11 |
| `migoo.observability.metrics.async-queue-capacity` | `8192` | 异步队列容量（`async=true` 时生效） |
| `migoo.observability.tracing.enabled` | `true` | 桥接与 X-Trace-Id 兼容开关 |
| `migoo.observability.tracing.sampling-probability` | 未设置 | 回写 `management.tracing.sampling.probability` |
| `migoo.observability.tracing.otlp-endpoint` | 未设置 | 回写 `management.opentelemetry.tracing.export.otlp.endpoint`；含 `/v1/traces` 时推导 metrics/logs 端点，见 §5.6 |
| `migoo.observability.tracing.otlp-enabled` | 未设置 | 统一回写 `management.tracing.export.otlp.enabled` / `management.otlp.metrics.export.enabled` / `management.logging.export.otlp.enabled`（Boot 4.1 默认均为 `true`），见 §5.6 |
| `migoo.observability.tracing.propagate-x-trace-id` | `true` | 响应头回写 `X-Trace-Id`（由 web 的 `FilterConfiguration` 读取） |
| `migoo.observability.logging.correlation` | `true` | 日志输出 traceId/spanId；为 `false` 时回写 `logging.expect-correlation-id=false` |
| `migoo.observability.logging.correlation-pattern` | 未设置 | 回写 `logging.pattern.correlation` |
| `migoo.observability.logging.format` | `off` | 结构化 JSON 日志：回写 `logging.structured.format.console` 与 `.file`，可选 `ecs` / `gelf` / `logstash`，MDC（traceId/spanId）自动带入，见 §5.4 |

**回写规则**：`management.*` 与 `logging.*` 是 Spring Boot 官方最终事实，用户显式配置时**以用户为准**——`ObservabilityEnvironmentPostProcessor` 遍历属性源判断该键是否已被配置（application.yml、环境变量、命令行等），只对**未配置**的键写默认值；即使它自己的属性源 `addFirst`（需压过 Boot 后置的 `logCorrelation` 默认源），也不会覆盖任何用户配置。`migoo.observability.enabled=false` 时整个处理器不回写。

### 7.2 推荐配置样例

最小可用（**阶段 ② 已完成**，引入组件即有 `/actuator/prometheus` + `/actuator/health` + `/actuator/info`，无需任何配置）：

```yaml
spring:
  application:
    name: demo
```

生产推荐：

```yaml
migoo:
  observability:
    metrics:
      prometheus-exposure: true
      common-tags:
        application: ${spring.application.name}
        env: ${PROFILE:dev}
    tracing:
      sampling-probability: 0.1
      otlp-enabled: true
      otlp-endpoint: http://otel-collector:4318/v1/traces
      propagate-x-trace-id: true
    logging:
      correlation: true
      format: logstash              # 结构化 JSON（可选 ecs/gelf/logstash），traceId/spanId 自动进 JSON

management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus        # 与 prometheus-exposure 二选一，显式配置优先
  endpoint:
    health:
      probes:
        enabled: true                          # /actuator/health/liveness、/readiness（K8s 探针）
  tracing:
    exemplars:
      include: sampled-traces
  metrics:
    tags:
      application: ${spring.application.name}  # 亦可用上表 common-tags 等价表达
```

本地开发（无 Collector）：

```yaml
migoo:
  observability:
    tracing:
      otlp-enabled: false        # 避免周期性连接失败日志
      sampling-probability: 1.0  # 排查时全采样
```

Prometheus 抓取配置：

```yaml
scrape_configs:
  - job_name: migoo-app
    metrics_path: /actuator/prometheus
    static_configs:
      - targets: ['demo:8080']
```

> 安全提醒：`/actuator/*` 需用 Security 配置限制来源（内网/网关白名单），勿直接暴露公网。

## 8. 落地顺序

沿用评估结论的 ①-⑤，每步独立可验收：

### ① 新模块骨架（✅ 本次交付）

- [x] `migoo-framework-parent/migoo-spring-boot-starter-observability/`（pom、`MiGooObservabilityAutoConfiguration`、`MigooObservabilityProperties`、`AutoConfiguration.imports`、属性单测）
- [x] 父 pom `<modules>` 增加一行
- [x] BOM `migoo-framework-dependencies` 增加 `dependencyManagement`
- [x] `.github/workflows/publish-parent.yml`：`-pl` 清单 + summary **补上 observability，并修复历史遗漏的 websocket**
- [x] `readme.md` 组件表格/结构树、`docs/index.md` 表格与引入示例同步（同样补 websocket）
- [x] 删除 common 中未使用的 `micrometer-core`（评估点名的瘦身项）

验收：`mvn clean verify` 全绿 ✅（11 模块，905 个用例，其中新模块 `MigooObservabilityPropertiesTest` 3 个）。本机未装 gnupg 时需加 `-Dgpg.skip=true`（签名校验见 §10）。

### ② Actuator + Micrometer 导出（✅ 代码已交付）

- [x] `ObservabilityMetricsConfiguration`（通用 tag、`@ConditionalOnMissingBean`）
- [x] `ObservabilityEnvironmentPostProcessor`（端点/采样/OTLP/关联回写）+ `META-INF/spring.factories` 注册（`order = HIGHEST_PRECEDENCE + 100`）
- 验收：单元测试已覆盖回写规则（补 `health,info,prometheus`、用户配置让位、单点开关联动）；样例应用 `curl localhost:8080/actuator/prometheus` 返回 `http_server_requests`、`jvm_memory_used` 等 —— 待接入样例应用后补实测输出

### ③ `traceparent`↔MDC 桥接 + 日志关联（✅ 代码已交付）

- [x] common：`TraceIdResolver` 接口（可空、不抛异常契约）
- [x] web：`TraceIdFilter` 注入 `ObjectProvider<TraceIdResolver>`、MDC 不覆盖/不清理策略、响应头补写最终值；`FilterConfiguration` 读取 `propagate-x-trace-id`
- [x] observability：`ObservabilityTraceConfiguration` + `MicrometerTraceIdResolver`；OTLP/采样/关联回写（`@AutoConfigureAfter` 保证 `Tracer` 先注册）
- 验收：单测覆盖取值顺序与响应头一致性；上下游两服务同 trace、日志含 `[traceId-spanId]`、旧客户端 `X-Trace-Id` 仍可查 —— 待样例应用实测

### ④ 信号事件订阅计数（✅ 代码已交付）

- [x] common：8 个事件 record（§4.3）
- [x] web/security/mq：5 处埋点（`RateLimitAspect`、`GlobalExceptionHandler`、`DefaultJwtAuthenticator`、`AbstractStreamMessageListener`、`RedisMQTemplate`），各带判空 + try/catch
- [x] observability：`SignalMetrics`（8 指标 + 单信号开关）+ `SignalEventListener` + `ObservabilitySignalConfiguration`
- 验收：`SignalEventListenerTest` 逐一投喂 8 事件断言计数与 tag、开关生效、注册表故障不上抛；各组件埋点用 `ArgumentCaptor` 断言字段；Prometheus 实测数值递增待样例应用

### ⑤ InfoContributor + 文档（✅ 代码已交付，实测输出待补）

- [x] `MigooFrameworkInfoContributor`（版本经资源过滤注入、模块按标记类探测）+ `ObservabilityHealthConfiguration`
- [ ] （可选）`MqBacklogHealthIndicator`
- [x] 本文档随实现回改（暴露默认值、事件字段、回写键、测试清单）
- [ ] 接入样例应用后补实测输出（`/actuator/info`、`/actuator/prometheus` 样例、时序图）

> 全量验收：`mvn clean verify -Dgpg.skip=true` 全绿 —— **11 模块、944 个用例、0 失败**（较骨架阶段 905 个新增 39 个）。

## 9. 测试清单（仓库约定：纯单元测试，junit/assertj/mockito，无 `@SpringBootTest`）

| 用例 | 断言 | 状态 |
|------|------|------|
| `MigooObservabilityPropertiesTest` | 三组子配置默认值（含 `async=false`、`async-queue-capacity=8192`、`format=off`）、setter 绑定、每实例独立 | ✅ 3 用例 |
| ② `ObservabilityMetricsConfigurationTest` | 直接调 `@Bean` 方法：通用 tag 对新建指标生效；空 map 时无副作用（`@ConditionalOnMissingBean` 属自动配置条件，纯单测不覆盖） | ✅ 2 用例 |
| ② `ObservabilityEnvironmentPostProcessorTest` | 未配置 → 补 `health,info,prometheus`；用户已配置 exposure/采样 → 不覆盖；`otlp-enabled` 三开关联动且显式配置的键让位；`otlp-endpoint` 推导 metrics/logs；`correlation=false` 压过 Boot `logCorrelation` 源；`format` 回写 console+file 且官方键让位、`off` 不写；`enabled=false` 一概不写 | ✅ 14 用例 |
| ③ `MicrometerTraceIdResolverTest` | mock `Tracer`/`TraceContext` 返回 traceId；无 span 返回 `null`；Tracer 抛异常不外抛（SPI 契约） | ✅ 3 用例 |
| ③ `TraceIdFilterTest`（web 模块） | 取值顺序（MDC → resolver → 上游头 → UUID）、resolver 优先于 `X-Trace-Id`、不覆盖/不清理 tracing 写入的 MDC、响应头 = 最终 MDC 值、`propagate=false` 不回写、异常路径 finally 清理 | ✅ 10 用例 |
| ④ `SignalEventListenerTest` | 逐一投喂 8 个事件 record，`SimpleMeterRegistry` 断言计数与 tag；同事件累加；`metrics.signals.<指标名>=false` 不计数且不影响其他信号；空值兜底 `unknown`；注册表故障不抛给发布方；**异步**：关闭时排空队列后计数可确定性断言、运行在 `migoo-observability-signal-*` 命名守护线程、故障与重复 `destroy` 幂等 | ✅ 8 用例 |
| ④ 埋点单测（各组件） | `ArgumentCaptor`/`verify` 断言字段：限流（`RateLimitAspectTest` +2）、500（`GlobalExceptionHandlerTest` +1）、登录失败/锁定/撤销（`DefaultJwtAuthenticatorTest` +6）、MQ 失败/死信（`AbstractStreamMessageListenerTest`）、发送（`RedisMQTemplateTest` +3）；另覆盖**发布失败不改变业务行为** | ✅ |
| ⑤ `MigooFrameworkInfoContributorTest` | `Info.Builder` 输出 `migoo.version`（非占位符）与 `modules`（按测试类路径探测到 common/observability、探测不到 web） | ✅ 1 用例 |

## 10. 发布与接线清单

**教训**：`websocket` 模块曾同时遗漏于发布 `-pl` 清单与 readme 结构树——组件新增必须**一次性**核对以下全部位置，否则「本地能用、Central 拿不到」：

- [x] `migoo-framework-parent/pom.xml` → `<modules>`
- [x] `migoo-framework-dependencies/pom.xml` → `<dependencyManagement>`
- [x] `.github/workflows/publish-parent.yml` → `Publish Sub-modules Only` 的 `-pl`（80 行）与 `Publication Summary`（92 行）
- [x] `readme.md` → 组件文档表格、项目结构树
- [x] `docs/index.md` → 组件表格、按需引入示例
- [x] 各 pom 版本号 `1.3.18` 全仓一致（硬编码，发版需全量替换）
- [x] 新模块 pom 含 `name/description/url/licenses/scm/developers`（Central 发布必填）
- [x] `mvn clean verify` 全绿后再打 tag
- [x] **机器校验**：`scripts/check-publish-modules.sh` 比对四方清单（父 pom `<modules>` / BOM `dependencyManagement` / `publish-parent.yml` 的 `-pl` / `Publication Summary` 汇总行），`publish-parent.yml` 与 `publish-dependencies.yml` 发布前各执行一次，任一漏项即 fail；本地可随时 `bash scripts/check-publish-modules.sh`
- [x] 本地构建注意：父 pom 把 `maven-gpg-plugin:sign` 绑在 `verify` 阶段，无 gpg 的机器用 `mvn clean verify -Dgpg.skip=true`（CI 发布流程照常签名）

## 11. 风险与取舍

| 风险 | 影响 | 处置 |
|------|------|------|
| 三类 OTLP 导出（tracing/metrics/logging）Boot 4.1 默认全开、端点指向本机 `:4318` | 无 Collector 时周期性连接失败刷日志 | ✅ ② 由 `otlp-enabled` 一键关闭三处（单测覆盖），文档给本地开发样例 |
| Actuator 默认只暴露 `health` | `/actuator/prometheus` 404，违背「开箱可用」 | ✅ ② `prometheus-exposure` 默认补 `health,info,prometheus`（用户显式配置优先，11 个单测覆盖） |
| `traceId` MDC 双写（Filter vs Tracing） | 日志 traceId 不确定 | ✅ ③ 不覆盖策略 + SPI 解耦依赖（`TraceIdFilterTest` 10 用例，含链路中补写场景） |
| 采样率默认 0.1 | 排障时 90% 请求无 trace | 文档给 `sampling-probability: 1.0` 排障样例 |
| tag 基数失控（URI/IP/username） | Prometheus 时序爆炸、内存涨 | §6.3 护栏 + 评审新指标必查 tags（`path` 取 `HandlerMapping` 最佳匹配模板，兜底 URI → `unknown`） |
| 事件监听同步执行 | 发布点变慢/异常 | ✅ 发布点 try/catch、监听器（`SignalMetrics`）内部捕获，均有「发布失败不改业务行为」单测；另提供 `metrics.async=true` 异步选项（内置单线程守护线程池 + 有界队列，满则丢弃告警、关闭时先排空，8 用例覆盖），默认同步——一次 Counter 写入开销小于线程切换 |
| 事件契约进 common | common 语义从纯工具扩展为「工具 + 跨模块契约」 | 已在 §1 记录取舍；事件零 Spring 依赖，common 仍保持零 Spring 依赖 |
| 新模块发布遗漏 | 用户引不到（websocket 教训） | ✅ §10 检查表 + `scripts/check-publish-modules.sh` 机器校验（两个发布 workflow 前置执行，三类漏项均有反向验证） |

## 12. 关联阅读

- `capability-assessment.md` —— 能力评估与缺口分级（本方案的输入）
- [Spring Boot Tracing](https://docs.spring.io/spring-boot/reference/actuator/tracing.html)
- [Spring Boot Metrics（Prometheus 章节）](https://docs.spring.io/spring-boot/reference/actuator/metrics.html)
- 组件文档索引：[docs/index.md](index.md)
