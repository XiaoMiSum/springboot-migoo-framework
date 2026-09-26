package xyz.migoo.framework.security.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * {@link MiGooWebSecurityFilterChainConfiguration#applyHeaders} 与
 * {@link SecurityProperties.Headers} 单元测试
 *
 * <p>装配策略「要关的才碰」：默认仅补 Referrer-Policy，其余保持 Spring Security
 * 默认头集；逐项开关只触碰对应配置器。</p>
 */
class MiGooWebSecurityFilterChainConfigurationTest {

    // ==================== 装配行为 ====================

    @Test
    void headersDisabledDisablesEntireConfigurer() {
        HeadersConfigurer<HttpSecurity> headers = spy(new HeadersConfigurer<>());
        // disable() 需要 builder（生产路径由 HttpSecurity 注入）；测试裸实例 stub 返回值，仅记录调用
        doReturn(null).when(headers).disable();
        SecurityProperties.Headers properties = new SecurityProperties.Headers();
        properties.setEnabled(false);

        MiGooWebSecurityFilterChainConfiguration.applyHeaders(headers, properties);

        // 总开关关闭 = 旧版 headers disable 行为，不逐项配置
        verify(headers).disable();
        verify(headers, never()).contentTypeOptions(any());
        verify(headers, never()).referrerPolicy(any());
        verify(headers, never()).contentSecurityPolicy(any());
    }

    @Test
    void defaultsOnlyAddReferrerPolicyAndKeepSpringDefaultsUntouched() {
        HeadersConfigurer<HttpSecurity> headers = spy(new HeadersConfigurer<>());

        MiGooWebSecurityFilterChainConfiguration.applyHeaders(headers, new SecurityProperties.Headers());

        // 5 项 Spring Security 默认头一项不碰（由框架保留），仅补充 Referrer-Policy
        verify(headers).referrerPolicy(any());
        verify(headers, never()).contentTypeOptions(any());
        verify(headers, never()).frameOptions(any());
        verify(headers, never()).httpStrictTransportSecurity(any());
        verify(headers, never()).cacheControl(any());
        verify(headers, never()).xssProtection(any());
        verify(headers, never()).contentSecurityPolicy(any());
        verify(headers, never()).disable();
    }

    @Test
    void perHeaderToggleTouchesOnlyThatHeader() {
        HeadersConfigurer<HttpSecurity> headers = spy(new HeadersConfigurer<>());
        SecurityProperties.Headers properties = new SecurityProperties.Headers();
        properties.setContentTypeOptions(false);

        MiGooWebSecurityFilterChainConfiguration.applyHeaders(headers, properties);

        verify(headers).contentTypeOptions(any());
        // 其余开关不触碰
        verify(headers, never()).frameOptions(any());
        verify(headers, never()).httpStrictTransportSecurity(any());
        verify(headers, never()).cacheControl(any());
        verify(headers, never()).xssProtection(any());
    }

    @Test
    void referrerPolicyCanBeTurnedOff() {
        HeadersConfigurer<HttpSecurity> headers = spy(new HeadersConfigurer<>());
        SecurityProperties.Headers properties = new SecurityProperties.Headers();
        properties.setReferrerPolicy(false);

        MiGooWebSecurityFilterChainConfiguration.applyHeaders(headers, properties);

        verify(headers, never()).referrerPolicy(any());
    }

    @Test
    void contentSecurityPolicyConfiguredWhenProvided() {
        HeadersConfigurer<HttpSecurity> headers = spy(new HeadersConfigurer<>());
        SecurityProperties.Headers properties = new SecurityProperties.Headers();
        properties.setContentSecurityPolicy("default-src 'self'");

        MiGooWebSecurityFilterChainConfiguration.applyHeaders(headers, properties);

        verify(headers).contentSecurityPolicy(any());
    }

    @Test
    void blankContentSecurityPolicyIgnored() {
        HeadersConfigurer<HttpSecurity> headers = spy(new HeadersConfigurer<>());
        SecurityProperties.Headers properties = new SecurityProperties.Headers();
        properties.setContentSecurityPolicy("   ");

        MiGooWebSecurityFilterChainConfiguration.applyHeaders(headers, properties);

        verify(headers, never()).contentSecurityPolicy(any());
    }

    // ==================== 属性默认值 ====================

    @Test
    void headersPropertyDefaults() {
        SecurityProperties.Headers properties = new SecurityProperties.Headers();

        // 默认启用：Spring Security 默认 5 头 + 框架补充 Referrer-Policy，CSP 不下发
        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.isContentTypeOptions()).isTrue();
        assertThat(properties.isFrameOptions()).isTrue();
        assertThat(properties.isHsts()).isTrue();
        assertThat(properties.isCacheControl()).isTrue();
        assertThat(properties.isXssProtection()).isTrue();
        assertThat(properties.isReferrerPolicy()).isTrue();
        assertThat(properties.getContentSecurityPolicy()).isNull();

        // 父配置默认挂载
        SecurityProperties securityProperties = new SecurityProperties();
        assertThat(securityProperties.getHeaders()).isNotNull();
    }
}
