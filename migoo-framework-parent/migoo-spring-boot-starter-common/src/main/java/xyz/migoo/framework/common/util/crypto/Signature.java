package xyz.migoo.framework.common.util.crypto;

import com.google.common.collect.Lists;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xiaomi
 * Created at 2024/5/16 19:03
 */
public class Signature {

    public static String[] getIgnoreFields(Class<?> clazz) {
        List<String> fieldNameList = new ArrayList<>();
        List<Field> fieldList = new ArrayList<>();
        Class<?> tempClass = clazz;
        while (Objects.nonNull(tempClass)) {
            fieldList.addAll(Arrays.asList(tempClass.getDeclaredFields()));
            tempClass = tempClass.getSuperclass();
        }
        fieldList.stream()
                .filter(field -> Objects.nonNull(field.getAnnotation(SignIgnore.class)))
                .forEach(field -> fieldNameList.add(field.getName()));
        return fieldNameList.toArray(new String[0]);
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
        List<String> ignoreKeyList = Lists.newArrayList(ignoreFields);
        ignoreKeyList.add("sign");
        ignoreKeyList.add("signature");
        final Map<String, ?> filter = data.entrySet().stream()
                .filter(entry -> !isBlankIfStr(entry.getValue()) && !ignoreKeyList.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        List<String> kvs = new TreeSet<>(filter.keySet()).stream()
                .map(key -> "%s=%s".formatted(key, filter.get(key)))
                .collect(Collectors.toList());
        return String.join(delimiter.toString(), kvs);
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
        if (StringUtils.hasText(secretKey)) {
            source += StringUtils.hasText(delimiter) ? delimiter : "";
            source += StringUtils.hasText(secretKeyName) ? secretKeyName + "=" + secretKey : secretKey;
        }
        return md5Hex(source);
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
    public static boolean verifySign(Map<String, ?> data, String... ignoreFields) {
        // 1 使用 data 生成 sign
        return verifySign(data, (String) data.remove("sign"), ignoreFields);
    }

    public static boolean verifySign(Map<String, ?> data, String sign, String... ignoreFields) {
        // 1 使用 data 生成 sign
        return verifySign(data, sign, "", ignoreFields);
    }

    public static boolean verifySign(Map<String, ?> data, String sign, String secretKey, String... ignoreFields) {
        // 1 使用 data 生成 sign
        return verifySign(data, sign, secretKey, "appKey", ignoreFields);
    }

    public static boolean verifySign(Map<String, ?> data, String sign, String secretKey, String secretKeyName, String... ignoreFields) {
        // 1 使用 data 生成 sign
        return verifySign(data, sign, secretKey, secretKeyName, "&", ignoreFields);
    }

    public static boolean verifySign(Map<String, ?> data, String sign, String secretKey, String secretKeyName, String delimiter, String... ignoreFields) {
        // 1 使用 data 生成 sign
        if (!StringUtils.hasText(sign)) return false;
        return getSign(data, secretKey, secretKeyName, delimiter, ignoreFields).equals(sign);
    }

    public static boolean verifySignUpper(Map<String, ?> data, String... ignoreFields) {
        // 1 使用 data 生成 sign
        return verifySignUpper(data, (String) data.remove("sign"), ignoreFields);
    }

    public static boolean verifySignUpper(Map<String, ?> data, String sign, String... ignoreFields) {
        // 1 使用 data 生成 sign
        return verifySignUpper(data, sign, "", ignoreFields);
    }

    public static boolean verifySignUpper(Map<String, ?> data, String sign, String secretKey, String... ignoreFields) {
        // 1 使用 data 生成 sign
        return verifySignUpper(data, sign, secretKey, "appKey", ignoreFields);
    }

    public static boolean verifySignUpper(Map<String, ?> data, String sign, String secretKey, String secretKeyName, String... ignoreFields) {
        // 1 使用 data 生成 sign
        return verifySignUpper(data, sign, secretKey, secretKeyName, "&", ignoreFields);
    }

    public static boolean verifySignUpper(Map<String, ?> data, String sign, String secretKey, String secretKeyName, String delimiter, String... ignoreFields) {
        // 1 使用 data 生成 sign
        return getSignUpper(data, secretKey, secretKeyName, delimiter, ignoreFields).equals(sign);
    }

    /**
     * MD5 哈希
     */
    private static String md5Hex(String source) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(source.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    /**
     * 判断对象是否为空白字符串
     */
    private static boolean isBlankIfStr(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof String str) {
            return str.isBlank();
        }
        return false;
    }
}
