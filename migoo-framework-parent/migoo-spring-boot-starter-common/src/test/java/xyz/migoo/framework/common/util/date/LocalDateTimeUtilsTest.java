package xyz.migoo.framework.common.util.date;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link LocalDateTimeUtils} 单元测试
 */
class LocalDateTimeUtilsTest {

    @Test
    void emptyEqualsEpochStart() {
        assertThat(LocalDateTimeUtils.EMPTY).isEqualTo(LocalDateTime.of(1970, 1, 1, 0, 0, 0));
        assertThat(LocalDateTimeUtils.buildTime(1970, 1, 1))
                .isEqualTo(LocalDateTime.of(1970, 1, 1, 0, 0, 0));
    }

    @Test
    void addTimeAddsDuration() {
        LocalDateTime before = LocalDateTime.now();
        LocalDateTime result = LocalDateTimeUtils.addTime(Duration.ofSeconds(10));
        LocalDateTime after = LocalDateTime.now();
        // 结果落在 [before+10s, after+10s] 区间内
        assertThat(result).isBetween(before.plusSeconds(10), after.plusSeconds(10));
    }

    @Test
    void minusTimeSubtractsDuration() {
        LocalDateTime before = LocalDateTime.now();
        LocalDateTime result = LocalDateTimeUtils.minusTime(Duration.ofSeconds(10));
        LocalDateTime after = LocalDateTime.now();
        assertThat(result).isBetween(before.minusSeconds(10), after.minusSeconds(10));
    }

    @Test
    void beforeNowWithPastReturnsTrue() {
        assertThat(LocalDateTimeUtils.beforeNow(LocalDateTime.now().minusSeconds(5))).isTrue();
        assertThat(LocalDateTimeUtils.beforeNow(LocalDateTime.now().plusSeconds(5))).isFalse();
    }

    @Test
    void afterNowWithFutureReturnsTrue() {
        assertThat(LocalDateTimeUtils.afterNow(LocalDateTime.now().plusSeconds(5))).isTrue();
        assertThat(LocalDateTimeUtils.afterNow(LocalDateTime.now().minusSeconds(5))).isFalse();
    }

    @Test
    void buildTimeBuildsMidnight() {
        assertThat(LocalDateTimeUtils.buildTime(2024, 1, 2))
                .isEqualTo(LocalDateTime.of(2024, 1, 2, 0, 0, 0));
    }

    @Test
    void buildBetweenTimeReturnsTwoElements() {
        LocalDateTime[] times = LocalDateTimeUtils.buildBetweenTime(2024, 1, 1, 2024, 1, 31);
        assertThat(times).hasSize(2);
        assertThat(times[0]).isEqualTo(LocalDateTime.of(2024, 1, 1, 0, 0, 0));
        assertThat(times[1]).isEqualTo(LocalDateTime.of(2024, 1, 31, 0, 0, 0));
    }

    @Test
    void isBetweenWithNullsReturnsFalse() {
        LocalDateTime now = LocalDateTime.now();
        assertThat(LocalDateTimeUtils.isBetween(null, now.plusDays(1))).isFalse();
        assertThat(LocalDateTimeUtils.isBetween(now.minusDays(1), null)).isFalse();
    }

    @Test
    void isBetweenWithWideWindowReturnsTrue() {
        LocalDateTime now = LocalDateTime.now();
        assertThat(LocalDateTimeUtils.isBetween(now.minusDays(1), now.plusDays(1))).isTrue();
    }

    @Test
    void isBetweenWithPastWindowReturnsFalse() {
        LocalDateTime now = LocalDateTime.now();
        assertThat(LocalDateTimeUtils.isBetween(now.minusDays(2), now.minusDays(1))).isFalse();
    }

    @Test
    void isBetweenWithStringWideWindowReturnsTrue() {
        // 全天范围，必然包含当前时间
        assertThat(LocalDateTimeUtils.isBetween("00:00", "23:59:59")).isTrue();
    }

    @Test
    void isBetweenWithStringPastWindowReturnsFalse() {
        // 已过去的凌晨窗口，当前时间必然不落在其中
        assertThat(LocalDateTimeUtils.isBetween("00:00", "00:00:01")).isFalse();
    }

    @Test
    void isOverlapWithLocalTimeOverlappingReturnsTrue() {
        assertThat(LocalDateTimeUtils.isOverlap(
                LocalTime.of(10, 0), LocalTime.of(12, 0),
                LocalTime.of(11, 0), LocalTime.of(13, 0))).isTrue();
    }

