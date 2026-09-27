package xyz.migoo.framework.security.config;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import xyz.migoo.framework.security.core.AuthUserDetails;
import xyz.migoo.framework.security.core.authentication.AuthUserDetailsFetcher;
import xyz.migoo.framework.web.config.WebRuntimeHints;

/**
 * Security 组件 GraalVM native image / AOT 运行时线索
 *
 * <p>认证主体与登录结果常被应用直接放进响应体，native 下需要反射访问其字段与构造器。</p>
 *
 * <p>由 {@link MiGooSecurityAutoConfiguration} 通过 {@code @ImportRuntimeHints} 引入。</p>
 *
 * @author xiaomi
 */
public class SecurityRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // 认证主体（应用常作为 @AuthenticationPrincipal 返回体的一部分）与登录结果
        hints.reflection().registerType(AuthUserDetails.class, WebRuntimeHints.JACKSON_MEMBERS);
        hints.reflection().registerType(AuthUserDetailsFetcher.LoginResult.class, WebRuntimeHints.JACKSON_MEMBERS);
    }
}
