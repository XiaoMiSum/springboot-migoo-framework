package xyz.migoo.framework.common.util.money;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link MoneyUtils} 单元测试
 */
class MoneyUtilsTest {

    @Test
    void calculateRatePrice() {
        // 1000 * 10% = 100
        assertThat(MoneyUtils.calculateRatePrice(1000, 10.0)).isEqualTo(100);
    }

    @Test
    void calculateRatePriceWithHalfUp() {
        // 1000 * 56.77% = 567.7 -> HALF_UP -> 568
        assertThat(MoneyUtils.calculateRatePrice(1000, 56.77)).isEqualTo(568);
    }

    @Test
    void calculateRatePriceFloor() {
        // 1000 * 56.77% = 567.7 -> FLOOR -> 567
        assertThat(MoneyUtils.calculateRatePriceFloor(1000, 56.77)).isEqualTo(567);
    }

    @Test
    void calculatorWithoutPercent() {
        // 100 * 2 = 200
        assertThat(MoneyUtils.calculator(100, 2, null)).isEqualTo(200);
    }

    @Test
    void calculatorWithPercentUsesIntegerDivision() {
        // 先整数除法：6020 / 100 = 60，再按 60.0% 计算：200 * 60 / 100 = 120
        assertThat(MoneyUtils.calculator(100, 2, 6020)).isEqualTo(120);
    }

    @Test
    void calculateRatePriceWithScaleAndRoundingMode() {
        assertThat(MoneyUtils.calculateRatePrice(1000, 10, 2, RoundingMode.HALF_UP))
                .isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void calculateRatePriceWithNullPriceReturnsZero() {
        assertThat(MoneyUtils.calculateRatePrice(null, 10, 2, RoundingMode.HALF_UP))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void fenToYuanInt() {
        assertThat(MoneyUtils.fenToYuan(100)).isEqualByComparingTo(new BigDecimal("1.00"));
        assertThat(MoneyUtils.fenToYuan(1)).isEqualByComparingTo(new BigDecimal("0.01"));
    }

    @Test
    void fenToYuanLong() {
        assertThat(MoneyUtils.fenToYuan(100L)).isEqualByComparingTo(new BigDecimal("1.00"));
    }

    @Test
    void fenToYuanNullLongReturnsZero() {
        assertThat(MoneyUtils.fenToYuan((Long) null)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void fenToYuanStr() {
        assertThat(MoneyUtils.fenToYuanStr(1)).isEqualTo("0.01");
    }

    @Test
    void yuanToFen() {
        assertThat(MoneyUtils.yuanToFen(new BigDecimal("1.23"))).isEqualTo(123L);
    }

    @Test
    void yuanToFenNullReturnsZero() {
        assertThat(MoneyUtils.yuanToFen(null)).isZero();
    }

    @Test
    void parseFenWithThousandSeparator() {
        assertThat(MoneyUtils.parseFen("1,234.56")).isEqualTo(123456L);
    }

    @Test
    void parseFenWithDecimal() {
        assertThat(MoneyUtils.parseFen("10.5")).isEqualTo(1050L);
    }

    @Test
    void parseFenWithInvalidAmountReturnsZero() {
        assertThat(MoneyUtils.parseFen("abc")).isZero();
    }

    @Test
    void parseFenWithNullOrEmptyReturnsZero() {
        assertThat(MoneyUtils.parseFen(null)).isZero();
        assertThat(MoneyUtils.parseFen("")).isZero();
    }

    @Test
    void priceMultiply() {
        assertThat(MoneyUtils.priceMultiply(new BigDecimal("10.5"), new BigDecimal("3")))
                .isEqualTo(new BigDecimal("31.50"));
    }

    @Test
    void priceMultiplyWithNullReturnsNull() {
        assertThat(MoneyUtils.priceMultiply(null, new BigDecimal("3"))).isNull();
        assertThat(MoneyUtils.priceMultiply(new BigDecimal("3"), null)).isNull();
    }

    @Test
    void priceMultiplyPercent() {
        assertThat(MoneyUtils.priceMultiplyPercent(new BigDecimal("100"), new BigDecimal("50")))
                .isEqualTo(new BigDecimal("50.00"));
    }

    @Test
    void safeMultiply() {
        // 10.123 * 2 = 20.246 -> HALF_UP 保留 2 位 -> 20.25
        assertThat(MoneyUtils.safeMultiply(new BigDecimal("10.123"), new BigDecimal("2"), 2))
                .isEqualByComparingTo(new BigDecimal("20.25"));
    }

    @Test
    void safeMultiplyWithNullReturnsZero() {
        assertThat(MoneyUtils.safeMultiply(null, new BigDecimal("2"), 2))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void calculatePercentage() {
        assertThat(MoneyUtils.calculatePercentage(new BigDecimal("100"), new BigDecimal("80")))
                .isEqualTo(new BigDecimal("80.00"));
    }

    @Test
    void calculatePercentageWithNullReturnsZero() {
        assertThat(MoneyUtils.calculatePercentage(null, new BigDecimal("80")))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void format() {
        assertThat(MoneyUtils.format(null)).isEqualTo("0.00");
        // HALF_UP：1.005 -> 1.01
        assertThat(MoneyUtils.format(new BigDecimal("1.005"))).isEqualTo("1.01");
    }

    @Test
    void compare() {
        BigDecimal one = new BigDecimal("1");
        assertThat(MoneyUtils.compare(null, null)).isZero();
        assertThat(MoneyUtils.compare(null, one)).isEqualTo(-1);
        assertThat(MoneyUtils.compare(one, null)).isEqualTo(1);
        assertThat(MoneyUtils.compare(new BigDecimal("1.5"), new BigDecimal("2.5"))).isNegative();
        assertThat(MoneyUtils.compare(one, one)).isZero();
    }
}
