package xyz.migoo.framework.common.util;

import org.junit.jupiter.api.Test;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * HttpUtils 单元测试
 */
class HttpUtilsTest {

    @Test
    void replaceUrlQuery_replacesExistingParam() {
        String result = HttpUtils.replaceUrlQuery("https://example.com/path?a=1&b=2", "a", "5");
        // 替换 a，同时保留 b
        assertThat(result).isEqualTo("https://example.com/path?a=5&b=2");
    }

    @Test
    void replaceUrlQuery_addsParamWhenNoQuery() {
        String result = HttpUtils.replaceUrlQuery("https://example.com/path", "a", "5");
        assertThat(result).isEqualTo("https://example.com/path?a=5");
    }

    @Test
    void replaceUrlQuery_encodesSpecialCharacters() {
        // 空格被 URLEncoder 编码为 +
        String space = HttpUtils.replaceUrlQuery("https://example.com/path", "q", "hello world");
        assertThat(space).contains("q=hello+world");

        // 中文按 UTF-8 编码
        String expected = URLEncoder.encode("你好", StandardCharsets.UTF_8);
        String chinese = HttpUtils.replaceUrlQuery("https://example.com/path", "q", "你好");
        assertThat(chinese).contains("q=" + expected);
    }

    @Test
    void replaceUrlQuery_malformedUrlThrowsRuntimeException() {
        assertThatThrownBy(() -> HttpUtils.replaceUrlQuery("http://exa mple.com/path", "a", "1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to replace URL query");
    }
}
