package xyz.migoo.framework.common.util.crypto;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.google.common.collect.Lists;
import xyz.migoo.framework.common.exception.util.ServiceExceptionUtil;

import java.util.*;

/**
 * @author xiaomi
 * Created at 2024/5/16 19:03
 */
public class Signature {

    /**
     * 签名源串
     *
     * @param data       数据源
     * @param ignoreKeys 忽略key
     * @return 签名对象
     */
    public static String getSignSource(Map<String, ?> data, String... ignoreKeys) {
        List<String> keyValList = new ArrayList<>();
        List<String> ignoreKeyList = Lists.newArrayList(ignoreKeys);
        ignoreKeyList.add("sign");
        for (Map.Entry<String, ?> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            // 检查为空是否参与签名
            if (StrUtil.isBlankIfStr(value) || ignoreKeyList.contains(key)) {
                continue;
            }
            keyValList.add(key + "=" + value);
        }
        Collections.sort(keyValList);
        return StrUtil.join("&", keyValList);
    }

    public static String getSign(String source) {
        return DigestUtil.md5Hex(source);
    }

    public static String getSignUpper(String source) {
        return getSign(source).toUpperCase(Locale.ROOT);
    }

    public static String getSign(Map<String, ?> data, String secretKey) {
        return getSign(data, secretKey, "appKey");
    }

    public static String getSignUpper(Map<String, ?> data, String secretKey) {
        return getSign(data, secretKey).toUpperCase(Locale.ROOT);
    }

    public static String getSign(Map<String, ?> data, String secretKey, String secretKeyName) {
        String source = getSignSource(data) + secretKeyName + "=" + secretKey;
        return DigestUtil.md5Hex(source);
    }

    public static String getSignUpper(Map<String, ?> data, String secretKey, String secretKeyName) {
        return getSign(data, secretKey, secretKeyName).toUpperCase(Locale.ROOT);
    }

    public static void verifySign(Map<String, ?> data, String secretKey, String sign, String... ignoreKeys) {
        if (!Objects.equals(sign, getSign(data, secretKey, "appKey"))) {
            throw ServiceExceptionUtil.get(-1, "签名验证失败");
        }
    }

    public static void verifySign(Map<String, ?> data, String secretKey, String secretKeyName, String sign, String... ignoreKeys) {
        if (!Objects.equals(sign, getSign(data, secretKey, secretKeyName))) {
            throw ServiceExceptionUtil.get(-1, "签名验证失败");
        }
    }

    public static void verifySignUpper(Map<String, ?> data, String secretKey, String sign, String... ignoreKeys) {
        if (!Objects.equals(sign, getSignUpper(data, secretKey, "appKey"))) {
            throw ServiceExceptionUtil.get(-1, "签名验证失败");
        }
    }

    public static void verifySignUpper(Map<String, ?> data, String secretKey, String secretKeyName, String sign, String... ignoreKeys) {
        if (!Objects.equals(sign, getSignUpper(data, secretKey, secretKeyName))) {
            throw ServiceExceptionUtil.get(-1, "签名验证失败");
        }
    }
}
