# 更新日志（CHANGELOG）

本项目遵循 [语义化版本](https://semver.org/lang/zh-CN/)。破坏性变更以 **BREAKING** 标注。

## 1.4.0 (2026-09-27)

面向现代应用的补全版本：安全修复（P0）→ 工程规范化（P1）→ 现代化能力（P2）。

### ⚠️ BREAKING

- **CORS 默认行为收紧**：`migoo.web.cors` 默认 `mode=strict` 且允许来源为空 = **不放行任何跨域来源**。
  原先隐式放行的行为不再存在；跨域必须显式选择四档模式之一：
  - `strict`（默认）：仅放行 `allowed-origins` / `allowed-origin-patterns` 中列出的来源，空列表 = 禁止跨域
  - `open`：放行所有来源，强制关闭凭证（Token 型开放平台 API）
  - `pattern`：按 `allowed-origin-patterns` 模式匹配（如 `https://*.example.com`，自有子域名生态）
  - `dynamic`：应用注册一个 `CorsOriginPredicate` Bean 逐请求判定（动态域名开放平台）
  - `*` 来源与 `allow-credentials=true` 组合、`dynamic` 缺少谓词 Bean、`pattern` 未配置模式 → **启动即失败（fail-fast）**
- **CORS 仅做来源限制，不做鉴权**：`CorsOriginPredicate` 命名与语义均为「来源准入」，身份认证仍由 security 组件负责
- **`PasswordUtils` 不再是 `@Component`**：框架不启用组件扫描，改为由 `MiGooSecurityAutoConfiguration`
  注册为 Bean 并注入 `PasswordEncoder`；应用自定义 `PasswordEncoder` Bean 时自动复用（`@ConditionalOnMissingBean`）
- **`EncryptTypeHandler` 密文格式升级**：新写入密文为 `v1:` 前缀的 AES-256-GCM（PBKDF2-SHA256 12 万轮派生）；
  无前缀历史密文仍走遗留 ECB+MD5 兼容解密，存量数据无需迁移。
  密钥来源优先级：系统属性 `migoo.encryptor.password` → 环境变量 `MIGOO_ENCRYPTOR_PASSWORD` → 遗留带点环境变量
- **`SecurityProperties.logoutUrl` 默认值** 由空改为 `/logout`；JWT `secret-key` 新增 ≥32 字节熵校验（不满足则启动失败）

### 🛡️ 安全修复（P0）

- `EncryptTypeHandler` 由「AES-ECB + MD5 口令派生、密钥可被弱口令爆破」重写为 AES-256-GCM 认证加密，消除选择密文篡改风险
- CORS 开放能力从「隐式、可被通配符+凭证绕过」改为四档显式模式 + 启动期组合校验
- 移除 BOM 中无法解析/长期未用的死依赖条目（spring-ai-bom、fastjson2、redisson、dynamic-datasource、jdom2、jsoup、ip2region、caffeine、wechatpay、alipay 等），降低供应链攻击面
- JWT 密钥熵校验、登出地址默认值修复，避免弱密钥与空登出配置上线

### 🛠️ 工程规范化（P1）

- 全部自动配置统一改用 `@AutoConfiguration`（含 `before`/`after` 排序元数据），删除「带 `@AutoConfiguration` 导入文件却是 `@Configuration` 类」的不一致
- 框架 Bean 全面补齐 `@ConditionalOnMissingBean`（`redisTemplate`、MyBatis-Plus 插件/填充器、`PasswordEncoder`、限流/幂等等），**应用自定义 Bean 优先**；`PasswordUtils`、`ObjectProvider` 可选注入修正「应用不接扩展点就启动失败」的缺陷
- `JacksonAutoConfiguration` 由空壳改为真实注册（`JsonMapperBuilderCustomizer` + 自定义模块），`migoo.web.jackson.enabled` 默认关闭以保证响应格式不被静默改变
- 新增 ApplicationContextRunner 冒烟测试（web / security / redis / mybatis）：默认可启动、非法配置 fail-fast、用户 Bean 可覆盖
- CI 门禁：JaCoCo 覆盖率、CodeQL 静态分析、OWASP dependency-check 依赖漏洞扫描、Dependabot、japicmp API 兼容性报告；`ci.yml` 增加示例工程端到端任务（`mvn -f examples/pom.xml verify`）
- `scripts/check-publish-modules.sh` 新增全仓版本号 / BOM 属性 / CHANGELOG 三方一致性校验（`examples/` 独立版本，不参与发布校验）
- MyBatis-Plus 升级 3.5.17、mybatis-plus-join 升级 1.5.9（适配 3.5.17 版本探测）、mybatis-plus-join 依赖修正
- **依赖传递性修复**：运行期硬引用的依赖不再标 `<optional>`（原会导致仅引入单个 starter 的应用 `NoClassDefFoundError` / 启动失败）——
  security→web、redis→common、mybatis→common、mq→redis、websocket→security 改为传递引入；
  移除 mybatis 对 web 的 `provided` 死依赖（全源码 0 引用）
- **方法级越权返回 403 而非 500**：`GlobalExceptionHandler` 按类层次名识别 spring-security 的
  `AccessDeniedException`/`AuthorizationDeniedException`（web 组件保持零 security 依赖），
  对齐文档承诺的「权限不足 → 403」，且不落 API 错误日志、不发布 SERVER_ERROR 事件

### ✨ 现代化能力（P2）

- GraalVM native image 支持：`WebRuntimeHints` / `SecurityRuntimeHints` / `MybatisRuntimeHints`
  反射与资源线索，经各自动配置 `@ImportRuntimeHints` 接线（统一响应体 Jackson 绑定、i18n 资源束、TypeHandler 反射实例化）
- 新增 `examples/` 示例工程（最小可用 Web 应用、CORS 四档模式演示、安全登录示例），含 `@SpringBootTest` 端到端冒烟
- `readme.md` 修复：项目结构树按真实仓库布局重写、断行代码修正、Maven Central badge 坐标修正、新增质量门禁表

### 🐛 缺陷修复

- `RateLimitAspect` 修复 Spring 7 下复合切点（`@annotation || @within`）的注解参数绑定为空导致 NPE、
  被限流接口全部 500：通知不再以形参绑定注解，改由方法体内按「方法注解 → 类注解」解析，
  切点表达式与语义不变；新增 `RateLimitAspectAopTest` 走真实 Spring AOP 代理回归（方法级 + 类级）
- `AuditLogAspect` 修复同类问题：方法级 `@AuditLog` 在复合切点下绑定为空导致 NPE、被审计接口全部 500，
  通知改为方法体内按「方法注解 → 类注解」解析，切点表达式与语义不变；
  新增 `AuditLogAspectAopTest` 走真实 Spring AOP 代理回归（方法级 + 类级）
- `IdempotentAspect` 修复同类问题：方法级 `@Idempotent` 在复合切点下绑定为空导致 NPE、被防重保护的接口全部 500，
  通知改为方法体内按「方法注解 → 类注解」解析，切点表达式与语义不变；
  新增 `IdempotentAspectAopTest` 走真实 Spring AOP 代理回归（方法级 + 类级）

### 📄 文档

- `docs/web.md`、`docs/security.md`、`docs/mybatis.md`、`docs/index.md`、`docs/websocket.md` 与本次行为变更同步
- 新增 `CONTRIBUTING.md`（开发/测试约定、新增组件 Checklist、发布流程）
