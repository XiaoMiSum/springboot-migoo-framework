package xyz.migoo.framework.apilog.core;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ApiErrorLog} 单元测试
 *
 * <p>验证 Lombok {@code @Data} 生成的 setter/getter、equals 行为（无 builder，仅 POJO）。</p>
 */
class ApiErrorLogTest {

    @Test
    void settersAndGettersRoundTripAllFields() {
        Date time = new Date();
        ApiErrorLog log = new ApiErrorLog();
        log.setApplicationName("app");
        log.setRequestMethod("GET");
        log.setRequestUrl("/api/test");
        log.setRequestParams("{\"a\":1}");
        log.setUserIp("127.0.0.1");
        log.setExceptionTime(time);
        log.setExceptionName("RuntimeException");
        log.setExceptionClassName("com.example.Foo");
        log.setExceptionFileName("Foo.java");
        log.setExceptionMethodName("run");
        log.setExceptionLineNumber(42);
        log.setExceptionStackTrace("stack");
        log.setExceptionRootCauseMessage("root cause");
        log.setExceptionMessage("message");

        assertThat(log.getApplicationName()).isEqualTo("app");
        assertThat(log.getRequestMethod()).isEqualTo("GET");
        assertThat(log.getRequestUrl()).isEqualTo("/api/test");
        assertThat(log.getRequestParams()).isEqualTo("{\"a\":1}");
        assertThat(log.getUserIp()).isEqualTo("127.0.0.1");
        assertThat(log.getExceptionTime()).isSameAs(time);
        assertThat(log.getExceptionName()).isEqualTo("RuntimeException");
        assertThat(log.getExceptionClassName()).isEqualTo("com.example.Foo");
        assertThat(log.getExceptionFileName()).isEqualTo("Foo.java");
        assertThat(log.getExceptionMethodName()).isEqualTo("run");
        assertThat(log.getExceptionLineNumber()).isEqualTo(42);
        assertThat(log.getExceptionStackTrace()).isEqualTo("stack");
        assertThat(log.getExceptionRootCauseMessage()).isEqualTo("root cause");
        assertThat(log.getExceptionMessage()).isEqualTo("message");
    }

    @Test
    void newInstanceHasNullFields() {
        ApiErrorLog log = new ApiErrorLog();
        assertThat(log.getApplicationName()).isNull();
        assertThat(log.getRequestMethod()).isNull();
        assertThat(log.getExceptionLineNumber()).isNull();
        assertThat(log.getExceptionTime()).isNull();
    }

    @Test
    void equalsConsidersAllFields() {
        ApiErrorLog a = sampleLog("app-a");
        ApiErrorLog b = sampleLog("app-a");
        ApiErrorLog c = sampleLog("app-b");

        assertThat(a).isEqualTo(b);
        assertThat(a).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
    }

    @Test
    void toStringContainsKeyFields() {
        ApiErrorLog log = sampleLog("app-t");
        assertThat(log.toString()).contains("app-t");
    }

    private static ApiErrorLog sampleLog(String appName) {
        ApiErrorLog log = new ApiErrorLog();
        log.setApplicationName(appName);
        log.setRequestMethod("GET");
        log.setRequestUrl("/api/x");
        log.setExceptionName("RuntimeException");
        log.setExceptionLineNumber(1);
        return log;
    }
}
