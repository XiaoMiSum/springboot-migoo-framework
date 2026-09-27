# migoo-framework 示例工程

独立于主构建的可运行示例，演示各 starter 的开箱用法。**不参与主 reactor / 发布流程**。

## 目录

| 示例 | 说明 | 端口 |
|------|------|------|
| [example-web](example-web) | 最小可用 Web 应用：统一响应 `Result`、全局异常、**CORS 四档来源限制**（strict/open/pattern/dynamic + `CorsOriginPredicate`） | 8080 |
| [example-security](example-security) | **JWT 安全登录**：登录换 token → 带 token 访问 → 角色校验 → 刷新 token | 8081 |

## 运行方式

示例依赖本地构建的框架构件（或已发布到 Maven Central 的 1.4.0），先在**仓库根**装载一次：

```bash
# 第一步：装载框架到本地仓库（跳过签名）
mvn install -DskipTests -Dgpg.skip=true

# 第二步：运行示例（任选其一）
mvn -f examples/pom.xml -pl example-web spring-boot:run
mvn -f examples/pom.xml -pl example-security spring-boot:run

# 第三步：端到端测试（CI 同样执行）
mvn -f examples/pom.xml verify
```

## 快速体验

### example-web（统一响应 + CORS）

```bash
# 统一响应
curl 'http://localhost:8080/api/hello?name=MiGoo'
# → {"code":200,"msg":"common.success","data":"hello, MiGoo"}

# 全局异常（业务码 1001000000，HTTP 仍为 200）
curl 'http://localhost:8080/api/boom'
# → {"code":1001000000,"msg":"demo.resource.not_found"}

# CORS：白名单来源放行 / 未知来源 403
curl -i -H 'Origin: http://localhost:3000' http://localhost:8080/api/hello
curl -i -H 'Origin: https://evil.example.org' http://localhost:8080/api/hello
```

CORS 四档模式在 `example-web/src/main/resources/application.yml` 中切换
（`strict` 默认安全白名单 / `open` 开放平台 / `pattern` 通配 / `dynamic` 谓词），
dynamic 谓词示例见 `DynamicCorsPredicateConfig`。**CORS 只做来源限制，不做鉴权。**

### example-security（JWT 登录链路）

```bash
# 1. 登录（内置账号 admin/demo123 与 user/demo123）
curl -s -X POST http://localhost:8081/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"demo123"}'
# → data.accessToken / data.refreshToken

# 2. 带 token 访问受保护接口
curl -s http://localhost:8081/api/me -H "Authorization: Bearer $ACCESS_TOKEN"

# 3. 角色校验（用 user 登录访问 → {"code":403}）
curl -s http://localhost:8081/api/admin -H "Authorization: Bearer $ACCESS_TOKEN"

# 4. 刷新 token
curl -s -X POST http://localhost:8081/auth/refresh -H "X-Refresh-Token: $REFRESH_TOKEN"

# 5. 免登录接口
curl -s http://localhost:8081/public/ping
```

> 本框架约定：响应始终 **HTTP 200**，业务状态由响应体 `Result.code` 表达
> （200 成功 / 401 认证失败 / 403 无权限 / 423 账号锁定 / 1001xxxxx 业务码）。
> `secret-key` 示例值仅用于本地演示，生产环境用 `openssl rand -base64 48` 生成并经环境变量注入。

## 约定说明

- 示例工程使用 `@SpringBootTest` 做端到端验证（验证自动配置装配与完整链路）；
  框架各 starter 模块自身仍遵守仓库「纯单元测试、无 @SpringBootTest」约定。
- 示例版本 `1.0.0-SNAPSHOT` 与框架版本无关；框架版本统一在 `examples/pom.xml`
  的 `migoo.framework.version` 属性中维护。
