package xyz.migoo.framework.web.core.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import xyz.migoo.framework.common.pojo.Result;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link WebFrameworkUtils} 单元测试
 *
 * <p>通过 Mockito mock {@link HttpServletRequest} 验证 login_user_id / login_user_name / result 三个
 * Request Attribute 的写入与读取。</p>
 */
class WebFrameworkUtilsTest {

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    /** 构造一个基于 HashMap 存取 attribute 的 request mock，模拟真实容器行为 */
    private static HttpServletRequest attributeRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Map<String, Object> attributes = new HashMap<>();
        doAnswer(invocation -> {
            attributes.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(request).setAttribute(anyString(), org.mockito.ArgumentMatchers.any());
        when(request.getAttribute(anyString())).thenAnswer(invocation -> attributes.get(invocation.getArgument(0)));
        return request;
    }

    @Test
    void setAndGetLoginUserIdRoundTrip() {
        HttpServletRequest request = attributeRequest();
        WebFrameworkUtils.setLoginUserId(request, 1001L);
        assertThat(WebFrameworkUtils.getLoginUserId(request)).isEqualTo(1001L);
    }

    @Test
    void setAndGetLoginUserNameRoundTrip() {
        HttpServletRequest request = attributeRequest();
        WebFrameworkUtils.setLoginUserName(request, "migoo");
        assertThat(WebFrameworkUtils.getLoginUserName(request)).isEqualTo("migoo");
    }

    @Test
    void getLoginUserIdReturnsNullWhenRequestIsNull() {
        assertThat(WebFrameworkUtils.getLoginUserId(null)).isNull();
    }

    @Test
    void getLoginUserNameReturnsNullWhenRequestIsNull() {
        assertThat(WebFrameworkUtils.getLoginUserName(null)).isNull();
    }

    @Test
    void getLoginUserIdReturnsNullWhenNotSet() {
        HttpServletRequest request = attributeRequest();
        assertThat(WebFrameworkUtils.getLoginUserId(request)).isNull();
    }

    @Test
    void setAndGetResultRoundTrip() {
        HttpServletRequest request = attributeRequest();
        Result<String> result = Result.ok("data");
        WebFrameworkUtils.setResult(request, result);
        assertThat(WebFrameworkUtils.getCommonResult(request)).isSameAs(result);
    }

    @Test
    void noArgGettersReadFromRequestContextHolder() {
        HttpServletRequest request = attributeRequest();
        WebFrameworkUtils.setLoginUserId(request, 42L);
        WebFrameworkUtils.setLoginUserName(request, "context-user");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertThat(WebFrameworkUtils.getLoginUserId()).isEqualTo(42L);
        assertThat(WebFrameworkUtils.getLoginUserName()).isEqualTo("context-user");
    }

    @Test
    void noArgGettersReturnNullWithoutRequestContext() {
        // 未设置 RequestContextHolder 时返回 null
        assertThat(WebFrameworkUtils.getLoginUserId()).isNull();
        assertThat(WebFrameworkUtils.getLoginUserName()).isNull();
    }
}
