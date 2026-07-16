package xyz.migoo.framework.common.util.http;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * HTTP 工具类
 *
 * @author xiaomi
 */
public class HttpUtils {

    /**
     * 替换 URL 查询参数
     */
    public static String replaceUrlQuery(String url, String key, String value) {
        try {
            URI uri = URI.create(url);
            String query = uri.getQuery();
            Map<String, String> params = parseQuery(query);
            params.put(key, value);
            String newQuery = buildQuery(params);
            return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(),
                    newQuery, uri.getFragment()).toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to replace URL query", e);
        }
    }

    /**
     * 解析查询参数
     */
    private static Map<String, String> parseQuery(String query) {
        Map<String, String> params = new LinkedHashMap<>();
        if (query == null || query.isEmpty()) {
            return params;
        }
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                params.put(key, value);
            }
        }
        return params;
    }

    /**
     * 构建查询字符串
     */
    private static String buildQuery(Map<String, String> params) {
        if (params.isEmpty()) {
            return "";
        }
        StringJoiner joiner = new StringJoiner("&");
        params.forEach((key, value) -> {
            joiner.add(URLEncoder.encode(key, StandardCharsets.UTF_8) + "=" +
                    URLEncoder.encode(value, StandardCharsets.UTF_8));
        });
        return joiner.toString();
    }

}
