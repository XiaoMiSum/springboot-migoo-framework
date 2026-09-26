package xyz.migoo.framework.common.sensitive;

/**
 * 敏感数据脱敏策略
 *
 * @author xiaomi
 */
public enum SensitiveType {

    /**
     * 手机号：保留前 3 后 4，如 {@code 138****5678}
     */
    MOBILE,

    /**
     * 身份证号：保留前 4 后 4，如 {@code 3301**********1234}
     */
    ID_CARD,

    /**
     * 邮箱：本地部分保留首字符（域名非敏感保留），如 {@code a***@example.com}
     */
    EMAIL,

    /**
     * 银行卡号：保留前 4 后 4，如 {@code 6222********7890}
     */
    BANK_CARD,

    /**
     * 密码：定长掩码 {@code ******}（不按原长度掩码，避免泄露长度信息）
     */
    PASSWORD
}
