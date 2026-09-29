package xyz.migoo.framework.web.core.wrapper;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link CachedBodyHttpServletRequest} 单元测试
 *
 * <p>验证请求体在构造时一次性缓存，后续 getInputStream / getReader 均可重复读取。</p>
 */
class CachedBodyHttpServletRequestTest {

    private static final String BODY = "{\"name\":\"migoo\",\"age\":18}";

    private static HttpServletRequest requestWithBody(String content) throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getInputStream()).thenReturn(inputStream(content));
        return request;
    }

    @Test
    void constructorCachesRequestBody() throws Exception {
        CachedBodyHttpServletRequest wrapper = new CachedBodyHttpServletRequest(requestWithBody(BODY), 1024);
        assertThat(new String(wrapper.getCachedBody(), StandardCharsets.UTF_8)).isEqualTo(BODY);
    }

    @Test
    void getInputStreamCanBeReadMultipleTimes() throws Exception {
        CachedBodyHttpServletRequest wrapper = new CachedBodyHttpServletRequest(requestWithBody(BODY), 1024);

        // 第一次读取
        assertThat(new String(wrapper.getInputStream().readAllBytes(), StandardCharsets.UTF_8)).isEqualTo(BODY);
        // 第二次读取（重新创建的流）
        assertThat(new String(wrapper.getInputStream().readAllBytes(), StandardCharsets.UTF_8)).isEqualTo(BODY);
    }

    @Test
    void getReaderCanBeReadMultipleTimes() throws Exception {
        CachedBodyHttpServletRequest wrapper = new CachedBodyHttpServletRequest(requestWithBody(BODY), 1024);

        try (BufferedReader reader = wrapper.getReader()) {
            assertThat(reader.readLine()).isEqualTo(BODY);
        }
        try (BufferedReader reader = wrapper.getReader()) {
            assertThat(reader.readLine()).isEqualTo(BODY);
        }
    }

    @Test
    void inputStreamExposesAvailableAndIsFinished() throws Exception {
        CachedBodyHttpServletRequest wrapper = new CachedBodyHttpServletRequest(requestWithBody("abc"), 1024);
        ServletInputStream in = wrapper.getInputStream();

        assertThat(in.isFinished()).isFalse();
        assertThat(in.isReady()).isTrue();
        assertThat(in.available()).isEqualTo(3);
        assertThat(in.read()).isEqualTo('a');
        assertThat(in.available()).isEqualTo(2);
        // 全部读完
        assertThat(in.read()).isEqualTo('b');
        assertThat(in.read()).isEqualTo('c');
        assertThat(in.read()).isEqualTo(-1);
        assertThat(in.isFinished()).isTrue();
        assertThat(in.available()).isZero();
    }

    @Test
    void emptyBodyIsSupported() throws Exception {
        CachedBodyHttpServletRequest wrapper = new CachedBodyHttpServletRequest(requestWithBody(""), 1024);
        assertThat(wrapper.getCachedBody()).isEmpty();
        assertThat(wrapper.getInputStream().isFinished()).isTrue();
    }

    @Test
    void bodyLargerThanMaxSizeDegradesToOverflowMode() throws Exception {
        // maxSize=5，body 长度 10，超出上限不抛异常，降级为溢出模式
        CachedBodyHttpServletRequest wrapper = new CachedBodyHttpServletRequest(requestWithBody("1234567890"), 5);

        // getCachedBody 返回 null（请求体未完整缓存）
        assertThat(wrapper.getCachedBody()).isNull();
        // 请求体仍可通过组合流完整读取一次（前缀 + 原始流剩余）
        assertThat(new String(wrapper.getInputStream().readAllBytes(), StandardCharsets.UTF_8)).isEqualTo("1234567890");
    }

    @Test
    void overflowModePreservesFullBodyAcrossLargeInput() throws Exception {
        // body 远大于 read buffer(8192)，验证前缀 + 原始流剩余拼接后完整无损
        String body = "a".repeat(10000);
        CachedBodyHttpServletRequest wrapper = new CachedBodyHttpServletRequest(requestWithBody(body), 5000);

        assertThat(wrapper.getCachedBody()).isNull();
        assertThat(new String(wrapper.getInputStream().readAllBytes(), StandardCharsets.UTF_8)).isEqualTo(body);
    }

    @Test
    void bodyExactlyMaxSizeIsAllowed() throws Exception {
        CachedBodyHttpServletRequest wrapper = new CachedBodyHttpServletRequest(requestWithBody("12345"), 5);
        assertThat(new String(wrapper.getCachedBody(), StandardCharsets.UTF_8)).isEqualTo("12345");
    }

    private static ServletInputStream inputStream(String content) {
        ByteArrayInputStream bais = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        return new ServletInputStream() {
            @Override
            public int read() {
                return bais.read();
            }

            @Override
            public int available() {
                return bais.available();
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
