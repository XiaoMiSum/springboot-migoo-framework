# springboot-migoo-framework 能力盘点与现代应用匹配度评估

> 评估日期：2026-09-25 ｜ 项目版本：1.3.18
> 评估方法：两轮全模块源码只读盘点（7 个 starter + 工程基线/CI/DX）+ 全量测试回归（902 用例全绿，BUILD SUCCESS 10/10 模块）
> 说明：路径均为相对项目根；「未发现」表示全仓 grep/glob 零命中，证据以源码为准。

---

## 一、评估框架（现代应用八维基线，2026 视角）

| # | 维度 | 关注点 |
|---|------|--------|
| 1 | 运行时先进性 | Java 21（虚拟线程）、Spring Boot 版本、AOT/native 就绪 |
| 2 | 可观测性 | TraceId 是否对接 W3C/OTel、Metrics（Micrometer）、结构化日志、审计 |
| 3 | 安全纵深 | Token 撤销、锁定/限流、2FA、OIDC SSO/Passkey、密码策略、安全响应头 |
| 4 | 开放标准契约 | 统一响应是否兼容 RFC 9457、OpenAPI 文档、i18n、校验 |
| 5 | 流量与弹性 | 限流（应用内 vs 网关协同）、熔断/重试/幂等、背压 |
| 6 | 数据与中间件 | 缓存原语、分布式锁、MQ 可靠性（重试/DLQ/发件箱）、多数据源/多租户 |
| 7 | 云原生适配 | 无状态、共享状态外置（StateStore SPI）、探针/优雅停机、12-factor 配置 |
| 8 | 开发者体验 | Starter + 条件装配、BOM、测试支持、文档、CI/代码规范 |

---

## 二、模块能力盘点

### 2.1 migoo-spring-boot-starter-common

包结构：`core/`、`enums/`、`exception/`、`pojo/`、`util/`、`validation/` —— 共 43 个公开类型；**无 `src/main/resources`**（不提供自动配置）。

| 能力 | 有/无 | 证据 |
|---|---|---|
| 统一响应/错误码/异常体系 | **有** | `pojo/Result.java`、`exception/ErrorCode.java`（record）、`GlobalErrorCodeConstants.java`（200/400/401/403/404/405/423/429/500/900/999）、`ServiceErrorCodeRange.java`、`ServiceException.java`、`ServiceExceptionUtil.java` |
| 分页模型 | **有** | `pojo/PageParam.java`（`@Min(1)`/`@Max(100)`）、`SortablePageParam.java`、`SortField.java`、`PageResult.java`、`util/object/PageUtils.java` |
| 参数校验 | **有** | `validation/{Mobile,Email,Password,InEnum}.java` + 对应 `*Validator.java`；`util/ValidationUtils.java` |
| JSON | **有** | `util/JsonUtils.java`；Jackson 3（`tools.jackson.core:jackson-databind`，BOM 3.1.0） |
| 日期 | **有** | `util/date/DateUtils.java`、`util/date/LocalDateTimeUtils.java` |
| 签名 | **有（仅工具）** | `util/crypto/SignatureUtils.java`（Map 拼串 MD5 签验签、`@SignIgnore`）、`util/crypto/RsaUtils.java`（SHA256withRSA 等） |
| 哈希/文件 | **有** | `util/FileUtils.java`（SHA-256）、`util/io/FileTypeUtils.java` |
| 本地缓存 | **有** | `util/CacheUtils.java`（Guava `LoadingCache` + `asyncReloading` + TTL 线程池） |
| HTTP | **弱** | `util/HttpUtils.java` **仅做 URL 查询参数替换/解析**，无 HTTP 客户端、无连接池、无重试 |
| 国际化 | **无（在 web）** | `migoo-spring-boot-starter-web/.../i18n/I18NMessage.java`、`I18NLocaleResolver.java`、`I18nConfiguration.java` |
| 分布式 ID | **无** | grep `Snowflake\|IdGenerator\|DistributedId` 仅命中 `BaseAutoIncDO` 的 `IdType.AUTO`；UUIDv7 在 mybatis 模块 |
| 幂等 | **无** | `REPEATED_REQUESTS`（900）**全仓库无调用点**（仅测试断言） |
| 脱敏 | **无** | grep `Sensitive\|Desensit\|mask` 仅命中 `DefaultJwtAuthenticator.java` 的日志私有 `maskToken()` |
| 限流 | **无（在 web）** | `web/.../annotation/RateLimit.java`、`core/ratelimit/*`、`config/RateLimitConfiguration.java` |
| 对称加密（AES/SM4/国密） | **无** | common 内零命中（AES 只在 mybatis `EncryptTypeHandler`）；BouncyCastle 在 pom 但 main 未用 |
| 指标 | **依赖有、代码无** | pom 引 `micrometer-core`，main 代码 `io.micrometer` 零命中 |
| Bean 拷贝 | **无** | 全仓库 `BeanUtils` 不存在 |