    @Test
    void isOverlapWithLocalTimeDisjointReturnsFalse() {
        assertThat(LocalDateTimeUtils.isOverlap(
                LocalTime.of(10, 0), LocalTime.of(11, 0),
                LocalTime.of(12, 0), LocalTime.of(13, 0))).isFalse();
    }

    @Test
    void isOverlapWithLocalDateTime() {
        LocalDateTime now = LocalDateTime.now();
        assertThat(LocalDateTimeUtils.isOverlap(
                now.minusHours(2), now, now.minusHours(1), now.plusHours(1))).isTrue();
        assertThat(LocalDateTimeUtils.isOverlap(
                now.minusHours(2), now.minusHours(1), now.plusHours(1), now.plusHours(2))).isFalse();
    }

    @Test
    void isOverlapWithNullArgsReturnsFalse() {
        LocalDateTime now = LocalDateTime.now();
        assertThat(LocalDateTimeUtils.isOverlap(null, now, now, now)).isFalse();
        assertThat(LocalDateTimeUtils.isOverlap(now, null, now, now)).isFalse();
        assertThat(LocalDateTimeUtils.isOverlap(now, now, null, now)).isFalse();
        assertThat(LocalDateTimeUtils.isOverlap(now, now, now, null)).isFalse();
    }

    @Test
    void beginOfMonth() {
        assertThat(LocalDateTimeUtils.beginOfMonth(LocalDateTime.of(2024, 3, 15, 10, 30)))
                .isEqualTo(LocalDateTime.of(2024, 3, 1, 0, 0));
    }

    @Test
    void endOfMonth() {
        // LocalTime.MAX = 23:59:59.999999999
        assertThat(LocalDateTimeUtils.endOfMonth(LocalDateTime.of(2024, 3, 15, 10, 30)))
                .isEqualTo(LocalDateTime.of(2024, 3, 31, 23, 59, 59, 999_999_999));
    }

    @Test
    void betweenDaysSincePastDateIsPositive() {
        // 过去 3 天 -> 差值为正数 3
        assertThat(LocalDateTimeUtils.between(LocalDateTime.now().minusDays(3))).isEqualTo(3L);
    }

    @Test
    void betweenReturnsDuration() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 1, 12, 0);
        assertThat(LocalDateTimeUtils.between(start, end)).isEqualTo(Duration.ofHours(2));
    }

    @Test
    void betweenWithUnitReturnsLong() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 1, 12, 0);
        assertThat(LocalDateTimeUtils.between(start, end, ChronoUnit.MINUTES)).isEqualTo(120L);
    }

    @Test
    void getTodayIsStartOfToday() {
        assertThat(LocalDateTimeUtils.getToday()).isEqualTo(LocalDate.now().atStartOfDay());
    }

    @Test
    void getYesterdayIsStartOfYesterday() {
        assertThat(LocalDateTimeUtils.getYesterday())
                .isEqualTo(LocalDate.now().minusDays(1).atStartOfDay());
    }

    @Test
    void getMonthIsFirstDayOfMonth() {
        assertThat(LocalDateTimeUtils.getMonth())
                .isEqualTo(LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay());
    }

    @Test
    void getYearIsFirstDayOfYear() {
        assertThat(LocalDateTimeUtils.getYear())
                .isEqualTo(LocalDate.now().with(TemporalAdjusters.firstDayOfYear()).atStartOfDay());
    }

    @Test
    void isInIsInclusive() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = start.plusHours(1);
        assertThat(LocalDateTimeUtils.isIn(start, start, end)).isTrue();
        assertThat(LocalDateTimeUtils.isIn(end, start, end)).isTrue();
        assertThat(LocalDateTimeUtils.isIn(end.plusMinutes(1), start, end)).isFalse();
        assertThat(LocalDateTimeUtils.isIn(null, start, end)).isFalse();
        assertThat(LocalDateTimeUtils.isIn(start, null, end)).isFalse();
        assertThat(LocalDateTimeUtils.isIn(start, start, null)).isFalse();
    }

    @Test
    void beginOfDayWithNullReturnsNull() {
        assertThat(LocalDateTimeUtils.beginOfDay(null)).isNull();
    }

    @Test
    void beginOfDayResetsTime() {
        assertThat(LocalDateTimeUtils.beginOfDay(LocalDateTime.of(2024, 5, 6, 7, 8, 9)))
                .isEqualTo(LocalDateTime.of(2024, 5, 6, 0, 0));
    }
}
