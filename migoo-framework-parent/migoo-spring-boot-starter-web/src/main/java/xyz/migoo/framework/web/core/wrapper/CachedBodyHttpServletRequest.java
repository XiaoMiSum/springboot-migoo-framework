package xyz.migoo.framework.web.core.wrapper;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;

/**
 * 自定义请求体缓存包装类，支持多次读取请求体内容。
 *
 * <p>当请求体超过 maxBodySize 时，不抛出异常，而是降级为"前缀 + 原始流剩余"的组合透传模式：
 * {@link #getCachedBody()} 返回 null，请求体仍可读取一次（不可重复读）。</p>
 *
 * @author xiaomi
 */
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

    /** 完整缓存的请求体；溢出模式下为 null */
    @Getter
    private final byte[] cachedBody;

    /** 溢出模式下已读取的前缀字节；正常模式下为 null */
    private final byte[] overflowPrefix;

    public CachedBodyHttpServletRequest(HttpServletRequest request, int maxBodySize) throws IOException {
        super(request);
        InputStream source = request.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.min(maxBodySize + 1, 8192));
        byte[] buffer = new byte[8192];
        int totalRead = 0;
        int bytesRead;
        boolean overflow = false;
        while ((bytesRead = source.read(buffer)) != -1) {
            totalRead += bytesRead;
            baos.write(buffer, 0, bytesRead);
            if (totalRead > maxBodySize) {
                // 超出上限：保留已读字节作为前缀，不再继续读取
                overflow = true;
                break;
            }
        }
        if (overflow) {
            this.cachedBody = null;
            this.overflowPrefix = baos.toByteArray();
        } else {
            this.cachedBody = baos.toByteArray();
            this.overflowPrefix = null;
        }
    }

    @Override
    public ServletInputStream getInputStream() {
        if (cachedBody != null) {
            return new CachedBodyServletInputStream(new ByteArrayInputStream(cachedBody));
        }
        // 溢出模式：前缀 + 原始流剩余部分，保证下游拿到完整请求体
        try {
            InputStream composite = new SequenceInputStream(
                    new ByteArrayInputStream(overflowPrefix),
                    super.getInputStream()
            );
            return new CachedBodyServletInputStream(composite);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }

    /**
     * 自定义 ServletInputStream 实现
     */
    private static class CachedBodyServletInputStream extends ServletInputStream {

        private final InputStream inputStream;

        public CachedBodyServletInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }

        @Override
        public int available() throws IOException {
            return inputStream.available();
        }

        @Override
        public boolean isFinished() {
            try {
                return inputStream.available() == 0;
            } catch (IOException e) {
                return true;
            }
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            throw new UnsupportedOperationException("异步读取不受支持");
        }
    }
}