**缺口**：分布式 ID、HTTP 幂等、脱敏注解、对称/国密加密、HTTP 客户端、BeanUtils 均缺失；签名工具**无框架级接线**（仅测试调用）；`micrometer-core`、`bouncycastle` 为未使用依赖（瘦身点）。

**文档漂移**：`docs/common.md:126,136,137` 引用 `BeanUtils.toBean`、`:140` 引用 `EncryptTypeHandler.encrypt`、`:143` 引用 `RSA.sign` —— 代码中均无对应类。

### 2.2 migoo-spring-boot-starter-redis

主代码仅 3 类：`config/RedisAutoConfiguration.java`、`core/RedisKit.java`（932 行）、`core/RedisKeyDefine.java`。

| 能力 | 有/无 | 证据 |
|---|---|---|
| RedisTemplate 封装 | **有** | `@Primary RedisTemplate<String,Object>`，key/value 均 String 序列化（手工 JSON、无类型元数据），`@AutoConfigureBefore(DataRedisAutoConfiguration)`（SB4 新包 `org.springframework.boot.data.redis.autoconfigure`） |
| 类型安全 Key 定义 | **有** | `RedisKeyDefine`：`keyTemplate` + `TypeReference<T>` + `TimeoutType{PERMANENT,FIXED,DYNAMIC}` + Builder |
| 缓存读写/过期 | **有** | `get/set/setIfAbsent/delete/hasKey/expire`；FIXED TTL 用 Lua（`SET_FIXED_SCRIPT`，仅首设 TTL） |
| 原子计数 | **有** | `increment/decrement`（Long/Double），配 7 段 Lua（INCR_FIXED/DYNAMIC、INCRBY_*、DECRBY_*） |
| 分布式锁（基础） | **有** | `tryLock` = `SET NX EX`；`unlock` = Lua 比对 value 后 `DEL` |
| ZSet/Hash/List | **有** | ZSet 全套；Hash 全套；List + 阻塞 `blPop/brPop` |
| 发布订阅 | **无（在 mq）** | RedisKit 无 `convertAndSend/subscribe` |
| 管道/事务 | **无** | grep `executePipelined\|MULTI` 零命中 |
| 锁高级特性 | **无** | 无看门狗续期/可重入/阻塞重试/红锁；**Redisson 仅在 BOM**（`migoo-framework-dependencies/pom.xml:186-188`），starter 均未依赖 |
| 布隆过滤器/延迟队列 | **无** | 零命中 |
| `@Cacheable`/Spring Cache 集成 | **无** | grep `CacheManager\|EnableCaching\|@Cacheable\|RedisCacheConfiguration` 零命中 |
| Set/Geo/Bitmap/HyperLogLog | **无** | — |

配置键全部走标准 `spring.data.redis.*`（单机/主从/哨兵/集群，见 `docs/redis.md:51-194`），本模块无自有配置前缀。

