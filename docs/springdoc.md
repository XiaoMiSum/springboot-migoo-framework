---
layout: default
---

# migoo-spring-boot-starter-springdoc

OpenAPI 3 接口文档组件，基于 springdoc-openapi——自动生成 `/v3/api-docs` 并内置 Swagger UI，
框架侧叠加默认定制（文档元信息、Bearer JWT 安全方案），前后端协作开箱即用。

## 快速开始

| 步骤 | 说明 |
|------|------|
| 1. 引入依赖 | 添加 `migoo-spring-boot-starter-springdoc` |
| 2. 访问文档 | Swagger UI: `/swagger-ui.html`，OpenAPI JSON: `/v3/api-docs` |
| 3. （可选）丰富描述 | `@Tag` / `@Operation` 标注控制器与方法；免认证端点用 `@SecurityRequirements` 清空安全要求 |

## 依赖

```xml
<dependency>
    <groupId>xyz.migoo.springboot</groupId>
    <artifactId>migoo-spring-boot-starter-springdoc</artifactId>
</dependency>
```

## 应用层对接

### 1. 默认定制（引入即自动生效）

```yaml
migoo:
  springdoc:
    title: ""                          # 文档标题，留空取 spring.application.name，再缺省 "MiGoo API"
    description: "MiGoo Framework API" # 文档描述
    version: "1.0.0"                   # 文档版本号
    security-scheme: true              # Bearer JWT 安全方案
```

- **文档元信息**：标题按「显式配置 → `spring.application.name` → `MiGoo API`」三级回退；
- **Bearer JWT 安全方案**：注册 `bearer-jwt`（HTTP Bearer / JWT）并加**全局安全要求**——
  Swagger UI 顶部出现 **Authorize** 按钮，一次填入 access_token 即可调试全部接口，
  与 security 组件的资源服务器认证（`Authorization: Bearer <token>`）直接对上。

### 2. 标注接口描述

```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

@Tag(name = "订单管理", description = "订单创建/查询/取消")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Operation(summary = "创建订单", description = "相同请求体的重复提交会被幂等拦截（业务码 900）")
    @PostMapping
    public Result<Long> create(@RequestBody @Valid OrderCreateReqBody req) { ... }

    // 免认证端点：清空全局 Bearer 安全要求
    @SecurityRequirements
    @GetMapping("/health-page")
    public Result<String> health() { ... }
}
```

- 返回值为统一响应 `Result<T>` / `PageResult<T>` 时，Swagger 中可见完整包装结构；
- 请求/响应模型用 `@Schema` 描述字段（jakarta 校验注解 `@NotNull` 等会自动带入文档）。

### 3. 覆盖与关闭

| 目标 | 方式 |
|------|------|
| 整体覆盖默认定制 | 应用自定义 `OpenApiCustomizer` Bean（`@ConditionalOnMissingBean`，存在即不注册本组件默认定制） |
| 只关安全方案 | `migoo.springdoc.security-scheme: false` |
| 关本组件定制（保留 springdoc 原生行为） | `migoo.springdoc.enabled: false` |
| 生产环境隐藏文档 | springdoc 原生开关 `springdoc.api-docs.enabled: false`、`springdoc.swagger-ui.enabled: false` |

- 扫描范围、分组、路径等高级配置直接用 springdoc 原生属性（如 `springdoc.packages-to-scan`、
  `springdoc.group-configs`），本组件不重复造轮子。

---

## 自动注册的组件

| 组件 | 说明 | 条件 |
|------|------|------|
| `MiGooSpringdocAutoConfiguration` | OpenAPI 定制（元信息 + Bearer JWT 安全方案） | `migoo.springdoc.enabled=true`（默认开启） |

## 配置项

```yaml
migoo:
  springdoc:                             # 本组件定制
    enabled: true                        # 总开关（关闭仅保留 springdoc 原生行为）
    title: ""                            # 文档标题（留空取 spring.application.name，再缺省 MiGoo API）
    description: "MiGoo Framework API"   # 文档描述
    version: "1.0.0"                     # 文档版本号
    security-scheme: true                # Bearer JWT 安全方案（Swagger UI Authorize 按钮）

springdoc:                               # springdoc 原生配置（透传，按需使用）
  api-docs:
    enabled: true                        # /v3/api-docs 端点
  swagger-ui:
    path: /swagger-ui.html               # UI 路径
  packages-to-scan: []                   # 仅扫描指定包
```

---

## 说明

- **版本口径**：springdoc-openapi 3.x（适配 Spring Boot 4 / Spring Framework 7），版本由
  `migoo-framework-dependencies` BOM 管理（`springdoc.version`）；
- **模块定位**：文档能力是可选件——不引入本组件时应用亦可自行引 springdoc-openapi，
  本组件提供的是框架级默认定制与统一版本管理；
- **CI 保障**：新模块纳入「父 pom / BOM / 发布 -pl / Summary」四方清单校验
  （`scripts/check-publish-modules.sh`，详见 [observability.md](observability.md) §10）。
