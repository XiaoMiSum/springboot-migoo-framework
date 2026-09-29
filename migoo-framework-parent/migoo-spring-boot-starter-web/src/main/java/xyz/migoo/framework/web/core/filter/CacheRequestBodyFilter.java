package xyz.migoo.framework.web.core.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;
import xyz.migoo.framework.web.core.util.ServletUtils;
import xyz.migoo.framework.web.core.wrapper.CachedBodyHttpServletRequest;

import java.io.IOException;

/**
 * Request Body 缓存 Filter，实现它的可重复读取
 *
 * @author xiaomi
 */
public class CacheRequestBodyFilter extends OncePerRequestFilter {

    private final int maxCacheBodySize;

    public CacheRequestBodyFilter(int maxCacheBodySize) {
        this.maxCacheBodySize = maxCacheBodySize;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws IOException, ServletException {
        // 超大请求跳过缓存，防止 OOM
        String contentLength = request.getHeader("Content-Length");
        if (contentLength != null) {
            try {
                if (Long.parseLong(contentLength) > maxCacheBodySize) {
                    filterChain.doFilter(request, response);
                    return;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        CachedBodyHttpServletRequest wrappedRequest;
        try {
            wrappedRequest = new CachedBodyHttpServletRequest(request, maxCacheBodySize);
        } catch (IOException e) {
            // 构造时读取流失败，回退到原始请求
            filterChain.doFilter(request, response);
            return;
        }
        filterChain.doFilter(wrappedRequest, response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        // GET/HEAD、表单、文件上传、二进制不缓存；仅缓存结构化文本及无 Content-Type 的请求
        return !ServletUtils.isCacheableRequestBody(request);
    }
}
