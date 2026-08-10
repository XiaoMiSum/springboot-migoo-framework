package xyz.migoo.framework.common.util.crypto;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SignatureUtils} 单元测试
 */
class SignatureUtilsTest {

    /** 测试用基类：baseField 标记 @SignIgnore */
    static class Base {
        @SignIgnore
        private String baseField;
    }

    /** 测试用子类：childField 标记 @SignIgnore，normal 不标记 */
    static class Child extends Base {
        @SignIgnore
        private String childField;
        private String normal;
    }

    @Test
    void getIgnoreFieldsReturnsAnnotatedFieldsAcrossHierarchy() {
        String[] fields = SignatureUtils.getIgnoreFields(Child.class);
        // 递归收集父类字段：包含 baseField 与 childField，不包含 normal
        assertThat(fields).containsExactlyInAnyOrder("baseField", "childField");
        assertThat(fields).doesNotContain("normal");
    }

    @Test
    void getSignSourceSortsKeysAndJoinsWithAmpersand() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("b", 2);
        data.put("a", 1);
        data.put("c", 3);
        // TreeSet 排序 -> a=1&b=2&c=3
        assertThat(SignatureUtils.getSignSource(data)).isEqualTo("a=1&b=2&c=3");
    }

    @Test
    void getSignSourceSkipsBlankValues() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("a", "x");
        data.put("b", null);
        data.put("c", "");
        data.put("d", "   ");
        assertThat(SignatureUtils.getSignSource(data)).isEqualTo("a=x");
    }

    @Test
    void getSignSourceAlwaysSkipsSignKeys() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("a", "x");
        data.put("sign", "s");
        data.put("signature", "s");
        // sign/signature 即使不在 ignoreFields 中也会被跳过
        assertThat(SignatureUtils.getSignSource(data)).isEqualTo("a=x");
    }

    @Test
    void getSignSourceWithCustomDelimiter() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("a", 1);
        data.put("b", 2);
        assertThat(SignatureUtils.getSignSource(data, (CharSequence) ";")).isEqualTo("a=1;b=2");
    }

    @Test
    void getSignReturnsMd5Hex() throws Exception {
        String sign = SignatureUtils.getSign("hello");
        assertThat(sign).isEqualTo(md5("hello"));
        // 32 位小写十六进制
        assertThat(sign).hasSize(32).matches("[0-9a-f]{32}");
    }

    @Test
    void getSignWithSecretKeyAppendsAppKeySuffix() throws Exception {
        assertThat(SignatureUtils.getSign("hello", "secret"))
                .isEqualTo(md5("hello&appKey=secret"));
    }

    @Test
    void getSignWithCustomSecretKeyName() throws Exception {
        assertThat(SignatureUtils.getSign("hello", "secret", "myKey"))
                .isEqualTo(md5("hello&myKey=secret"));
    }

    @Test
    void getSignWithCustomDelimiter() throws Exception {
        assertThat(SignatureUtils.getSign("hello", "secret", "appKey", ";"))
                .isEqualTo(md5("hello;appKey=secret"));
    }

    @Test
    void getSignWithEmptySecretKeyHasNoSuffix() throws Exception {
        // 空 secretKey 时不追加后缀
        assertThat(SignatureUtils.getSign("hello", "")).isEqualTo(md5("hello"));
    }

    @Test
    void getSignUpperReturnsUppercase() throws Exception {
        assertThat(SignatureUtils.getSignUpper("hello")).isEqualTo(md5("hello").toUpperCase());
    }

    @Test
    void getSignFromDataMap() throws Exception {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", "alice");
        data.put("age", 30);
        // getSignSource("age=30&name=alice") + 默认 appKey
        assertThat(SignatureUtils.getSign(data, "s3cret", new String[0]))
                .isEqualTo(md5("age=30&name=alice&appKey=s3cret"));
    }

    @Test
    void verifySignReturnsTrueWhenSignMatches() {
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("name", "alice");
        data.put("age", 30);
        data.put("sign", SignatureUtils.getSign(data));
        assertThat(SignatureUtils.verifySign(data)).isTrue();
    }

    @Test
    void verifySignReturnsFalseWhenSignMismatches() {
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("name", "alice");
        data.put("age", 30);
        String sign = SignatureUtils.getSign(data);
        data.put("name", "bob");
        assertThat(SignatureUtils.verifySign(data, sign, new String[0])).isFalse();
    }

    @Test
    void verifySignWithNullOrEmptySignReturnsFalse() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("a", 1);
        assertThat(SignatureUtils.verifySign(data, null, new String[0])).isFalse();
        assertThat(SignatureUtils.verifySign(data, "", new String[0])).isFalse();
    }

    @Test
    void verifySignUpperRoundtrip() {
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("name", "alice");
        data.put("age", 30);
        data.put("sign", SignatureUtils.getSignUpper(data));
        assertThat(SignatureUtils.verifySignUpper(data)).isTrue();
    }

    @Test
    void verifySignRemovesSignKeyFromMap() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("name", "alice");
        data.put("age", 30);
        String expected = SignatureUtils.getSign(data);
        data.put("sign", expected);
        assertThat(SignatureUtils.verifySign(data)).isTrue();
        // verifySign 会先把 "sign" 键从 map 中移除
        assertThat(data).doesNotContainKey("sign");
    }

    private static String md5(String source) throws Exception {
        byte[] digest = MessageDigest.getInstance("MD5")
                .digest(source.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte b : digest) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
