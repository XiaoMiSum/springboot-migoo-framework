package xyz.migoo.framework.springdoc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * springdoc 文档组件配置属性
 *
 * @author xiaomi
 */
@Data
@ConfigurationProperties(prefix = "migoo.springdoc")
public class MigooSpringdocProperties {

    /**
     * 是否启用本组件的 OpenAPI 默认定制（元信息 + Bearer JWT 安全方案）。
     * 关闭后 springdoc 本身的文档端点仍然可用，只是不再叠加本组件的默认定制
     */
    private boolean enabled = true;

    /**
     * 文档标题（留空取 spring.application.name，再缺省 MiGoo API）
     */
    private String title = "";

    /**
     * 文档描述
     */
    private String description = "MiGoo Framework API";

    /**
     * 文档版本号
     */
    private String version = "1.0.0";

    /**
     * 是否注册 Bearer JWT 安全方案（Swagger UI 顶部显示 Authorize 按钮，
     * 全局接口默认携带 Authorization 请求头，便于联调）
     */
    private boolean securityScheme = true;
}