**缺口**：Spring Cache 注解集成缺失；锁无 watchdog/重入/等待（Redisson 已在 BOM 未接线）；无管道/集合/位图/Geo；无布隆/延迟队列/滑动窗口原语；Hash 存 POJO 强转类型安全弱；无穿透/击穿/雪崩模式封装。

### 2.3 migoo-spring-boot-starter-mq

文件：`config/{MQProperties,MQAutoConfiguration}.java`、`core/RedisMQTemplate.java`、`core/message/AbstractMessage.java`、`core/stream/*`、`core/pubsub/*`、`core/interceptor/*`。

| 能力 | 有/无 | 证据 |
|---|---|---|
| API 形态 | **有** | `RedisMQTemplate.send(AbstractChannelMessage)`（Pub/Sub）/ `send(AbstractStreamMessage)`（Stream，返回 `RecordId`）；channel 默认类名 |
| 消费者组 | **有** | `createConsumerGroup()`（BUSYGROUP 容错）、`Consumer.from(group, IP@pid)`、`ReadOffset.lastConsumed()`、`autoAcknowledge(false)`、`batchSize(10)` **硬编码**（`MQAutoConfiguration.java:135`） |
| 消息确认 ACK | **有** | `AbstractStreamMessageListener.java:107` 成功后 `acknowledge`；`migoo.mq.delete-after-ack`（默认 false） |
| 重试 | **有** | `retry-count` header，超过 `migoo.mq.max-retry`（默认 3）转死信；**失败先 ACK 再重新 add** |
| 死信队列 | **有** | `DEAD_LETTER_SUFFIX=":dead_letter"`，附 `error-message/error-time`；`migoo.mq.dead-letter-enabled`（默认 true） |
| 幂等 | **有** | `IdempotentMessageInterceptor`：key `mq:idempotent:{channel}:{messageId}`，Lua 原子 setnx+TTL；成功打标、失败删标允许重试、已消费直接 ACK 不计重试；`migoo.mq.idempotent.enabled`（默认 true）、`expire-time`（24h） |
| 消息标识 | **有** | `AbstractMessage` 构造器 `UUID.randomUUID()` |
| 拦截器扩展点 | **有** | 6 个钩子（send/consume × before/after/error），`CopyOnWriteArrayList` 拦截器链 |
| 配置项 | **有** | `MQProperties`（prefix `migoo.mq`）：`group/max-retry/dead-letter-enabled/delete-after-ack/idempotent.*` |
| 本地消息表/事务性发件箱 | **无** | grep `outbox\|发件箱\|本地消息表\|TransactionalMessage` 零命中；`send()` 直接写 Redis |
| Pub/Sub 可靠性 | **无（明示）** | `AbstractChannelMessageListener.java:67` 注释「Pub/Sub 不支持重试，仅记录日志」；`docs/mq.md:138`「无 ACK、无重试」 |
| 退避/延迟重试 | **无** | 重试为立即重投，无 backoff |
| 孤儿消息认领 | **无** | grep `xautoclaim\|XPENDING\|pending` 于 parent **零命中**——消费者崩溃后 in-flight 消息永不认领 |
| 顺序/延迟消息/轨迹/限速 | **无** | — |

**缺口（可靠性硬伤标 ⚠）**：⚠ 无 XPENDING/XAUTOCLAIM；⚠ 无退避重试（雪崩风险）；⚠ 无事务性发件箱（丢消息窗口）；Pub/Sub 路径无 ACK/重试/持久化；单一全局消费组 `migoo.mq.group`；无业务键幂等、无死信重投 API、batch/并发不可配、无指标埋点。

### 2.4 migoo-spring-boot-starter-mybatis

主代码：`config/MybatisAutoConfiguration.java`、`core/{BaseMapperX, LambdaQueryWrapperX, QueryWrapperX, LambdaUpdateWrapperX, MPJLambdaWrapperX}`、`core/dataobject/{BaseDO, BaseUuidDO, BaseAutoIncDO}`、`core/handler/*`、`core/util/*`。

