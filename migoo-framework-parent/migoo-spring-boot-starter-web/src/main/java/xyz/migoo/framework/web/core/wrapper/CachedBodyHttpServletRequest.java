package xyz.migoo.framework.web.core.wrapper;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.Getter;
import org.springframework.util.StreamUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 自定义请求体缓存包装类，支持多次读取请求体内容
 *
 * @author xiaomi
 */
@Getter
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

    /**
     * -- GETTER --
     * 获取缓存的请求体字节数组
     *
     * @return 缓存的请求体字节数组
     */
    private final byte[] cachedBody;

    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        // 读取并缓存请求体内容
        this.cachedBody = StreamUtils.copyToByteArray(request.getInputStream());
    }

    @Override
    public ServletInputStream getInputStream() {
        // 返回缓存的输入流
        return new CachedBodyServletInputStream(new ByteArrayInputStream(cachedBody));
    }

    @Override
    public BufferedReader getReader() {
        // 返回缓存的 Reader
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }

    /**
     * 自定义 ServletInputStream 实现
     */
    private static class CachedBodyServletInputStream extends ServletInputStream {

        private final ByteArrayInputStream inputStream;

        public CachedBodyServletInputStream(ByteArrayInputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public int read() {
            return inputStream.read();
        }

        @Override
        public int available() {
            return inputStream.available();
        }

        @Override
        public boolean isFinished() {
            return inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            // 不支持异步读取
            throw new UnsupportedOperationException("异步读取不受支持");
        }
    }
}
