package xyz.migoo.framework.security.config;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.context.annotation.ImportRuntimeHints;
import xyz.migoo.framework.security.core.AuthUserDetails;
import xyz.migoo.framework.security.core.authentication.AuthUserDetailsFetcher;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SecurityRuntimeHints} 单元测试：验证认证主体/登录结果的反射线索与自动配置接线
 */
class SecurityRuntimeHintsTest {

    private final RuntimeHints hints = new RuntimeHints();

    private RuntimeHints registered() {
        new SecurityRuntimeHints().registerHints(hints, getClass().getClassLoader());
        return hints;
    }

    @Test
    void authTypesRegisteredForJacksonAccess() {
        registered();
        assertThat(hints.reflection().getTypeHint(AuthUserDetails.class)).isNotNull();
        assertThat(hints.reflection().getTypeHint(AuthUserDetails.class).getMemberCategories())
                .contains(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                        MemberCategory.INVOKE_PUBLIC_METHODS,
                        MemberCategory.ACCESS_DECLARED_FIELDS);
        // 嵌套登录结果类型同样登记
        assertThat(hints.reflection().getTypeHint(AuthUserDetailsFetcher.LoginResult.class)).isNotNull();
    }

    @Test
    void autoConfigurationImportsRuntimeHints() {
        ImportRuntimeHints annotation = MiGooSecurityAutoConfiguration.class.getAnnotation(ImportRuntimeHints.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).contains(SecurityRuntimeHints.class);
    }
}
