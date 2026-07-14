package xyz.migoo.framework.web.core.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;
import xyz.migoo.framework.common.util.servlet.ServletUtils;
import xyz.migoo.framework.web.core.wrapper.CachedBodyHttpServletRequest;

import java.io.IOException;

/**
 * Request Body 缓存 Filter，实现它的可重复读取
 *
 * @author xiaomi
 */
public class CacheRequestBodyFilter extends OncePerRequestFilter {

    /**
     * 最大缓存 body 大小：10MB
     */
    private static final int MAX_CACHE_BODY_SIZE = 10 * 1024 * 1024;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws IOException, ServletException {
        // 超大请求跳过缓存，防止 OOM
        String contentLength = request.getHeader("Content-Length");
        if (contentLength != null) {
            try {
                if (Long.parseLong(contentLength) > MAX_CACHE_BODY_SIZE) {
                    filterChain.doFilter(request, response);
                    return;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        try {
            var wrappedRequest = new CachedBodyHttpServletRequest(request);
            filterChain.doFilter(wrappedRequest, response);
        } catch (IOException e) {
            // 如果读取失败 则直接使用原始请求
            filterChain.doFilter(request, response);
        }
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        // 只处理 json 请求内容
        return !ServletUtils.isJsonRequest(request);
    }

}
