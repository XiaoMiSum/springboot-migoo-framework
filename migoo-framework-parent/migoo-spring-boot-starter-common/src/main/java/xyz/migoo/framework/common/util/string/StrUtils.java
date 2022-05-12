package xyz.migoo.framework.common.util.string;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;

import java.util.Map;

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

}
