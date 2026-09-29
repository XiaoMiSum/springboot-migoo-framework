package xyz.migoo.framework.web.core.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import xyz.migoo.framework.web.core.wrapper.CachedBodyHttpServletRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link CacheRequestBodyFilter} 单元测试
 *
 * <p>验证结构化文本（JSON/XML/text）及无 Content-Type 请求包装为 {@link CachedBodyHttpServletRequest}、
 * 超大请求跳过缓存、GET/表单/文件上传/二进制请求直接透传等行为。</p>
 */
class CacheRequestBodyFilterTest {

    @Test
    void wrapsJsonRequestBody() throws Exception {
        HttpServletRequest request = jsonRequest("{\"name\":\"migoo\"}");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        new CacheRequestBodyFilter(1024).doFilter(request, response, filterChain);

        ArgumentCaptor<ServletRequest> captor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(filterChain).doFilter(captor.capture(), any());
        assertThat(captor.getValue()).isInstanceOf(CachedBodyHttpServletRequest.class);
        CachedBodyHttpServletRequest wrapped = (CachedBodyHttpServletRequest) captor.getValue();
        assertThat(new String(wrapped.getCachedBody(), StandardCharsets.UTF_8)).isEqualTo("{\"name\":\"migoo\"}");
    }

    @Test
    void passesThroughBinaryRequest() throws Exception {
        HttpServletRequest request = bodyRequest("binary", "application/octet-stream");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        new CacheRequestBodyFilter(1024).doFilter(request, response, filterChain);

        ArgumentCaptor<ServletRequest> captor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(filterChain).doFilter(captor.capture(), any());
        assertThat(captor.getValue()).isSameAs(request);
    }

    @Test
    void wrapsTextPlainRequest() throws Exception {
        HttpServletRequest request = bodyRequest("hello", "text/plain");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        new CacheRequestBodyFilter(1024).doFilter(request, response, filterChain);

        ArgumentCaptor<ServletRequest> captor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(filterChain).doFilter(captor.capture(), any());
        assertThat(captor.getValue()).isInstanceOf(CachedBodyHttpServletRequest.class);
    }

    @Test
    void wrapsXmlRequest() throws Exception {
        HttpServletRequest request = bodyRequest("<xml/>", "application/xml");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        new CacheRequestBodyFilter(1024).doFilter(request, response, filterChain);

        ArgumentCaptor<ServletRequest> captor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(filterChain).doFilter(captor.capture(), any());
        assertThat(captor.getValue()).isInstanceOf(CachedBodyHttpServletRequest.class);
    }

    @Test
    void wrapsRequestWithoutContentType() throws Exception {
        // axios 空 body POST 不发送 Content-Type，仍需缓存以避免 getReader/getInputStream 互斥
        HttpServletRequest request = bodyRequest("", null);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        new CacheRequestBodyFilter(1024).doFilter(request, response, filterChain);

        ArgumentCaptor<ServletRequest> captor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(filterChain).doFilter(captor.capture(), any());
        assertThat(captor.getValue()).isInstanceOf(CachedBodyHttpServletRequest.class);
    }

    @Test
    void skipsGetRequest() throws Exception {
        HttpServletRequest request = jsonRequest("{\"name\":\"migoo\"}");
        when(request.getMethod()).thenReturn("GET");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        new CacheRequestBodyFilter(1024).doFilter(request, response, filterChain);

        ArgumentCaptor<ServletRequest> captor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(filterChain).doFilter(captor.capture(), any());
        assertThat(captor.getValue()).isSameAs(request);
    }

    @Test
    void skipsFormUrlencodedRequest() throws Exception {
        HttpServletRequest request = bodyRequest("a=1", "application/x-www-form-urlencoded");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        new CacheRequestBodyFilter(1024).doFilter(request, response, filterChain);

        ArgumentCaptor<ServletRequest> captor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(filterChain).doFilter(captor.capture(), any());
        assertThat(captor.getValue()).isSameAs(request);
    }

    @Test
    void skipsMultipartRequest() throws Exception {
        HttpServletRequest request = bodyRequest("--boundary", "multipart/form-data; boundary=boundary");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        new CacheRequestBodyFilter(1024).doFilter(request, response, filterChain);

        ArgumentCaptor<ServletRequest> captor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(filterChain).doFilter(captor.capture(), any());
        assertThat(captor.getValue()).isSameAs(request);
    }

    @Test
    void skipsCachingWhenContentLengthExceedsMaxSize() throws Exception {
        HttpServletRequest request = jsonRequest("{\"name\":\"migoo\"}");
        when(request.getHeader("Content-Length")).thenReturn("5000");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        new CacheRequestBodyFilter(1024).doFilter(request, response, filterChain);

        ArgumentCaptor<ServletRequest> captor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(filterChain).doFilter(captor.capture(), any());
        assertThat(captor.getValue()).isSameAs(request);
    }

    @Test
    void wrapsWhenContentLengthInvalid() throws Exception {
        HttpServletRequest request = jsonRequest("{\"name\":\"migoo\"}");
        when(request.getHeader("Content-Length")).thenReturn("abc");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        new CacheRequestBodyFilter(1024).doFilter(request, response, filterChain);

        ArgumentCaptor<ServletRequest> captor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(filterChain).doFilter(captor.capture(), any());
        assertThat(captor.getValue()).isInstanceOf(CachedBodyHttpServletRequest.class);
    }

    @Test
    void fallsBackToOriginalRequestWhenReadFails() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContentType()).thenReturn("application/json");
        when(request.getInputStream()).thenThrow(new IOException("read failed"));
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        new CacheRequestBodyFilter(1024).doFilter(request, response, filterChain);

        ArgumentCaptor<ServletRequest> captor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(filterChain).doFilter(captor.capture(), any());
        assertThat(captor.getValue()).isSameAs(request);
    }

    /** 构造一个 Content-Type 为 application/json、可读的 request mock */
    private static HttpServletRequest jsonRequest(String body) throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContentType()).thenReturn("application/json");
        when(request.getInputStream()).thenReturn(inputStream(body));
        return request;
    }

    /** 构造一个指定 Content-Type（可为 null）、可读的 POST request mock */
    private static HttpServletRequest bodyRequest(String body, String contentType) throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        if (contentType != null) {
            when(request.getContentType()).thenReturn(contentType);
        }
        when(request.getMethod()).thenReturn("POST");
        when(request.getInputStream()).thenReturn(inputStream(body));
        return request;
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
