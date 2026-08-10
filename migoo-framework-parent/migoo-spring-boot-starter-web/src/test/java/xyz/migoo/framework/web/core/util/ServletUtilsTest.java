package xyz.migoo.framework.web.core.util;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link ServletUtils} 单元测试
 *
 * <p>全部通过 Mockito mock {@link HttpServletRequest}/{@link HttpServletResponse} 完成纯单元测试。</p>
 */
class ServletUtilsTest {

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    // ========== writeJSON ==========

    @Test
    void writeJSONWritesJsonWithJsonContentType() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));

        ServletUtils.writeJSON(response, Map.of("name", "migoo"));

        assertThat(sw.toString()).isEqualTo("{\"name\":\"migoo\"}");
        verify(response).setContentType("application/json;charset=utf-8");
    }

    // ========== writeAttachment ==========

    @Test
    void writeAttachmentSetsOctetStreamAndEncodesChineseFilename() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(outputStream(baos));

        byte[] content = "hello".getBytes(StandardCharsets.UTF_8);
        ServletUtils.writeAttachment(response, "中文文件.txt", content);

        String expected = "attachment;filename=" + URLEncoder.encode("中文文件.txt", StandardCharsets.UTF_8);
        verify(response).setHeader("Content-Disposition", expected);
        verify(response).setContentType("application/octet-stream");
        assertThat(baos.toByteArray()).isEqualTo(content);
    }

    // ========== getUserAgent ==========

    @Test
    void getUserAgentReturnsHeaderValue() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("User-Agent")).thenReturn("curl/8.0");
        assertThat(ServletUtils.getUserAgent(request)).isEqualTo("curl/8.0");
    }

    @Test
    void getUserAgentReturnsEmptyWhenHeaderMissing() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("User-Agent")).thenReturn(null);
        assertThat(ServletUtils.getUserAgent(request)).isEmpty();
    }

    // ========== getLocale ==========

    @Test
    void getLocaleReturnsHeaderValue() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Locale")).thenReturn("en-US");
        assertThat(ServletUtils.getLocale(request)).isEqualTo("en-US");
    }

    @Test
    void getLocaleDefaultsToZhCnWhenHeaderMissing() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Locale")).thenReturn(null);
        assertThat(ServletUtils.getLocale(request)).isEqualTo("zh-CN");
    }

    @Test
    void getLocaleDefaultsToZhCnWhenHeaderEmpty() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Locale")).thenReturn("");
        assertThat(ServletUtils.getLocale(request)).isEqualTo("zh-CN");
    }

    // ========== getClientIP ==========

    @Test
    void getClientIPReadsXForwardedForHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1");
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        assertThat(ServletUtils.getClientIP(request)).isEqualTo("10.0.0.1");
    }

    @Test
    void getClientIPPreferenceIsForwardedThenRealIp() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn("10.0.0.2");
        assertThat(ServletUtils.getClientIP(request)).isEqualTo("10.0.0.2");
    }

    @Test
    void getClientIPSkipsUnknownHeadersAndFallsBackToRemoteAddr() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("unknown");
        when(request.getHeader("X-Real-IP")).thenReturn("");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        assertThat(ServletUtils.getClientIP(request)).isEqualTo("127.0.0.1");
    }

    @Test
    void getClientIPWithCustomHeaderNames() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Custom-IP")).thenReturn("10.1.1.1");
        assertThat(ServletUtils.getClientIP(request, "X-Custom-IP")).isEqualTo("10.1.1.1");
    }

    // ========== isJsonRequest ==========

    @Test
    void isJsonRequestAcceptsJsonContentType() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContentType()).thenReturn("application/json;charset=UTF-8");
        assertThat(ServletUtils.isJsonRequest(request)).isTrue();
    }

    @Test
    void isJsonRequestIsCaseInsensitive() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContentType()).thenReturn("APPLICATION/JSON");
        assertThat(ServletUtils.isJsonRequest(request)).isTrue();
    }

    @Test
    void isJsonRequestRejectsNonJsonOrNull() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContentType()).thenReturn("text/html");
        assertThat(ServletUtils.isJsonRequest(request)).isFalse();

        when(request.getContentType()).thenReturn(null);
        assertThat(ServletUtils.isJsonRequest(request)).isFalse();
    }

    // ========== getParams / getParamMap ==========

    @Test
    void getParamMapJoinsMultiValuesWithComma() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Map<String, String[]> params = new HashMap<>();
        params.put("ids", new String[]{"1", "2", "3"});
        when(request.getParameterMap()).thenReturn(params);

        assertThat(ServletUtils.getParamMap(request)).containsEntry("ids", "1,2,3");
    }

    @Test
    void getParamsReturnsUnmodifiableMap() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameterMap()).thenReturn(new HashMap<>());
        assertThatThrownBy(() -> ServletUtils.getParams(request).put("x", new String[]{"y"}))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    // ========== getBody / getBodyBytes ==========

    @Test
    void getBodyReadsContentFromReader() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("{\"a\":1}")));
        assertThat(ServletUtils.getBody(request)).isEqualTo("{\"a\":1}");
    }

    @Test
    void getBodyBytesReadsAllBytesFromInputStream() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getInputStream()).thenReturn(inputStream("hello body"));
        assertThat(new String(ServletUtils.getBodyBytes(request), StandardCharsets.UTF_8)).isEqualTo("hello body");
    }

    // ========== getHeaders ==========

    @Test
    void getHeadersCollectsAllHeaders() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Enumeration<String> names = Collections.enumeration(List.of("Host", "UA"));
        when(request.getHeaderNames()).thenReturn(names);
        when(request.getHeader("Host")).thenReturn("localhost");
        when(request.getHeader("UA")).thenReturn("agent");
        assertThat(ServletUtils.getHeaders(request)).containsEntry("Host", "localhost").containsEntry("UA", "agent");
    }

    // ========== getReferer ==========

    @Test
    void getRefererReturnsHeaderValue() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Referer")).thenReturn("https://example.com");
        assertThat(ServletUtils.getReferer(request)).isEqualTo("https://example.com");
    }

    // ========== 无参版本（依赖 RequestContextHolder） ==========

    @Test
    void noArgGetUserAgentReadsRequestContextHolder() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("User-Agent")).thenReturn("test-agent");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        assertThat(ServletUtils.getUserAgent()).isEqualTo("test-agent");
    }

    @Test
    void noArgGetLocaleReadsRequestContextHolder() {
        // 注意：当前实现 getLocale() 无参版本实际返回的是 User-Agent（疑似 bug，见报告），此处按现状断言
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("User-Agent")).thenReturn("test-agent");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        assertThat(ServletUtils.getLocale()).isEqualTo("test-agent");
    }

    // ========== 辅助方法 ==========

    private static ServletOutputStream outputStream(ByteArrayOutputStream baos) {
        return new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
            }

            @Override
            public void write(int b) {
                baos.write(b);
            }
        };
    }

    private static ServletInputStream inputStream(String content) {
        ByteArrayInputStream bais = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        return new ServletInputStream() {
            @Override
            public int read() {
                return bais.read();
            }

            @Override
            public boolean isFinished() {
                return bais.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }
        };
    }
}
