package xyz.migoo.framework.common.util.date;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link DateUtils} 单元测试
 */
class DateUtilsTest {

    @Test
    void ofLocalDateTimeReturnsDateWithSameInstant() {
        LocalDateTime ldt = LocalDateTime.of(2024, 1, 2, 3, 4, 5);
        Date date = DateUtils.of(ldt);
        assertThat(date).isNotNull();
        assertThat(date.toInstant().toEpochMilli())
                .isEqualTo(ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }

    @Test
    void ofNullLocalDateTimeReturnsNull() {
        assertThat(DateUtils.of((LocalDateTime) null)).isNull();
    }

    @Test
    void ofDateRoundtrips() {
        Date date = new Date(1700000000000L);
        LocalDateTime ldt = DateUtils.of(date);
        assertThat(ldt).isNotNull();
        // Date -> LocalDateTime -> Date 保持同一时刻
        assertThat(DateUtils.of(ldt).toInstant().toEpochMilli())
                .isEqualTo(date.toInstant().toEpochMilli());
    }

    @Test
    void ofNullDateReturnsNull() {
        assertThat(DateUtils.of((Date) null)).isNull();
    }

    @Test
    void addTimeAddsDuration() {
        Duration duration = Duration.ofSeconds(10);
        long before = System.currentTimeMillis();
        Date result = DateUtils.addTime(duration);
        long after = System.currentTimeMillis();
        // ±1000ms 容差，避免时钟抖动
        assertThat(result.getTime())
                .isBetween(before + duration.toMillis() - 1000, after + duration.toMillis() + 1000);
    }

    @Test
    void isExpiredWithPastTimeReturnsTrue() {
        assertThat(DateUtils.isExpired(LocalDateTime.now().minusDays(1))).isTrue();
    }

    @Test
    void isExpiredWithFutureTimeReturnsFalse() {
        assertThat(DateUtils.isExpired(LocalDateTime.now().plusDays(1))).isFalse();
    }

    @Test
    void buildTimeBuildsMidnightDate() {
        Date date = DateUtils.buildTime(2024, 1, 2);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        assertThat(calendar.get(Calendar.YEAR)).isEqualTo(2024);
        assertThat(calendar.get(Calendar.MONTH)).isZero();
        assertThat(calendar.get(Calendar.DAY_OF_MONTH)).isEqualTo(2);
        assertThat(calendar.get(Calendar.HOUR_OF_DAY)).isZero();
        assertThat(calendar.get(Calendar.MINUTE)).isZero();
        assertThat(calendar.get(Calendar.SECOND)).isZero();
        assertThat(calendar.get(Calendar.MILLISECOND)).isZero();
    }

    @Test
    void buildTimeWithHourMinuteSecond() {
        Date date = DateUtils.buildTime(2024, 1, 2, 3, 4, 5);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        assertThat(calendar.get(Calendar.HOUR_OF_DAY)).isEqualTo(3);
        assertThat(calendar.get(Calendar.MINUTE)).isEqualTo(4);
        assertThat(calendar.get(Calendar.SECOND)).isEqualTo(5);
        assertThat(calendar.get(Calendar.MILLISECOND)).isZero();
    }

    @Test
    void maxDateHandlesNulls() {
        Date a = new Date(1000L);
        Date b = new Date(2000L);
        assertThat(DateUtils.max(null, a)).isSameAs(a);
        assertThat(DateUtils.max(a, null)).isSameAs(a);
        assertThat(DateUtils.max(a, b)).isSameAs(b);
        assertThat(DateUtils.max(b, a)).isSameAs(b);
    }

    @Test
    void maxLocalDateTimeHandlesNulls() {
        LocalDateTime a = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime b = LocalDateTime.of(2024, 1, 2, 0, 0);
        assertThat(DateUtils.max(null, a)).isSameAs(a);
        assertThat(DateUtils.max(a, null)).isSameAs(a);
        assertThat(DateUtils.max(a, b)).isSameAs(b);
        assertThat(DateUtils.max(b, a)).isSameAs(b);
    }

    @Test
    void isTodayWithNowReturnsTrue() {
        assertThat(DateUtils.isToday(LocalDateTime.now())).isTrue();
    }

    @Test
    void isTodayWithYesterdayReturnsFalse() {
        assertThat(DateUtils.isToday(LocalDateTime.now().minusDays(1))).isFalse();
    }

    @Test
    void isTodayWithNullReturnsFalse() {
        assertThat(DateUtils.isToday(null)).isFalse();
    }

    @Test
    void isYesterdayWithYesterdayReturnsTrue() {
        assertThat(DateUtils.isYesterday(LocalDateTime.now().minusDays(1))).isTrue();
    }

    @Test
    void isYesterdayWithTodayReturnsFalse() {
        assertThat(DateUtils.isYesterday(LocalDateTime.now())).isFalse();
    }

    @Test
    void isYesterdayWithNullReturnsFalse() {
        assertThat(DateUtils.isYesterday(null)).isFalse();
    }
}
