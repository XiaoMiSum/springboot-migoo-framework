package xyz.migoo.framework.web.core.util;

import com.google.common.base.Strings;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletRequestWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import xyz.migoo.framework.common.util.JsonUtils;
import xyz.migoo.framework.common.util.network.NetworkUtils;
import xyz.migoo.framework.web.core.wrapper.CachedBodyHttpServletRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 客户端工具类
 *
 * @author xiaomi
 */
public class ServletUtils {

    /**
     * 返回 JSON 字符串
     *
     * @param response 响应
     * @param object   对象，会序列化成 JSON 字符串
     */
    // 必须使用 APPLICATION_JSON_UTF8_VALUE，否则会乱码
    public static void writeJSON(HttpServletResponse response, Object object) {
        String content = JsonUtils.toJsonString(object);
        write(response, content);
    }

    /**
     * 返回附件
     *
     * @param response 响应
     * @param filename 文件名
     * @param content  附件内容
     */
    public static void writeAttachment(HttpServletResponse response, String filename, byte[] content) throws IOException {
        // 设置 header 和 contentType
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        // 输出附件
        response.getOutputStream().write(content);
        response.getOutputStream().flush();
    }

    /**
     * @param request 请求
     * @return ua
     */
    public static String getUserAgent(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        return Objects.isNull(ua) ? "" : ua;
    }

    /**
     * @param request 请求
     * @return locale
     */
    public static String getLocale(HttpServletRequest request) {
        String locale = request.getHeader("Locale");
        return Strings.isNullOrEmpty(locale) ? "zh-CN" : locale;
    }

    /**
     * 获得请求
     *
     * @return HttpServletRequest
     */
    public static HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes)) {
            return null;
        }
        return ((ServletRequestAttributes) requestAttributes).getRequest();
    }

    public static String getLocale() {
        HttpServletRequest request = getRequest();
        return Objects.isNull(request) ? "zh-CN" : getUserAgent(request);
    }

    public static String getUserAgent() {
        HttpServletRequest request = getRequest();
        return Objects.isNull(request) ? null : getUserAgent(request);
    }

    public static String getClientIP() {
        HttpServletRequest request = getRequest();
        String[] headers = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};
        return Objects.isNull(request) ? null : getClientIPByHeader(request, headers);
    }

    /**
     * 获得当前请求的路由模板（无请求上下文时返回 {@code unknown}）
     *
     * <p>优先取 SpringMVC 最佳匹配模板（如 {@code /user/{id}}）而非真实 URI，
     * 用于可观测性指标 tag 时控制基数。</p>
     *
     * @return 路由模板；无请求上下文时为 {@code unknown}
     */
    public static String getRoutePattern() {
        return getRoutePattern(getRequest());
    }

    /**
     * 获得指定请求的路由模板
     *
     * <p>取值顺序：MVC 最佳匹配模板 → 请求 URI → {@code unknown}。</p>
     *
     * @param request 请求
     * @return 路由模板；请求为空或 URI 为空时为 {@code unknown}
     */
    public static String getRoutePattern(HttpServletRequest request) {
        if (Objects.isNull(request)) {
            return "unknown";
        }
        Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (pattern instanceof String text && !text.isBlank()) {
            return text;
        }
        String uri = request.getRequestURI();
        return Strings.isNullOrEmpty(uri) ? "unknown" : uri;
    }

    public static boolean isJsonRequest(ServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith(MediaType.APPLICATION_JSON_VALUE);
    }


    private static void write(HttpServletResponse response, String text) {
        response.setContentType("application/json;charset=utf-8");
        try (Writer writer = response.getWriter()) {
            writer.write(text);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 读取请求体字节数组
     *
     * @param request 请求
     * @return 请求体；请求流已被消费或读取失败时返回 {@code null}
     */
    public static byte[] getBodyBytes(ServletRequest request) {
        try {
            InputStream is = request.getInputStream();
            return is.readAllBytes();
        } catch (IOException | IllegalStateException e) {
            // getReader/getInputStream 二选一（Tomcat 不允许切换），日志等旁路读取必须降级而非抛出
            return null;
        }
    }

    /**
     * 读取请求体字符串
     *
     * <p>优先取 {@link CachedBodyHttpServletRequest} 的缓存体：请求往往被安全过滤器等外层包装件层层包裹，
     * 缓存件不在最外层，需逐层下钻；无缓存时回退读取 reader。</p>
     *
     * @param request 请求
     * @return 请求体；请求体已被消费（{@code @RequestBody} 已调用 {@code getInputStream()}）或读取失败时返回 {@code null}
     */
    public static String getBody(ServletRequest request) {
        CachedBodyHttpServletRequest cached = findCachedBody(request);
        if (cached != null) {
            byte[] body = cached.getCachedBody();
            return body == null ? null : new String(body, StandardCharsets.UTF_8);
        }
        try (final BufferedReader reader = request.getReader()) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException | IllegalStateException e) {
            // 同 getBodyBytes：请求体已被上游消费时不可再读，返回 null 让调用方按「无请求体」处理
            return null;
        }
    }

    /**
     * 在请求包装链中查找请求体缓存件
     *
     * @param request 请求（可能位于包装链任意一层）
     * @return 缓存件；未缓存时返回 {@code null}
     */
    private static CachedBodyHttpServletRequest findCachedBody(ServletRequest request) {
        ServletRequest current = request;
        while (current != null) {
            if (current instanceof CachedBodyHttpServletRequest cached) {
                return cached;
            }
            current = current instanceof ServletRequestWrapper wrapper ? wrapper.getRequest() : null;
        }
        return null;
    }

    public static Map<String, String> getParamMap(ServletRequest request) {
        Map<String, String> params = new HashMap<>();
        for (Map.Entry<String, String[]> entry : getParams(request).entrySet()) {
            params.put(entry.getKey(), String.join(",", entry.getValue()));
        }
        return params;
    }

    public static Map<String, String[]> getParams(ServletRequest request) {
        final Map<String, String[]> map = request.getParameterMap();
        return Collections.unmodifiableMap(map);
    }

    public static String getReferer() {
        HttpServletRequest request = getRequest();
        return Objects.isNull(request) ? null : getReferer(request);
    }

    public static String getReferer(HttpServletRequest request) {
        return request.getHeader("Referer");
    }

    public static Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            headers.put(name, request.getHeader(name));
        }
        return headers;
    }

    public static String getClientIP(HttpServletRequest request, String... otherHeaderNames) {
        String[] headers = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};
        if (otherHeaderNames != null && otherHeaderNames.length > 0) {
            headers = Arrays.copyOf(headers, headers.length + otherHeaderNames.length);
            System.arraycopy(otherHeaderNames, 0, headers, headers.length - otherHeaderNames.length, otherHeaderNames.length);
        }
        return getClientIPByHeader(request, headers);
    }

    public static String getClientIPByHeader(HttpServletRequest request, String... headerNames) {
        String ip;
        for (String header : headerNames) {
            ip = request.getHeader(header);
            if (!NetworkUtils.isUnknown(ip)) {
                return NetworkUtils.getMultistageReverseProxyIp(ip, null);
            }
        }
        ip = request.getRemoteAddr();
        return NetworkUtils.getMultistageReverseProxyIp(ip, null);
    }
}
