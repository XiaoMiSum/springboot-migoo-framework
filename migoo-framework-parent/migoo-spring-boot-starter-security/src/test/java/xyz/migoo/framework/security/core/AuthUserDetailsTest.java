package xyz.migoo.framework.security.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link AuthUserDetails} 单元测试
 */
class AuthUserDetailsTest {

    @Test
    void chainSettersReturnSameInstance() {
        TestAuthUser user = new TestAuthUser();
        // 链式 setter 必须返回 this，方便流式构造
        assertThat(user.setId(1L)).isSameAs(user);
        assertThat(user.setName("admin")).isSameAs(user);
        assertThat(user.setUsername("admin")).isSameAs(user);
        assertThat(user.setPassword("secret")).isSameAs(user);
        assertThat(user.setEnabled(true)).isSameAs(user);
        assertThat(user.setAuthorities(List.of())).isSameAs(user);
        assertThat(user.setTotpSecret("SECRET")).isSameAs(user);
        assertThat(user.setTwoFactorEnabled(true)).isSameAs(user);
        assertThat(user.setTwoFactorBound(true)).isSameAs(user);
        assertThat(user.setAttrs(Map.of("k", "v"))).isSameAs(user);
    }

    @Test
    void getUsernameReturnsConfiguredValue() {
        TestAuthUser user = TestAuthUser.of(1L, "alice");
        assertThat(user.getUsername()).isEqualTo("alice");
    }

    @Test
    void getPasswordReturnsConfiguredValueAndIsJsonIgnored() throws Exception {
        TestAuthUser user = TestAuthUser.of(1L, "alice");
        user.setPassword("plain-password");
        assertThat(user.getPassword()).isEqualTo("plain-password");
        // getPassword 声明了 @JsonIgnore，序列化时不会暴露密码
        assertThat(AuthUserDetails.class.getMethod("getPassword")
                .getAnnotation(JsonIgnore.class)).isNotNull();
    }

    @Test
    void getAuthoritiesReturnsEmptyListWhenNull() {
        TestAuthUser user = new TestAuthUser();
        assertThat(user.getAuthorities()).isEmpty();
    }

    @Test
    void getAuthoritiesReturnsConfiguredAuthorities() {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        TestAuthUser user = new TestAuthUser();
        user.setAuthorities(authorities);
        assertThat(user.getAuthorities()).isSameAs(authorities);
        assertThat(user.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void isEnabledReflectsEnabledFlag() {
        assertThat(new TestAuthUser().setEnabled(false).isEnabled()).isFalse();
        assertThat(new TestAuthUser().setEnabled(true).isEnabled()).isTrue();
    }

    @Test
    void idAndNameRoundtrip() {
        TestAuthUser user = TestAuthUser.of(42L, "bob");
        user.setName("Bob");
        assertThat(user.getId()).isEqualTo(42L);
        assertThat(user.getName()).isEqualTo("Bob");
    }

    @Test
    void totpFieldsRoundtrip() {
        TestAuthUser user = new TestAuthUser();
        user.setTotpSecret("BASE32SECRET");
        user.setTwoFactorEnabled(true);
        user.setTwoFactorBound(true);
        assertThat(user.getTotpSecret()).isEqualTo("BASE32SECRET");
        assertThat(user.isTwoFactorEnabled()).isTrue();
        assertThat(user.isTwoFactorBound()).isTrue();
    }

    @Test
    void authoritiesNullSafeWhenBuildingAuthentication() {
        // getAuthorities 返回空集合，可用于 UsernamePasswordAuthenticationToken 构造
        TestAuthUser user = new TestAuthUser();
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertThat(authorities).isNotNull().isEmpty();
    }
}
