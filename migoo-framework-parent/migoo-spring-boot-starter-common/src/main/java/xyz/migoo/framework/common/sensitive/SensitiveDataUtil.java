package xyz.migoo.framework.common.sensitive;

/**
 * 敏感数据脱敏工具（配合 {@link Sensitive} 注解使用，也可独立调用）
 *
 * @author xiaomi
 */
public final class SensitiveDataUtil {

    private SensitiveDataUtil() {
    }

    /**
     * 按策略脱敏
     *
     * @param value 原值
     * @param type  脱敏策略
     * @return 掩码后的值；{@code value} 或 {@code type} 为 null 时原样返回
     */
    public static String mask(String value, SensitiveType type) {
        if (value == null || value.isEmpty() || type == null) {
            return value;
        }
        return switch (type) {
            case MOBILE -> maskMiddle(value, 3, 4);
            case ID_CARD -> maskMiddle(value, 4, 4);
            case BANK_CARD -> maskMiddle(value, 4, 4);
            case EMAIL -> maskEmail(value);
            case PASSWORD -> "******";
        };
    }

    /**
     * 中间掩码：保留前 {@code prefix} 后 {@code suffix}；
     * 长度不足以保留两侧时整体掩码（避免掩码结果本身泄露信息）
     */
    private static String maskMiddle(String value, int prefix, int suffix) {
        if (value.length() <= prefix + suffix) {
            return "*".repeat(value.length());
        }
        return value.substring(0, prefix)
                + "*".repeat(value.length() - prefix - suffix)
                + value.substring(value.length() - suffix);
    }

    /**
     * 邮箱掩码：本地部分保留首字符 + {@code ***} + 域名；
     * 非邮箱格式（无 @）时整体掩码
     */
    private static String maskEmail(String value) {
        int at = value.indexOf('@');
        if (at <= 0) {
            return "*".repeat(value.length());
        }
        return value.charAt(0) + "***" + value.substring(at);
    }
}