| 能力 | 有/无 | 证据 |
|---|---|---|
| SB4 / MP 集成 | **有** | `pom.xml:73-81` 依赖 `mybatis-plus-spring-boot4-starter` + `mybatis-plus-jsqlparser`；`@AutoConfiguration(before=MybatisPlusAutoConfiguration)` + `@MapperScan`；BOM：SB 4.1.0、MP 3.5.16、MPJ 1.5.6 |
| 分页 | **有** | `PaginationInnerInterceptor`（`MybatisAutoConfiguration.java:34`）；`MyBatisUtils.buildPage`；`BaseMapperX.selectPage/selectJoinPage → PageResult` |
| 逻辑删除 | **有** | `BaseDO.isDeleted` `@TableLogic` + `@TableField(fill=INSERT)` |
| 自动填充 | **有** | `DefaultFieldHandler`：`createdAt/updatedAt/isDeleted`；`BaseUuidDO` 主键缺省 **UUIDv7**（`UuidCreator.getTimeOrderedEpoch()`） |
| 审计字段 | **部分** | 仅时间戳+删除标记；**无 createBy/updateBy 操作人** |
| 字段加密 | **有（基础）** | `EncryptTypeHandler`：AES/ECB/PKCS5 + **MD5 派生密钥**，密钥读 `mybatis-plus.encryptor.password`（System property / env） |
| 时区 | **有** | `UTCLocalDateTimeHandler` 全局注册，DB 统一 UTC |
| 多数据源 | **不内置（可插拔）** | `docs/mybatis.md:236-295` 指引引入 `dynamic-datasource-spring-boot4-starter` + `@DS`，版本在 BOM |
| 动态表名 | **无** | 零命中 |
| 租户 | **无** | grep `tenant` 零命中 |
| 乐观锁/防全表/SQL 审计 | **无** | `mybatisPlusInterceptor()` **只装了分页**（L31-36） |
| 查询增强 | **有** | `LambdaQueryWrapperX` 空值跳过 + `or/and(Consumer)`；`MPJLambdaWrapperX` 联表；`BaseMapperX` 批量便捷方法 |

**缺口**：租户隔离与动态表名缺失；插件面过窄（无乐观锁/防全表/慢查询）；加密偏弱（ECB、MD5 派生、密钥进程内可读、无轮换）；审计缺操作人；多数据源是「文档级能力」；无分布式 ID 插件。

**文档漂移**：`docs/mybatis.md:148` 的 `JsonLongSetTypeHandler` 不存在（实际为 String/Integer/LongList）。

### 2.5 migoo-spring-boot-starter-web

自动配置入口：`AutoConfiguration.imports`（3 条目）→ `MiGooWebAutoConfiguration` 通过 `@Import` 装配 7 个子配置。

已知项（全局异常、Result、TraceId、i18n、CORS、请求体缓存、限流）之外：

1. **Jackson 3 定制**：`jackson/config/JacksonAutoConfiguration.java` + `LocalDateTimeSerializer|Deserializer`、`BigDecimalSerializer`
2. **虚拟线程**：`config/VirtualThreadConfiguration.java`（Tomcat protocol handler 换 `ofVirtual()`）
3. **响应体增强**：`ResponseBodyStorageAdvice`（Result 存 request attribute）、`ResponseBodyI18nAdvice`（`Result.msg` 自动国际化）
4. **API 错误日志 SPI**：`apilog/core/ApiErrorLog.java` + `ApiErrorLogFrameworkService.java`，`GlobalExceptionHandler` L235 在 500 时回调，需应用落库（`docs/web.md:62-75`）
5. **StateStore 抽象**：`core/store/StateStore|InMemoryStateStore|RedisStateStore` + `MiGooWebRedisStateStoreAutoConfiguration`（Redis 在 classpath 自动切分布式）
6. **`@RateLimit` + 切面**：`RateLimitAspect`（全仓唯一 `@Aspect`）
7. 过滤器顺序常量：common 的 `enums/WebFilterOrderEnum.java`

