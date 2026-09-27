package xyz.migoo.framework.security.core.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import xyz.migoo.framework.web.core.handler.GlobalExceptionHandler;
import xyz.migoo.framework.web.i18n.I18NMessage;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link GlobalExceptionHandler} 访问拒绝映射测试
 * <p>
 * web 组件不依赖 spring-security，按类层次全限定名识别 AccessDeniedException；
 * 本测试放在 security 模块，以真实 spring-security 异常类验证：
 * 方法级 {@code @PreAuthorize}/{@code @Secured} 越权 → 403（而非兜底 500）。
 */
class GlobalExceptionHandlerAccessDeniedTest {

    private GlobalExceptionHandler handler;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler("example-security", null, new I18NMessage(new StaticMessageSource()));
        request = new MockHttpServletRequest();
    }

    @Test
    void accessDeniedMapsToForbiddenCode403() {
        var result = handler.defaultExceptionHandler(request, new AccessDeniedException("denied"));
        assertThat(result.getCode()).isEqualTo(403);
    }

    @Test
    void authorizationDeniedSubclassMapsToForbiddenCode403() {
        // 方法级 @PreAuthorize 抛出的是 AuthorizationDeniedException（AccessDeniedException 子类）
        var result = handler.defaultExceptionHandler(request, new AuthorizationDeniedException("denied"));
        assertThat(result.getCode()).isEqualTo(403);
    }

    @Test
    void accessDeniedInCauseChainMapsToForbiddenCode403() {
        var result = handler.defaultExceptionHandler(request,
                new IllegalStateException("wrapper", new AccessDeniedException("denied")));
        assertThat(result.getCode()).isEqualTo(403);
    }

    @Test
    void otherExceptionStillMapsTo500() {
        // 非访问拒绝异常保持兜底 500（不被 403 分支误吞）
        var result = handler.defaultExceptionHandler(request, new IllegalStateException("boom"));
        assertThat(result.getCode()).isEqualTo(500);
    }
}
