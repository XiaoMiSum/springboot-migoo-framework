package xyz.migoo.framework.common.util.object;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ExceptionUtils} 单元测试
 */
class ExceptionUtilsTest {

    // ========== stacktraceToString ==========

    @Test
    void stacktraceToString_null() {
        assertThat(ExceptionUtils.stacktraceToString(null)).isEqualTo("");
    }

    @Test
    void stacktraceToString_exception() {
        IllegalArgumentException exception = new IllegalArgumentException("boom");
        String stack = ExceptionUtils.stacktraceToString(exception);
        assertThat(stack).contains("IllegalArgumentException").contains("boom");
    }

    // ========== getMessage ==========

    @Test
    void getMessage_null() {
        assertThat(ExceptionUtils.getMessage(null)).isEqualTo("");
    }

    @Test
    void getMessage_withMessage() {
        assertThat(ExceptionUtils.getMessage(new RuntimeException("boom"))).isEqualTo("RuntimeException: boom");
    }

    @Test
    void getMessage_withoutMessage() {
        // 无 message 时仅返回简单类名
        assertThat(ExceptionUtils.getMessage(new RuntimeException())).isEqualTo("RuntimeException");
    }

    // ========== getRootCauseMessage ==========

    @Test
    void getRootCauseMessage() {
        Throwable chain = new RuntimeException("outer", new IllegalStateException("inner"));
        assertThat(ExceptionUtils.getRootCauseMessage(chain)).isEqualTo("IllegalStateException: inner");
        // null 时返回空串
        assertThat(ExceptionUtils.getRootCauseMessage(null)).isEqualTo("");
    }

    // ========== getRootCause ==========

    @Test
    void getRootCause_null() {
        assertThat(ExceptionUtils.getRootCause(null)).isNull();
    }

    @Test
    void getRootCause_noCause_returnsSelf() {
        RuntimeException exception = new RuntimeException("x");
        assertThat(ExceptionUtils.getRootCause(exception)).isSameAs(exception);
    }

    @Test
    void getRootCause_nestedChain_returnsDeepest() {
        IllegalStateException inner = new IllegalStateException("inner");
        IllegalArgumentException middle = new IllegalArgumentException("middle", inner);
        RuntimeException outer = new RuntimeException("outer", middle);
        assertThat(ExceptionUtils.getRootCause(outer)).isSameAs(inner);
    }

    @Test
    void getRootCause_maxDepth() {
        IllegalStateException inner = new IllegalStateException("inner");
        IllegalArgumentException middle = new IllegalArgumentException("middle", inner);
        RuntimeException outer = new RuntimeException("outer", middle);

        // depth 0 -> 最顶层
        assertThat(ExceptionUtils.getRootCause(outer, 0)).isSameAs(outer);
        // depth 1 -> 第二层
        assertThat(ExceptionUtils.getRootCause(outer, 1)).isSameAs(middle);
        // 深度超过链长 -> 最深层
        assertThat(ExceptionUtils.getRootCause(outer, 10)).isSameAs(inner);
        // null -> null
        assertThat(ExceptionUtils.getRootCause(null, 3)).isNull();
    }

    // ========== isCausedBy ==========

    @Test
    void isCausedBy() {
        // null -> false
        assertThat(ExceptionUtils.isCausedBy(null, RuntimeException.class)).isFalse();

        // 直接匹配
        assertThat(ExceptionUtils.isCausedBy(new IllegalArgumentException(), IllegalArgumentException.class)).isTrue();

        // 嵌套匹配
        assertThat(ExceptionUtils.isCausedBy(new RuntimeException(new IllegalStateException()), IllegalStateException.class))
                .isTrue();

        // 不匹配
        assertThat(ExceptionUtils.isCausedBy(new RuntimeException(), IllegalStateException.class)).isFalse();

        // 多类型 varargs，命中其中之一即可
        assertThat(ExceptionUtils.isCausedBy(new IllegalArgumentException(),
                IllegalStateException.class, IllegalArgumentException.class)).isTrue();
    }

}
