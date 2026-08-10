package xyz.migoo.framework.security.core.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.NativeWebRequest;
import xyz.migoo.framework.security.core.TestAuthUser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SecurityFrameworkUtils} 单元测试
 */
class SecurityFrameworkUtilsTest {

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void obtainAuthorizationExtractsBearerToken() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer abc123");
        assertThat(SecurityFrameworkUtils.obtainAuthorization(request, "Authorization")).isEqualTo("abc123");
    }

    @Test
    void obtainAuthorizationTrimsBearerToken() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("  Bearer  abc123  ");
        assertThat(SecurityFrameworkUtils.obtainAuthorization(request, "Authorization")).isEqualTo("abc123");
    }

    @Test
    void obtainAuthorizationReturnsNullWhenHeaderMissing() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);
        assertThat(SecurityFrameworkUtils.obtainAuthorization(request, "Authorization")).isNull();
    }

    @Test
    void obtainAuthorizationReturnsNullWhenHeaderBlank() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("   ");
        assertThat(SecurityFrameworkUtils.obtainAuthorization(request, "Authorization")).isNull();
    }

    @Test
    void obtainAuthorizationReturnsNullWhenNotBearerScheme() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");
        assertThat(SecurityFrameworkUtils.obtainAuthorization(request, "Authorization")).isNull();
    }

    @Test
    void obtainAuthorizationExtractsBearerAnywhereInHeader() {
        // 实现使用 indexOf("Bearer ") 截取，Bearer 不在头部也可提取
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("abc Bearer token123");
        assertThat(SecurityFrameworkUtils.obtainAuthorization(request, "Authorization")).isEqualTo("token123");
    }

    @Test
    void obtainAuthorizationFromNativeWebRequest() {
        NativeWebRequest request = mock(NativeWebRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer xyz789");
        assertThat(SecurityFrameworkUtils.obtainAuthorization(request, "Authorization")).isEqualTo("xyz789");
    }

    @Test
    void getLoginUserReturnsUserFromContext() {
        TestAuthUser user = TestAuthUser.of(1L, "admin");
        setAuthentication(user);
        assertThat(SecurityFrameworkUtils.getLoginUser()).isSameAs(user);
    }

    @Test
    void getLoginUserReturnsNullWhenNoAuthentication() {
        assertThat(SecurityFrameworkUtils.getLoginUser()).isNull();
    }

    @Test
    void getLoginUserReturnsNullWhenPrincipalNotAuthUserDetails() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("anonymous", null));
        assertThat(SecurityFrameworkUtils.getLoginUser()).isNull();
    }

    @Test
    void getLoginUserIdReturnsStringId() {
        setAuthentication(TestAuthUser.of(42L, "admin"));
        assertThat(SecurityFrameworkUtils.getLoginUserId()).isEqualTo("42");
    }

    @Test
    void getLoginUserIdReturnsNullWhenNotLoggedIn() {
        assertThat(SecurityFrameworkUtils.getLoginUserId()).isNull();
    }

    @Test
    void setLoginUserWritesContextAndRequestAttributes() {
        TestAuthUser user = TestAuthUser.of(7L, "admin");
        user.setName("管理员");
        HttpServletRequest request = mock(HttpServletRequest.class);

        SecurityFrameworkUtils.setLoginUser(user, request);

        // 认证信息写入 SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isSameAs(user);
        // 用户编号与姓名写入 request attribute，供 ApiAccessLogFilter 等使用
        verify(request).setAttribute("login_user_id", 7L);
        verify(request).setAttribute("login_user_name", "管理员");
    }

    private static void setAuthentication(TestAuthUser user) {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }
}