**未发现（明确）**：OpenAPI/springdoc/swagger、数据脱敏注解、文件上传封装、审计日志 AOP、HTTP 接口幂等（幂等仅在 mq 模块）。

**TraceId 不对接标准**：`TraceIdFilter.java` L16-30 —— header `X-Trace-Id`，缺失时 UUID 无横线，写 MDC key `traceId` 并回写响应头。全仓 grep `traceparent|W3C|baggage|Span|Tracer` **0 命中**；无 `opentelemetry-api`；无 `logback-spring.xml`；跨消息仅测试出现一个 `trace-id` 字符串头。

### 2.6 migoo-spring-boot-starter-security

自动配置：`AutoConfiguration.imports`（3 类）。

已知项之外：

1. **双模式**：`SecurityProperties.SecurityMode.JWT | OAUTH2`（L27、L288-297）；OAuth2 仅 resource-server（`OAuth2ResourceServerAutoConfiguration.java:31-43`，issuer-uri 自动发现 / jwk-set-uri）
2. **refresh token**：`accessTokenExpires=30m`、`refreshTokenExpires=7d`、`refreshHeaderName=X-Refresh-Token`（L99-120）
3. **登录锁定三策略**：fixed/incremental/sliding-window（L146-283 + `core/lockout/*`），`MiGooSecurityAutoConfiguration.java:165-213` 注册，底层用 web 的 `StateStore`
4. **token 撤销钩子**：`UserDetailsBridge.isTokenRevoked/isUserRevoked/revokeByUserId`、`AuthUserDetailsFetcher.revokeToken/revokeUserTokens`、`DefaultJwtAuthenticator.requireNotRevoked` L200-203
5. **密码编码**：BCrypt（L106-109）
6. **TOTP**：`TotpAuthenticator` + `@RequiresTotp` + `TotpInterceptor`，`addInterceptors` 注册到 `/**`（L58-61）

| 能力 | 结论 | 证据 |
|---|---|---|
| OIDC 登录客户端（第三方 SSO） | **未发现** | security pom 只有 `oauth2-resource-server`（L78-81）；grep `oauth2-client\|oauth2Login\|ClientRegistration` 0 |
| Passkey / WebAuthn | **未发现** | 零命中 |
| 密码强度 | **有（在 common）** | `validation/Password.java` + `PasswordValidator.java`（字母+数字+特殊字符+长度） |
| 密码过期/历史 | **未发现** | 零命中 |
| 审计日志 | **未发现** | 仅登录失败计数与错误日志 SPI |
| `@PreAuthorize` | **有** | `MiGooWebSecurityFilterChainConfiguration.java:33` `@EnableMethodSecurity(securedEnabled=true)`（prePost 默认开）；`docs/security.md:244-245,354` |
| CSRF | **显式禁用** | 同文件 L50-51（token 无 session） |
| 安全响应头 | **显式禁用** | 同文件 L54-55 `.headers(...disable)` —— HSTS/CSP/X-Frame 等默认头全关 |
| 多租户 | **未发现** | grep `tenant` 0 |

其他：`permit-all-urls`、`logout-url` 可配；`SessionCreationPolicy.STATELESS` + permitAll 分发类型放行。

### 2.7 migoo-spring-boot-starter-websocket

| 能力 | 结论 | 证据 |
|---|---|---|
| 认证 | **有** —— 握手期 token 校验（Header 或 query） | `WebSocketAuthInterceptor.java:46-113`，复用 security `AuthUserDetailsFetcher.verifyToken`，通过写 session attributes |
| 条件装配 | 仅 security 存在时注册 | `WebSocketSecurityConfiguration.java:19-21`（`@ConditionalOnClass` + `@ConditionalOnBean`） |
| 本地会话 | **有** | `LocalWebSocketSessionManager` + `AbstractWebSocketSessionManager` |
| 分布式会话 | **有** —— Redis Pub/Sub 跨节点 | `migoo.websocket.distributed=true` → `DistributedWebSocketSessionManager`（L51 `ChannelTopic`，消息格式 `BINARY:userId:base64` L282） |
| 广播 | **有** —— 用户/会话/全局/房间/二进制 | `WebSocketSessionManager.java:66-221`（`sendToUser/sendToSession/broadcast/房间/在线统计`） |
| 心跳 | **未发现** | Handler 无 ping/pong、无定时任务；**`WebSocketProperties.maxSessionTimeout=1800000` 是死配置**（定义了没有任何逻辑消费） |
| STOMP / SockJS | **未发现** | 仅 `@EnableWebSocket` 原生 |
| 多端点 | 有 | `WebSocketProperties.endpoints` + `allowedOrigins` |

