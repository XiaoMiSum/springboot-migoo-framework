package xyz.migoo.framework.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 金额工具类
 *
 * @author migoo
 * @since 1.3.16
 */
public final class Money {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal THOUSAND = new BigDecimal("1000");
    private static final int DEFAULT_SCALE = 2;

    private Money() {
    }

    /**
     * 分转元
     *
     * @param fen 分
     * @return 元（BigDecimal）
     */
    public static BigDecimal fenToYuan(Long fen) {
        if (fen == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(fen).divide(HUNDRED, DEFAULT_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 分转元（字符串）
     *
     * @param fen 分
     * @return 元（字符串，保留两位小数）
     */
    public static String fenToYuanStr(Long fen) {
        return fenToYuan(fen).toString();
    }

    /**
     * 元转分
     *
     * @param yuan 元
     * @return 分
     */
    public static Long yuanToFen(BigDecimal yuan) {
        if (yuan == null) {
            return 0L;
        }
        return yuan.multiply(HUNDRED).setScale(0, RoundingMode.HALF_UP).longValue();
    }

    /**
     * 千分位金额转分
     *
     * @param amount 千分位金额字符串，如 "1,234.56"
     * @return 分
     */
    public static Long parseFen(String amount) {
        if (amount == null || amount.isEmpty()) {
            return 0L;
        }
        // 移除千分位分隔符
        String cleaned = amount.replace(",", "");
        try {
            BigDecimal yuan = new BigDecimal(cleaned);
            return yuanToFen(yuan);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * 金额乘法（安全）
     *
     * @param amount  金额
     * @param factor  乘数
     * @param scale   保留小数位数
     * @return 结果
     */
    public static BigDecimal safeMultiply(BigDecimal amount, BigDecimal factor, int scale) {
        if (amount == null || factor == null) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(factor).setScale(scale, RoundingMode.HALF_UP);
    }

    /**
     * 计算百分比金额
     *
     * @param amount     金额
     * @param percentage 百分比（如 80 表示 80%）
     * @return 计算后的金额
     */
    public static BigDecimal calculatePercentage(BigDecimal amount, BigDecimal percentage) {
        if (amount == null || percentage == null) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(percentage).divide(HUNDRED, DEFAULT_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 格式化金额（保留两位小数）
     */
    public static String format(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        return amount.setScale(DEFAULT_SCALE, RoundingMode.HALF_UP).toString();
    }

    /**
     * 比较两个金额
     *
     * @return 负数表示 amount1 < amount2，0 表示相等，正数表示 amount1 > amount2
     */
    public static int compare(BigDecimal amount1, BigDecimal amount2) {
        if (amount1 == null && amount2 == null) {
            return 0;
        }
        if (amount1 == null) {
            return -1;
        }
        if (amount2 == null) {
            return 1;
        }
        return amount1.compareTo(amount2);
    }
}
