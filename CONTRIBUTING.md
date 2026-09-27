# 贡献指南

感谢参与 migoo-framework 的建设。本文档面向框架本身的开发者（示例工程的使用方式见 [examples/README.md](examples/README.md)）。

## 开发环境

| 工具  | 版本要求 |
|-------|----------|
| JDK   | 21+（`JAVA_HOME` 指向 JDK 21） |
| Maven | 3.9+ |
| Git   | 任意现代版本 |

## 构建与测试

```bash
# 全量构建（父 pom 把 gpg 签名绑在 verify 阶段，本机无密钥时跳过签名）
mvn clean verify -Dgpg.skip=true

# 只跑某模块
mvn -pl migoo-framework-parent/migoo-spring-boot-starter-web test

# 本地执行 CI 同款门禁
bash scripts/check-publish-modules.sh   # 模块清单 + 版本号全仓一致
bash scripts/check-coverage.sh          # 聚合行覆盖率 ≥ 75%（可用 MIN_LINE_COVERAGE 覆盖）

# 示例工程（先装载框架构件）
mvn install -DskipTests -Dgpg.skip=true
mvn -f examples/pom.xml verify

# API 二进制兼容性报告（对比 japicmp.old.version 基线，只报告不卡点）
mvn -Pjapicmp -pl migoo-framework-parent/migoo-spring-boot-starter-web verify
```

## 测试约定

- **各 starter 模块**：纯单元测试，JUnit 5 + AssertJ + Mockito；**不引入 `@SpringBootTest`**，
  需要验证自动配置装配时使用 `ApplicationContextRunner`（样例见各 starter 的 `*AutoConfigurationSmokeTest`）。
- **examples/ 示例工程**：例外，可用 `@SpringBootTest` + MockMvc 做端到端链路验证。
- 新功能必须附带测试；修 bug 必须先写能复现的失败用例。

## 编码约定

- 注释、Javadoc、提交信息使用中文；公开类型与方法必须有 Javadoc，作者标注 `@author xiaomi`。
- 自动配置遵循 Spring Boot 规范：`@AutoConfiguration` + `@ConditionalOnMissingBean`（应用可覆盖），
  组件协作通过接口/SPI 解耦，不使用组件扫描。
- 依赖传递性原则：**运行期硬引用的依赖不得标 `<optional>`**（会导致使用方 `NoClassDefFoundError`），
  真正可选的能力用 `@ConditionalOnClass(name = "...")` 等按名条件守护。
- 框架响应约定：HTTP 恒为 200，业务状态由 `Result.code` 表达（200/400/401/403/423/500/业务码）。

## 新增组件 / 模块 Checklist

遗漏任一处会导致「本地能用、Central 拿不到」，完整清单见 [docs/observability.md §10](docs/observability.md)：

- [ ] `migoo-framework-parent/pom.xml` → `<modules>`
- [ ] `migoo-framework-dependencies/pom.xml` → `dependencyManagement`
- [ ] `.github/workflows/publish-parent.yml` → `-pl` 清单与 `Publication Summary` 汇总行
- [ ] `readme.md` → 组件表格、项目结构树
- [ ] `docs/index.md` → 组件表格、按需引入示例 + 新增 `docs/xxx.md` 组件文档
- [ ] 新模块 pom 含 `name/description/url/licenses/scm/developers`（Central 发布必填）
- [ ] `bash scripts/check-publish-modules.sh` 全绿

## 质量门禁（CI 自动执行）

| 门禁 | 说明 |
|------|------|
| 清单/版本一致性 | `scripts/check-publish-modules.sh` |
| 构建 + 测试 | `mvn clean verify -Dgpg.skip=true`（含 JaCoCo 单模块行/分支 ≥50% 卡点） |
| 覆盖率 | `scripts/check-coverage.sh` 聚合行覆盖 ≥75% |
| 示例端到端 | `mvn -f examples/pom.xml verify`（登录/CORS/权限完整链路） |
| 静态分析 | CodeQL（push/PR/每周） |
| 依赖漏洞 | OWASP dependency-check（每月+手动，CVSS ≥7 失败） |
| API 兼容性 | japicmp 对比上一发布版，报告进 step summary |
| 依赖升级 | Dependabot（Maven + Actions 每周） |

PR 合并前以上门禁必须全绿。

## 发布流程（维护者）

1. 全仓版本号升级（根 pom / BOM 属性 / 父 pom / 各 starter / `docs`、`readme` 引用处），
   脚本会校验三方一致：`bash scripts/check-publish-modules.sh`
2. `CHANGELOG.md` 增加新版本条目，**BREAKING 变更必须显式标注**
3. 更新 `migoo-framework-parent/pom.xml` 的 `japicmp.old.version` 为上一发布版
4. `mvn clean verify -Dgpg.skip=true` + 两支门禁脚本全绿
5. 打 tag，先发 BOM（`publish-dependencies.yml`），再发 parent + starters（`publish-parent.yml`）

## 行为变更说明

本仓库 1.4.0 起按 minor 版本发布行为变更（CORS 默认收紧、密码工具 Bean 化、加密格式升级等），
破坏性变更在 `CHANGELOG.md` 中以 **BREAKING** 标注，升级前请阅读对应条目。