---

## 三、工程基线

| 能力 | 结论 | 证据 |
|---|---|---|
| Spring Boot | **4.1.0** | `migoo-framework-dependencies/pom.xml:40` |
| Java | **21** | `migoo-framework-parent/pom.xml:58-59`（compiler source/target=21）；CI 用 JDK21 |
| BOM | **自建 + 导入 Spring BOM** | `migoo-framework-dependencies/pom.xml:81-112`：spring-boot-dependencies、jackson-bom 3.1.0、jackson 2.21.2、**spring-ai-bom 2.0.0（仓库内无任何 spring-ai 代码使用）** |
| actuator | **未发现** | 全仓 0 命中 |
| micrometer | **仅声明未使用** | common pom L109-113 `micrometer-core`；java 代码 `MeterRegistry|Metrics.` 0 命中；无 registry 实现、无导出 |
| OpenTelemetry | **未发现** | 0 命中 |
| springdoc/OpenAPI/swagger | **未发现** | 0 命中 |
| resilience4j / spring-retry | **未发现** | 所有 pom `retry` 0 命中（MQ 重试为自研） |
| GraalVM native / AOT | **未发现** | 无 native-maven-plugin、无 reflect-config/aot hints |
| 构建插件 | 有 | compiler 3.13.0（`<parameters>true</>` 供 `@RateLimit("#username")` SpEL）、surefire 3.5.2、gpg、central-publishing |
| 死仓库残留 | ⚠ | `migoo-framework-parent/pom.xml:207`、`migoo-framework-dependencies/pom.xml:349` 仍声明 **jcenter** |

### 测试与 DX

| 项 | 结论 | 证据 |
|---|---|---|
| 单元测试 | **有 —— 约 108 个测试类，纯单元测试**（本次回归 902 用例全绿） | common 43、web 22、mybatis 14、security 12、mq 10、websocket 5、redis 2；依赖仅 junit/assertj/mockito |
| 测试切片 / 上下文测试 | **未发现** | grep `ApplicationContextRunner\|@SpringBootTest\|@WebMvcTest` 0 命中；pom 无 `spring-boot-starter-test` |
| 测试资源 | **未发现** | `**/src/test/resources/**` 0 命中 |
| 示例工程 | **未发现** | 无 demo/example 模块 |
| 文档覆盖 | **有 —— 7 模块 + 首页** | `docs/`：index/common/web/security/websocket/mybatis/redis/mq + `_config.yml`；**readme.md L104-111 文档表格漏列 websocket** |
| 配置元数据补充 | 无手工 | 仅 `spring-boot-configuration-processor` 自动生成 |
| CI | **仅发布流水线** | `.github/workflows/` 只有 `publish-dependencies.yml`、`publish-parent.yml`（release/workflow_dispatch 触发，先 `mvn clean verify` 再 `deploy -DskipTests`）；**无 push/pull_request 触发** |
| CI 漂移 | ⚠ | release profile 定义在 `.github/settings.xml:40-47`（pom 无 profiles）；`publish-parent.yml:80,92` 发布清单**不含 websocket 模块** |
| 代码规范/覆盖率 | **未发现** | grep `checkstyle\|spotless\|forbidden-apis\|pmd\|sonar\|jacoco` 0 命中；仅根 `lombok.config` |
| 发布自动化 | 有 | parent pom L128-164（gpg + central autoPublish）、根 `deploy.sh` |

---

