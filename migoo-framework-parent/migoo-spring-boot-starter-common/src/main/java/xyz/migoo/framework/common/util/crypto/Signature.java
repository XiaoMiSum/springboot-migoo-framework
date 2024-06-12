package xyz.migoo.framework.common.util.crypto;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.google.common.collect.Lists;
import xyz.migoo.framework.common.exception.util.ServiceExceptionUtil;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author xiaomi
 * Created at 2024/5/16 19:03
 */
public class Signature {

    public static String[] getIgnoreFields(Class<?> clazz) {
        List<String> list = new ArrayList<>();
        List<Field> fieldList = new ArrayList<>();
        Class<?> tempClass = clazz;
        while (Objects.nonNull(tempClass)) {
            fieldList.addAll(Arrays.asList(tempClass.getDeclaredFields()));
            tempClass = tempClass.getSuperclass();
        }
        for (Field field : fieldList) {
            if (Objects.nonNull(field.getAnnotation(SignIgnore.class))) {
                list.add(field.getName());
            }
        }
        return list.toArray(new String[0]);
    }

    /**
     * 签名源串
     *
     * @param data         数据源
     * @param ignoreFields 忽略key
     * @return 签名对象
     */
    public static String getSignSource(Map<String, ?> data, String... ignoreFields) {
        return getSignSource(data, "&", ignoreFields);
    }

    /**
     * 签名源串
     *
     * @param data         数据源
     * @param delimiter    连接字符
     * @param ignoreFields 忽略key
     * @return 签名对象
     */
    public static String getSignSource(Map<String, ?> data, CharSequence delimiter, String... ignoreFields) {
        List<String> keyValList = new ArrayList<>();
        List<String> ignoreKeyList = Lists.newArrayList(ignoreFields);
        ignoreKeyList.add("sign");
        ignoreKeyList.add("signature");
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
        return StrUtil.join(delimiter, keyValList);
    }

    public static String getSign(String source) {
        return getSign(source, "");
    }

    public static String getSign(String source, String secretKey) {
        return getSign(source, secretKey, "appKey");
    }

    public static String getSign(String source, String secretKey, String secretKeyName) {
        return getSign(source, secretKey, secretKeyName, "&");
    }

    public static String getSign(String source, String secretKey, String secretKeyName, CharSequence delimiter) {
        if (StrUtil.isNotBlank(secretKey)) {
            source += StrUtil.isNotBlank(delimiter) ? delimiter : "";
            source += StrUtil.isNotBlank(secretKeyName) ? secretKeyName + "=" + secretKey : secretKey;
        }
        return DigestUtil.md5Hex(source);
    }

    public static String getSignUpper(String source) {
        return getSignUpper(source, "");
    }

    public static String getSignUpper(String source, String secretKey) {
        return getSignUpper(source, secretKey, "appKey");
    }

    public static String getSignUpper(String source, String secretKey, String secretKeyName) {
        return getSignUpper(source, secretKey, secretKeyName, "&");
    }

    public static String getSignUpper(String source, String secretKey, String secretKeyName, CharSequence delimiter) {
        return getSign(source, secretKey, secretKeyName, delimiter).toUpperCase(Locale.ROOT);
    }

    public static String getSign(Map<String, ?> data, String... ignoreFields) {
        return getSign(data, "", ignoreFields);
    }

    public static String getSign(Map<String, ?> data, String secretKey, String... ignoreFields) {
        return getSign(data, secretKey, "appKey", ignoreFields);
    }

    public static String getSign(Map<String, ?> data, String secretKey, String secretKeyName, String... ignoreFields) {
        return getSign(data, secretKey, secretKeyName, "&", ignoreFields);
    }

    public static String getSign(Map<String, ?> data, String secretKey, String secretKeyName, CharSequence delimiter, String... ignoreFields) {
        String source = getSignSource(data, delimiter, ignoreFields);
        return getSign(source, secretKey, secretKeyName, delimiter);
    }

    public static String getSignUpper(Map<String, ?> data, String... ignoreFields) {
        return getSignUpper(data, "", ignoreFields);
    }

    public static String getSignUpper(Map<String, ?> data, String secretKey, String... ignoreFields) {
        return getSignUpper(data, secretKey, "appKey", ignoreFields);
    }

    public static String getSignUpper(Map<String, ?> data, String secretKey, String secretKeyName, String... ignoreFields) {
        return getSignUpper(data, secretKey, secretKeyName, "&", ignoreFields);
    }

    public static String getSignUpper(Map<String, ?> data, String secretKey, String secretKeyName, CharSequence delimiter, String... ignoreFields) {
        String source = getSignSource(data, delimiter, ignoreFields);
        return getSignUpper(source, secretKey, secretKeyName, delimiter);
    }

    /**
     * 数据签名验证
     *
     * @param data         待验证的数据
     * @param ignoreFields 忽略的key
     */
    public static void verifySign(Map<String, ?> data, String... ignoreFields) {
        // 1 使用 data 生成 sign
        verifySign(data, (String) data.remove("sign"), ignoreFields);
    }

    public static void verifySign(Map<String, ?> data, String sign, String... ignoreFields) {
        // 1 使用 data 生成 sign
        verifySign(data, sign, "", ignoreFields);
    }

    public static void verifySign(Map<String, ?> data, String sign, String secretKey, String... ignoreFields) {
        // 1 使用 data 生成 sign
        verifySign(data, sign, secretKey, "appKey", ignoreFields);
    }

    public static void verifySign(Map<String, ?> data, String sign, String secretKey, String secretKeyName, String... ignoreFields) {
        // 1 使用 data 生成 sign
        verifySign(data, sign, secretKey, secretKeyName, "&", ignoreFields);
    }

    public static void verifySign(Map<String, ?> data, String sign, String secretKey, String secretKeyName, String delimiter, String... ignoreFields) {
        // 1 使用 data 生成 sign
        if (!getSign(data, secretKey, secretKeyName, delimiter, ignoreFields).equals(sign)) {
            throw ServiceExceptionUtil.get(-1, "来源数据签名验证失败");
        }
    }

    public static void verifySignUpper(Map<String, ?> data, String... ignoreFields) {
        // 1 使用 data 生成 sign
        verifySignUpper(data, (String) data.remove("sign"), ignoreFields);
    }

    public static void verifySignUpper(Map<String, ?> data, String sign, String... ignoreFields) {
        // 1 使用 data 生成 sign
        verifySignUpper(data, sign, "", ignoreFields);
    }

    public static void verifySignUpper(Map<String, ?> data, String sign, String secretKey, String... ignoreFields) {
        // 1 使用 data 生成 sign
        verifySignUpper(data, sign, secretKey, "appKey", ignoreFields);
    }

    public static void verifySignUpper(Map<String, ?> data, String sign, String secretKey, String secretKeyName, String... ignoreFields) {
        // 1 使用 data 生成 sign
        verifySignUpper(data, sign, secretKey, secretKeyName, "&", ignoreFields);
    }

    public static void verifySignUpper(Map<String, ?> data, String sign, String secretKey, String secretKeyName, String delimiter, String... ignoreFields) {
        // 1 使用 data 生成 sign
        if (!getSignUpper(data, secretKey, secretKeyName, delimiter, ignoreFields).equals(sign)) {
            throw ServiceExceptionUtil.get(-1, "来源数据签名验证失败");
        }
    }
}
