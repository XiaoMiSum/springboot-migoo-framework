package xyz.migoo.framework.common.util;

import com.google.common.collect.Maps;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 字符串工具类
 *
 * @author xiaomi
 */
public class StringUtils {

    /**
     * 截断字符串，超过最大长度时补充 ...
     */
    public static String maxLength(CharSequence str, int maxLength) {
        if (str == null) {
            return "";
        }
        String s = str.toString();
        if (s.length() <= maxLength - 3) {
            return s;
        }
        return s.substring(0, maxLength - 3) + "...";
    }

    /**
     * 指定字符串的替换
     */
    public static String replace(String str, Map<String, String> replaceMap) {
        assert StringUtils.hasText(str);
        if (replaceMap == null || replaceMap.isEmpty()) {
            return str;
        }
        String result = null;
        for (String key : replaceMap.keySet()) {
            result = str.replace(key, replaceMap.get(key));
        }
        return result;
    }

    /**
     * 将指定字符串首字母转小写
     *
     * @param str 字符串
     * @return 首字母小写
     */
    public static String firstLetter2Lower(String str) {
        assert StringUtils.hasText(str);
        char[] cs = str.toCharArray();
        cs[0] += 32;
        return String.valueOf(cs);
    }

    /**
     * 将指定字符串首字母转大写
     *
     * @param str 字符串
     * @return 首字母大写
     */
    public static String firstLetter2Updater(String str) {
        assert StringUtils.hasText(str);
        char[] cs = str.toCharArray();
        cs[0] -= 32;
        return String.valueOf(cs);
    }

    /**
     * 给定字符串是否以任何一个字符串开始
     * 给定字符串和数组为空都返回 false
     *
     * @param str      给定字符串
     * @param prefixes 需要检测的开始字符串
     * @since 3.0.6
     */
    public static boolean startWithAny(String str, Collection<String> prefixes) {
        if (!StringUtils.hasText(str) || CollectionUtils.isEmpty(prefixes)) {
            return false;
        }

        for (String prefix : prefixes) {
            if (str.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 分割字符串为 Long 列表
     */
    public static List<Long> splitToLong(String value, CharSequence separator) {
        if (!StringUtils.hasText(value)) {
            return List.of();
        }
        String[] parts = value.split(separator.toString());
        return Arrays.stream(parts)
                .filter(StringUtils::hasText)
                .mapToLong(Long::parseLong)
                .boxed()
                .collect(Collectors.toList());
    }

    /**
     * 分割字符串为 Integer 列表
     */
    public static List<Integer> splitToInt(String value, CharSequence separator) {
        if (!StringUtils.hasText(value)) {
            return List.of();
        }
        String[] parts = value.split(separator.toString());
        return Arrays.stream(parts)
                .filter(StringUtils::hasText)
                .mapToInt(Integer::parseInt)
                .boxed()
                .collect(Collectors.toList());
    }

    /**
     * 分割字符串为 Map
     */
    public static Map<String, String> splitToMap(String value, CharSequence separator1, CharSequence separator2) {
        if (!StringUtils.hasText(value)) {
            return Maps.newHashMap();
        }
        List<String> strings = Arrays.asList(value.split(separator1.toString()));
        Map<String, String> map = Maps.newHashMap();
        String sep = separator2.toString();
        strings.forEach(item -> {
            int index = item.indexOf(sep);
            if (index >= 0) {
                map.put(item.substring(0, index), (index + 1) == item.length() ? "" :
                        item.substring(index + 1));
            }
        });
        return map;
    }

    /**
     * 驼峰转下划线
     *
     * @param str 驼峰字符串
     * @return 下划线字符串
     */
    public static String toUnderlineCase(String str) {
        if (!StringUtils.hasText(str)) {
            return str;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * 判断对象是否为空白字符串
     *
     * @param obj 对象
     * @return 是否为空白
     */
    public static boolean isBlankIfStr(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof String str) {
            return str.isBlank();
        }
        return false;
    }

}