## 四、八维能力矩阵

| # | 维度 | 判定 | 摘要 |
|---|------|:----:|------|
| 1 | 运行时先进性 | ✅ | SB 4.1.0 + Java 21 + 虚拟线程 + Jackson 3 + MP boot4 starter + 规范 `@AutoConfiguration`/BOM；⚠ 无 AOT/native、jcenter 残留、spring-ai-bom 引而未用 |
| 2 | 可观测性 | ❌ | TraceId 仅 MDC 自造 UUID（`X-Trace-Id`），不对接 W3C/OTel；micrometer 挂着未用；无 actuator（health/metrics/探针全无）；唯一亮点是 500 错误日志 SPI |
| 3 | 安全纵深 | ✅ | JWT/OAuth2 RS 双模式、token 级/用户级撤销、三策略锁定取最严、`@RateLimit`、TOTP 2FA、BCrypt、`@EnableMethodSecurity`、STATELESS；⚠ 缺 OIDC SSO/Passkey/审计/密码过期；**安全响应头显式 disable** |
| 4 | 开放标准契约 | ⚠ | 统一 `Result` + i18n + jakarta 校验齐全；无 springdoc/OpenAPI；无 RFC 9457 兼容模式 |
| 5 | 流量与弹性 | ⚠ | 应用内限流完整（IP/USER/KEY + SpEL + StateStore 三级装配）；无熔断、无退避重试、无限流-网关协同引导 |
| 6 | 数据与中间件 | ⚠ | ✅ Redis 类型安全 key + Lua 原子计数；MQ 骨架好（消费者组+ACK+重试+死信+幂等拦截器）；MyBatis 分页/逻辑删除/UTC/UUIDv7/AES<br>❌ MQ 无 XPENDING/XAUTOCLAIM、无退避、无发件箱；Redis 锁无 watchdog（Redisson 在 BOM 未接线）、无 `@Cacheable`；无分布式 ID/HTTP 幂等/脱敏/租户/乐观锁/操作人审计 |
| 7 | 云原生适配 | ⚠ | ✅ 无状态 + StateStore 外置、12-factor、虚拟线程；❌ 无健康探针/优雅停机、无 native、TraceId 不跨服务/跨 MQ 传播 |
| 8 | 开发者体验 | ⚠ | ✅ 7 模块文档全覆盖、BOM、902 单测；❌ 无 demo、无上下文测试、**无 PR/push CI**、无规范/覆盖率门禁；文档漂移（BeanUtils、JsonLongSetTypeHandler、readme/发布清单漏 websocket） |

---

## 五、核心优势（值得保留/宣传）

1. **技术栈代差优势**：SB 4.1 + Java 21 + 虚拟线程 + Jackson 3，全套对齐 2026 标准
2. **安全链路是全项目最佳设计**：统一认证管线（机械校验 → 类型隔离 → 撤销 → 加载）+ 分层错误（401/400/423/500）+ StateStore 三级装配，测试固化（DefaultJwtAuthenticatorTest 19 + JJwtTokenProviderTest 13）
3. **StateStore SPI 抽象选得好**：限流计数、锁定状态、失败计数共用「内存/Redis/自定义」一套切换机制
4. **MQ 幂等拦截器**（Lua 原子 setnx + 失败删标允许重试）在自研 MQ 中是亮点
5. **装配规范**：`@ConditionalOnMissingBean` 贯穿、BOM 统一版本、条件装配互不打架

---

## 六、缺口分级

### P0 —— 生产系统「会出事」级

| # | 缺口 | 后果 | 修复成本 |
|---|------|------|---------|
| 1 | **可观测性三件套**：actuator + Micrometer 实际接线 + OTel/W3C trace（`X-Trace-Id` 桥接 `traceparent`） | K8s 无健康探针、无指标、故障无法跨服务定位——现代运维绝对底线 | 中（依赖已半就位） |
| 2 | **MQ PEL 认领 + 退避重试** | 消费者宕机 → 消息永久滞留 PEL；失败即重投 → 雪崩 | 小（XPENDING/XAUTOCLAIM 成熟模式） |
| 3 | **PR/push CI 门禁** | 实证：`RateLimitAspectTest` 编译错误长期存在无人发现（构建一直是 `-DskipTests compile`）——没有门禁，902 个测试形同虚设 | 极小 |

