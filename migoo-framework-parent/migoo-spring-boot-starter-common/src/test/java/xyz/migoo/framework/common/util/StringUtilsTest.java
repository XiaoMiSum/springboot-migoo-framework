package xyz.migoo.framework.common.util;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * StringUtils 单元测试
 */
class StringUtilsTest {

    @Test
    void hasText() {
        assertThat(StringUtils.hasText(null)).isFalse();
        assertThat(StringUtils.hasText("")).isFalse();
        assertThat(StringUtils.hasText("   ")).isFalse();
        assertThat(StringUtils.hasText("\t\n")).isFalse();
        assertThat(StringUtils.hasText("a")).isTrue();
        assertThat(StringUtils.hasText(" a ")).isTrue();
    }

    @Test
    void maxLength() {
        // null -> 空字符串
        assertThat(StringUtils.maxLength(null, 10)).isEqualTo("");
        // 短字符串原样返回
        assertThat(StringUtils.maxLength("abc", 10)).isEqualTo("abc");
        // 长度 == maxLength - 3 时不截断
        assertThat(StringUtils.maxLength("abcdefg", 10)).isEqualTo("abcdefg");
        // 超过时截断并追加 ...
        assertThat(StringUtils.maxLength("abcdefghij", 6)).isEqualTo("abc...");
        assertThat(StringUtils.maxLength("abcdefg", 6)).isEqualTo("abc...");
    }

    @Test
    void replace_nullOrEmptyMapReturnsOriginal() {
        assertThat(StringUtils.replace("hello", null)).isEqualTo("hello");
        assertThat(StringUtils.replace("hello", Map.of())).isEqualTo("hello");
    }

    @Test
    void replace_replacesAllOccurrencesOfKey() {
        // 单个 key：替换该 key 的所有出现位置
        assertThat(StringUtils.replace("banana", Map.of("a", "X"))).isEqualTo("bXnXnX");
    }

    @Test
    void replace_multipleKeys_matchesActualSourceBehavior() {
        // 源码实现：循环内每次都对原始 str 执行 replace，最终返回最后一次迭代的结果
        // （并非累积替换），此处用 LinkedHashMap 保证迭代顺序确定
        Map<String, String> map = new LinkedHashMap<>();
        map.put("a", "x");
        map.put("o", "y");
        assertThat(StringUtils.replace("foo bar", map)).isEqualTo("fyy bar");
    }

    @Test
    void firstLetter2Lower() {
        // 仅传合法非空输入（源码有 assert 保护）
        assertThat(StringUtils.firstLetter2Lower("Abc")).isEqualTo("abc");
        assertThat(StringUtils.firstLetter2Lower("ABC")).isEqualTo("aBC");
        assertThat(StringUtils.firstLetter2Lower("A")).isEqualTo("a");
    }

    @Test
    void firstLetter2Updater() {
        // 仅传合法非空输入（源码有 assert 保护）
        assertThat(StringUtils.firstLetter2Updater("abc")).isEqualTo("Abc");
        assertThat(StringUtils.firstLetter2Updater("abc def")).isEqualTo("Abc def");
        assertThat(StringUtils.firstLetter2Updater("a")).isEqualTo("A");
    }

    @Test
    void startWithAny() {
        // 空字符串 / 空前缀集合 -> false
        assertThat(StringUtils.startWithAny("", List.of("a"))).isFalse();
        assertThat(StringUtils.startWithAny(null, List.of("a"))).isFalse();
        assertThat(StringUtils.startWithAny("hello", List.of())).isFalse();
        assertThat(StringUtils.startWithAny("hello", null)).isFalse();
        // 命中任意前缀 -> true
        assertThat(StringUtils.startWithAny("hello", List.of("he"))).isTrue();
        assertThat(StringUtils.startWithAny("hello", List.of("x", "hel"))).isTrue();
        assertThat(StringUtils.startWithAny("hello", List.of("x", "y"))).isFalse();
    }

    @Test
    void splitToLong() {
        assertThat(StringUtils.splitToLong("1,2,3", ",")).containsExactly(1L, 2L, 3L);
        // 空片段被过滤
        assertThat(StringUtils.splitToLong("1,,3", ",")).containsExactly(1L, 3L);
        assertThat(StringUtils.splitToLong(null, ",")).isEmpty();
        assertThat(StringUtils.splitToLong("", ",")).isEmpty();
        assertThat(StringUtils.splitToLong("  ", ",")).isEmpty();
    }

    @Test
    void splitToInt() {
        assertThat(StringUtils.splitToInt("1,2,3", ",")).containsExactly(1, 2, 3);
        assertThat(StringUtils.splitToInt("1,,3", ",")).containsExactly(1, 3);
        assertThat(StringUtils.splitToInt(null, ",")).isEmpty();
        assertThat(StringUtils.splitToInt("", ",")).isEmpty();
    }

    @Test
    void splitToMap() {
        assertThat(StringUtils.splitToMap("a=1,b=2", ",", "="))
                .containsEntry("a", "1").containsEntry("b", "2");
        assertThat(StringUtils.splitToMap(null, ",", "=")).isEmpty();
        assertThat(StringUtils.splitToMap("", ",", "=")).isEmpty();
        // 不含分隔符的条目被跳过
        assertThat(StringUtils.splitToMap("a=1,b", ",", "="))
                .containsExactlyEntriesOf(Map.of("a", "1"));
        // key 后无值 -> 空字符串
        assertThat(StringUtils.splitToMap("k=", ",", "="))
                .containsExactlyEntriesOf(Map.of("k", ""));
    }

    @Test
    void toUnderlineCase() {
        assertThat(StringUtils.toUnderlineCase("userName")).isEqualTo("user_name");
        assertThat(StringUtils.toUnderlineCase("UserName")).isEqualTo("user_name");
        assertThat(StringUtils.toUnderlineCase("user_name")).isEqualTo("user_name");
        // null / 空串原样返回
        assertThat(StringUtils.toUnderlineCase(null)).isNull();
        assertThat(StringUtils.toUnderlineCase("")).isEmpty();
    }

    @Test
    void isBlankIfStr() {
        assertThat(StringUtils.isBlankIfStr(null)).isTrue();
        assertThat(StringUtils.isBlankIfStr("   ")).isTrue();
        assertThat(StringUtils.isBlankIfStr("")).isTrue();
        assertThat(StringUtils.isBlankIfStr("x")).isFalse();
        // 非字符串对象 -> false
        assertThat(StringUtils.isBlankIfStr(42)).isFalse();
        assertThat(StringUtils.isBlankIfStr(new Object())).isFalse();
    }
}
