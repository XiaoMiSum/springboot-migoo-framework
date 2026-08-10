package xyz.migoo.framework.common.exception;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link ServiceExceptionUtil} 的单元测试
 * <p>
 * 注意：MESSAGES 是 JVM 内共享的静态 ConcurrentHashMap，本类使用互不重复的业务错误码
 * （1_000_000_xxx 区间），并在 @AfterEach 中清理，避免测试方法之间相互污染。
 *
 * @author xiaomi
 */
class ServiceExceptionUtilTest {

    /**
     * 记录当前测试方法注册的 (code, message)，用于 @AfterEach 清理
     */
    private static final List<Map.Entry<Integer, String>> REGISTERED = new ArrayList<>();

    private static void putAndRegister(Integer code, String message) {
        ServiceExceptionUtil.put(code, message);
        REGISTERED.add(Map.entry(code, message));
    }

    @AfterEach
    void cleanUp() {
        REGISTERED.forEach(entry -> ServiceExceptionUtil.delete(entry.getKey(), entry.getValue()));
        REGISTERED.clear();
    }

    @Test
    void put_shouldRegisterTemplateForErrorCode() {
        // 注册模板后，get(ErrorCode) 优先使用模板
        putAndRegister(1_000_000_001, "存在模板 {}");
        ServiceException exception = ServiceExceptionUtil.get(ErrorCode.of(1_000_000_001, "兜底提示"));
        assertThat(exception.getCode()).isEqualTo(1_000_000_001);
        // 无参数时不替换占位符，模板原样返回
        assertThat(exception.getMessage()).isEqualTo("存在模板 {}");
    }

    @Test
    void getErrorCode_withoutTemplate_shouldUseErrorCodeMsg() {
        // 未注册模板时，回退到 ErrorCode 自带的 msg
        ServiceException exception = ServiceExceptionUtil.get(ErrorCode.of(1_000_000_002, "兜底提示"));
        assertThat(exception.getCode()).isEqualTo(1_000_000_002);
        assertThat(exception.getMessage()).isEqualTo("兜底提示");
    }

    @Test
    void getErrorCodeWithParams_shouldFormatPlaceholders() {
        // 带参数时替换模板中的 {} 占位符
        putAndRegister(1_000_000_003, "你好 {}, 世界 {}");
        ServiceException exception = ServiceExceptionUtil.get(ErrorCode.of(1_000_000_003, "兜底"), "A", "B");
        assertThat(exception.getCode()).isEqualTo(1_000_000_003);
        assertThat(exception.getMessage()).isEqualTo("你好 A, 世界 B");
    }

    @Test
    void getWithCode_andTemplateRegistered_shouldUseTemplate() {
        // get(Integer) 从 MESSAGES 中取模板
        putAndRegister(1_000_000_004, "m{}t");
        ServiceException exception = ServiceExceptionUtil.get(1_000_000_004);
        assertThat(exception.getCode()).isEqualTo(1_000_000_004);
        assertThat(exception.getMessage()).isEqualTo("m{}t");
    }

    @Test
    void getWithCode_withoutTemplate_shouldThrowNpe() {
        // 未注册模板时 MESSAGES.get(code) 返回 null，doFormat 内 new StringBuilder(null.length() + 50) 会 NPE（与源码行为一致）
        assertThatThrownBy(() -> ServiceExceptionUtil.get(1_000_000_005))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void getCodeWithParams_shouldFormatPlaceholders() {
        // get(Integer, params) 使用注册的模板进行格式化
        putAndRegister(1_000_000_006, "a{}b{}c");
        ServiceException exception = ServiceExceptionUtil.get(1_000_000_006, "1", "2");
        assertThat(exception.getCode()).isEqualTo(1_000_000_006);
        assertThat(exception.getMessage()).isEqualTo("a1b2c");
    }

    @Test
    void get0_shouldBuildExceptionWithFormattedMessage() {
        // get0 直接使用传入的模板与参数
        ServiceException exception = ServiceExceptionUtil.get0(1_000_000_007, "值: {} 结束", "X");
        assertThat(exception.getCode()).isEqualTo(1_000_000_007);
        assertThat(exception.getMessage()).isEqualTo("值: X 结束");
    }

    @Test
    void getWithCodeAndDirectMessage_shouldNotFormat() {
        // get(code, message) 直接使用 message，不做格式化
        ServiceException exception = ServiceExceptionUtil.get(1_000_000_008, "直接消息");
        assertThat(exception.getCode()).isEqualTo(1_000_000_008);
        assertThat(exception.getMessage()).isEqualTo("直接消息");
    }

    @Test
    void putAll_shouldRegisterAllTemplates() {
        // putAll 批量注册模板
        Map<Integer, String> messages = new HashMap<>();
        messages.put(1_000_000_009, "putAll 模板 {}");
        ServiceExceptionUtil.putAll(messages);
        REGISTERED.addAll(messages.entrySet());
        ServiceException exception = ServiceExceptionUtil.get(ErrorCode.of(1_000_000_009, "兜底"), "V");
        assertThat(exception.getMessage()).isEqualTo("putAll 模板 V");
    }

    @Test
    void delete_shouldRemoveRegisteredTemplate() {
        // 删除后回退到 ErrorCode 自带的 msg
        putAndRegister(1_000_000_010, "待删除模板");
        ServiceExceptionUtil.delete(1_000_000_010, "待删除模板");
        ServiceException exception = ServiceExceptionUtil.get(ErrorCode.of(1_000_000_010, "兜底"));
        assertThat(exception.getMessage()).isEqualTo("兜底");
    }

    @Test
    void delete_withWrongMessage_shouldKeepTemplate() {
        // ConcurrentMap.remove(key, value) 要求 key 和 value 都匹配才会删除
        putAndRegister(1_000_000_011, "保留模板");
        ServiceExceptionUtil.delete(1_000_000_011, "不匹配的消息");
        ServiceException exception = ServiceExceptionUtil.get(ErrorCode.of(1_000_000_011, "兜底"));
        assertThat(exception.getMessage()).isEqualTo("保留模板");
    }

    @Test
    void doFormat_shouldReplaceAllPlaceholders() {
        // 正常替换全部占位符
        String result = ServiceExceptionUtil.doFormat(1, "hello {}, world {}", "A", "B");
        assertThat(result).isEqualTo("hello A, world B");
    }

    @Test
    void doFormat_tooManyParamsWithoutPlaceholder_shouldReturnPattern() {
        // 模板中没有 {} 且参数过多时，直接返回原模板
        String result = ServiceExceptionUtil.doFormat(2, "fixed", "a", "b");
        assertThat(result).isEqualTo("fixed");
    }

    @Test
    void doFormat_tooManyParams_shouldReturnPartialString() {
        // 模板只有一个 {} 却传入两个参数：替换第一个后停止，返回部分字符串（不会崩溃）
        String result = ServiceExceptionUtil.doFormat(3, "hello {}", "A", "B");
        assertThat(result).isEqualTo("hello A");
    }

    @Test
    void doFormat_tooFewParams_shouldAppendRemainder() {
        // 参数不足时，剩余的模板内容原样追加到末尾
        String result = ServiceExceptionUtil.doFormat(4, "hello {} world {}", "A");
        assertThat(result).isEqualTo("hello A world {}");
    }
}