### P1 —— 能力缺口（业务长大后必补）

| # | 缺口 | 说明 |
|---|------|------|
| 4 | 安全响应头 | 当前显式禁用全部默认头；至少恢复基础头（**若直面公网则升 P0**） |
| 5 | springdoc/OpenAPI | 现代前后端协作标配 |
| 6 | 「三件套」：分布式 ID、HTTP 幂等、脱敏注解 | `REPEATED_REQUESTS`(900) 定义了却全仓无调用——幂等差一层窗户纸 |
| 7 | `@Cacheable` 集成 + Redis 锁接线 Redisson（watchdog） | Redisson 已在 BOM，接线成本低 |
| 8 | OIDC `oauth2-client` SSO + 安全审计日志 | 企业级登录与合规 |
| 9 | MQ 事务性发件箱 | 本地事务与发消息的丢消息窗口 |
| 10 | 文档清理 | jcenter、`BeanUtils`/`JsonLongSetTypeHandler` 幽灵引用、readme 与发布清单漏 websocket |

### P2 —— 时机未到/锦上添花

GraalVM native/AOT；Passkey/WebAuthn、密码过期；租户隔离 + 乐观锁 + 操作人审计；demo 工程 + `ApplicationContextRunner` 上下文测试；覆盖率/代码规范门禁；RFC 9457 兼容模式；WebSocket 心跳（`maxSessionTimeout` 从死配置变活）；STOMP 支持。

---

## 七、演进路线建议

1. **第一周（P0 收尾，成本极低）**：加 PR CI workflow → MQ 认领 + 退避 → 恢复基础安全头
2. **一个迭代（P0 大头）**：Micrometer 计数（QPS/延迟/限流命中/锁定命中）+ actuator 健康探针 + trace 对接 W3C —— 做完这三件，框架从「开发时现代」变成「运维时现代」
3. **按业务需要（P1）**：OpenAPI → 三件套 → Redisson/`@Cacheable` → SSO/审计
4. **顺手项**：文档漂移清理与 P0 CI 一起做（CI 可加文档-代码一致性检查）

**一句话总评**：核心链路（安全/流量/数据）的**设计质量**是同类自研框架上游水平；短板不在「做得不好的地方」，而在「压根没做的地方」——可观测性、标准契约、工程门禁。补齐三个 P0 后，可负责任地称为匹配现代应用需求的基础组件。

---

## 附录：可观测性建设方向（2026-09-25 讨论结论）

**结论：新建 `migoo-spring-boot-starter-observability` 承载「能力」，现有组件只保留极薄的「信号发出点」——能力收敛到新组件，信号留在原组件。**

- 依赖方向决定：actuator/micrometer/OTel/导出链不应污染各 starter；现有反例是 common pom 挂着未使用的 `micrometer-core`
- 横切属性：三支柱贯穿 7 个模块，行业标准做法即独立基础设施层（Spring Boot 自身亦然）
- 统一决策单点管理：OTLP endpoint、采样率、批量导出、`migoo.observability.*` 开关
- 现有组件保留：`TraceIdFilter` 本体（web）、信号发出（用 Spring `ApplicationEvent`，零 micrometer 依赖）、业务日志与 `ApiErrorLog`；HTTP/JDBC/Redis 等标准埋点由 actuator 自动完成（零代码）
- 落地顺序：① 新模块骨架（父 pom + BOM + 发布清单，勿漏 websocket 教训）→ ② actuator/Micrometer 导出 → ③ `traceparent`↔MDC 桥接 + logback pattern → ④ 信号事件订阅计数（限流/锁定/撤销/MQ/500）→ ⑤ HealthIndicator + `docs/observability.md`
