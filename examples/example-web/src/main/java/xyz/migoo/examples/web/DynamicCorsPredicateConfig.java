package xyz.migoo.examples.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.migoo.framework.web.core.cors.CorsOriginPredicate;

/**
 * CORS DYNAMIC 模式演示：应用注册 {@link CorsOriginPredicate} Bean，
 * 框架在每次预检/请求时回调该谓词逐条判定来源，一次接入即可覆盖开放平台
 * 「来源不可枚举」的场景（不同域名无需逐个配置）。
 *
 * <p>当前示例默认 {@code migoo.web.cors.mode=strict}，本 Bean 不参与判定；
 * 将 application.yml 中的 mode 改为 {@code dynamic} 后生效。
 * 谓词只做来源限制，不做任何鉴权语义。</p>
 *
 * @author xiaomi
 */
@Configuration
public class DynamicCorsPredicateConfig {

    /**
     * 放行规则：本机开发地址 + 任意 example.com 子域（按业务自行改写）
     */
    @Bean
    public CorsOriginPredicate demoCorsOriginPredicate() {
        return origin -> "http://localhost:3000".equals(origin)
                || origin.endsWith("://example.com")
                || origin.endsWith(".example.com");
    }
}
