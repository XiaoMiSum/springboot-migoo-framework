package xyz.migoo.framework.web.core.cors;

/**
 * 动态 CORS 来源准入谓词（{@link CorsMode#DYNAMIC} 模式扩展点）
 *
 * <p>仅回答一个问题：这个 Origin 的浏览器跨域请求是否放行——这是<strong>域名准入</strong>，
 * 与用户身份鉴权无关（鉴权必须在服务端独立完成，CORS 不提供任何鉴权能力）。</p>
 *
 * <p>实现方通常是查询自身的应用注册表/DB/Redis，例如开放平台判断该 JS 来源域名是否已注册。
 * 新增来源域名只需在业务侧注册，无需修改框架配置。</p>
 *
 * <pre>{@code
 * @Bean
 * CorsOriginPredicate openPlatformCors(RegisteredAppRepository apps) {
 *     return origin -> apps.findActiveByJsOrigin(origin).isPresent();
 * }
 * }</pre>
 *
 * @author xiaomi
 */
@FunctionalInterface
public interface CorsOriginPredicate {

    /**
     * 判断来源是否放行
     *
     * @param origin 请求的 Origin 头（如 {@code https://partner.example.com}），非空
     * @return true = 放行（响应反射该具体 Origin）；false = 拒绝（不下发 CORS 放行头）
     */
    boolean isAllowed(String origin);
}
