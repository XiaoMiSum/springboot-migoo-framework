package xyz.migoo.framework.common.util.money;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 金额工具类
 */
public class MoneyUtils {

    /**
     * 百分比对应的 BigDecimal 对象
     */
    public static final BigDecimal PERCENT_100 = BigDecimal.valueOf(100);

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal THOUSAND = new BigDecimal("1000");
    private static final int DEFAULT_SCALE = 2;
    private static final int PRICE_SCALE = 2;

    private MoneyUtils() {
    }

    /**
     * 计算百分比金额，四舍五入
     *
     * @param price 金额
     * @param rate  百分比，例如说 56.77% 则传入 56.77
     * @return 百分比金额
     */
    public static Integer calculateRatePrice(Integer price, Double rate) {
        return calculateRatePrice(price, rate, 0, RoundingMode.HALF_UP).intValue();
    }

    /**
     * 计算百分比金额，向下传入
     *
     * @param price 金额
     * @param rate  百分比，例如说 56.77% 则传入 56.77
     * @return 百分比金额
     */
    public static Integer calculateRatePriceFloor(Integer price, Double rate) {
        return calculateRatePrice(price, rate, 0, RoundingMode.FLOOR).intValue();
    }

    /**
     * 计算百分比金额
     *
     * @param price   金额（单位分）
     * @param count   数量
     * @param percent 折扣（单位分），列如 60.2%，则传入 6020
     * @return 商品总价
     */
    public static Integer calculator(Integer price, Integer count, Integer percent) {
        price = price * count;
        if (percent == null) {
            return price;
        }
        return MoneyUtils.calculateRatePriceFloor(price, (double) (percent / 100));
    }

    /**
     * 计算百分比金额
     *
     * @param price        金额
     * @param rate         百分比，例如说 56.77% 则传入 56.77
     * @param scale        保留小数位数
     * @param roundingMode 舍入模式
     */
    public static BigDecimal calculateRatePrice(Number price, Number rate, int scale, RoundingMode roundingMode) {
        BigDecimal priceBD = toBigDecimal(price);
        BigDecimal rateBD = toBigDecimal(rate);
        return priceBD.multiply(rateBD) // 乘以
                .divide(BigDecimal.valueOf(100), scale, roundingMode); // 除以 100
    }

    /**
     * 分转元
     *
     * @param fen 分
     * @return 元
     */
    public static BigDecimal fenToYuan(int fen) {
        return BigDecimal.valueOf(fen).divide(BigDecimal.valueOf(100), PRICE_SCALE, RoundingMode.HALF_UP);
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
     * <p>
     * 例如说 fen 为 1 时，则结果为 0.01
     *
     * @param fen 分
     * @return 元
     */
    public static String fenToYuanStr(int fen) {
        return fenToYuan(fen).toString();
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
     * 金额相乘，默认进行四舍五入
     * <p>
     * 位数：{@link #PRICE_SCALE}
     *
     * @param price 金额
     * @param count 数量
     * @return 金额相乘结果
     */
    public static BigDecimal priceMultiply(BigDecimal price, BigDecimal count) {
        if (price == null || count == null) {
            return null;
        }
        return price.multiply(count).setScale(PRICE_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 金额相乘（百分比），默认进行四舍五入
     * <p>
     * 位数：{@link #PRICE_SCALE}
     *
     * @param price   金额
     * @param percent 百分比
     * @return 金额相乘结果
     */
    public static BigDecimal priceMultiplyPercent(BigDecimal price, BigDecimal percent) {
        if (price == null || percent == null) {
            return null;
        }
        return price.multiply(percent).divide(PERCENT_100, PRICE_SCALE, RoundingMode.HALF_UP);
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

    /**
     * Number 转 BigDecimal
     */
    private static BigDecimal toBigDecimal(Number number) {
        if (number == null) {
            return BigDecimal.ZERO;
        }
        if (number instanceof BigDecimal) {
            return (BigDecimal) number;
        }
        if (number instanceof Integer || number instanceof Long) {
            return BigDecimal.valueOf(number.longValue());
        }
        return BigDecimal.valueOf(number.doubleValue());
    }

}
