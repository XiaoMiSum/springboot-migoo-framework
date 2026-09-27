package xyz.migoo.framework.web.core.cors;

/**
 * CORS 来源限制模式
 *
 * <p>CORS 的职责是浏览器侧的跨域来源限制：哪些域名的<strong>网页</strong>可以跨域发起请求并读取响应。
 * 它不做鉴权——对 curl/服务间调用无约束，接口安全必须由服务端鉴权独立保证。</p>
 *
 * @author xiaomi
 */
public enum CorsMode {

    /**
     * 严格模式（默认）：仅放行 {@code allowed-origins} / {@code allowed-origin-patterns} 中列出的来源；
     * 两者均为空时不注册 CORS 过滤器（即不做任何跨域放行，等同同源策略默认行为）。
     *
     * <p>适合后台管理系统等来源固定的单体/内部应用。</p>
     */
    STRICT,

    /**
     * 开放模式：放行所有来源（{@code *}），<strong>强制关闭凭证</strong>（规范禁止 {@code *} + credentials）。
     *
     * <p>适合以 Token（Authorization 头）认证的开放平台 API——
     * 此时 CORS 仅允许第三方网页发起调用，安全性由服务端 Token 校验保证。
     * 若配置了 {@code allow-credentials=true} 则启动失败。</p>
     */
    OPEN,

    /**
     * 通配模式：按 {@code allowed-origin-patterns} 的模式匹配来源（如 {@code https://*.example.com}），
     * 适合自有子域名生态；不允许与 {@code *} 通配 + 凭证组合（会退化为全开放）。
     */
    PATTERN,

    /**
     * 动态模式：每个请求的来源交由应用注册的 {@link CorsOriginPredicate} Bean 判定，
     * 适合来源长尾、动态注册的开放平台（Cookie 场景）。
     *
     * <p>未注册 {@link CorsOriginPredicate} Bean 时启动失败。</p>
     */
    DYNAMIC
}
