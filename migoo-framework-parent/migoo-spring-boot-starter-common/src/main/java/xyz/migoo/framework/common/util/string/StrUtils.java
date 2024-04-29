package xyz.migoo.framework.common.util.string;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;

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
public class StrUtils {

    public static String maxLength(CharSequence str, int maxLength) {
        return StrUtil.maxLength(str, maxLength - 3); // -3 的原因，是该方法会补充 ... 恰好
    }

    /**
     * 指定字符串的
     *
     * @param str
     * @param replaceMap
     * @return
     */
    public static String replace(String str, Map<String, String> replaceMap) {
        assert StrUtil.isNotBlank(str);
        if (ObjectUtil.isEmpty(replaceMap)) {
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
        assert StrUtil.isNotBlank(str);
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
        assert StrUtil.isNotBlank(str);
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
        if (StrUtil.isEmpty(str) || ArrayUtil.isEmpty(prefixes)) {
            return false;
        }

        for (CharSequence suffix : prefixes) {
            if (StrUtil.startWith(str, suffix, false)) {
                return true;
            }
        }
        return false;
    }

    public static List<Long> splitToLong(String value, CharSequence separator) {
        long[] longs = StrUtil.splitToLong(value, separator);
        return Arrays.stream(longs).boxed().collect(Collectors.toList());
    }

    public static List<Integer> splitToInt(String value, CharSequence separator) {
        int[] longs = StrUtil.splitToInt(value, separator);
        return Arrays.stream(longs).boxed().collect(Collectors.toList());
    }
}
