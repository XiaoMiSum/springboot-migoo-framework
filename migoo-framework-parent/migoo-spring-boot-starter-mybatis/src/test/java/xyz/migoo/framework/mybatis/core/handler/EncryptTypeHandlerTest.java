package xyz.migoo.framework.mybatis.core.handler;

import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EncryptTypeHandlerTest {

    private static final String ENCRYPTOR_PASSWORD_KEY = "mybatis-plus.encryptor.password";

    private final EncryptTypeHandler handler = new EncryptTypeHandler();

    @BeforeAll
    static void setUpPassword() {
        System.setProperty(ENCRYPTOR_PASSWORD_KEY, "test-encrypt-password");
    }

    @AfterAll
    static void tearDownPassword() {
        System.clearProperty(ENCRYPTOR_PASSWORD_KEY);
    }

    @Test
    void encryptReturnsNullForNull() {
        assertThat(EncryptTypeHandler.encrypt(null)).isNull();
    }

    @Test
    void encryptProducesV1GcmCiphertext() {
        String encrypted = EncryptTypeHandler.encrypt("hello");
        assertThat(encrypted).isNotBlank();
        assertThat(encrypted).isNotEqualTo("hello");
        // 新写入一律为 v1 GCM 前缀格式（随机 IV，同明文多次加密结果不同）
        assertThat(encrypted).startsWith(EncryptTypeHandler.VERSION_PREFIX);
        assertThat(EncryptTypeHandler.encrypt("hello")).isNotEqualTo(encrypted);
    }

    @Test
    void roundTripEncryptDecrypt() {
        assertThat(EncryptTypeHandler.decrypt(EncryptTypeHandler.encrypt("hello"))).isEqualTo("hello");
        assertThat(EncryptTypeHandler.decrypt(EncryptTypeHandler.encrypt("中文敏感数据")))
                .isEqualTo("中文敏感数据");
        assertThat(EncryptTypeHandler.decrypt(EncryptTypeHandler.encrypt(""))).isEmpty();
    }

    @Test
    void decryptReadsLegacyEcbCiphertext() throws Exception {
        // 生成历史格式密文：AES/ECB/PKCS5Padding + MD5 单轮派生（旧实现）
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/ECB/PKCS5Padding");
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
        byte[] digest = md.digest("test-encrypt-password".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        byte[] key = new byte[16];
        System.arraycopy(digest, 0, key, 0, Math.min(digest.length, 16));
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, new javax.crypto.spec.SecretKeySpec(key, "AES"));
        String legacy = java.util.Base64.getEncoder().encodeToString(
                cipher.doFinal("legacy-value".getBytes(java.nio.charset.StandardCharsets.UTF_8)));

        // 无 v1 前缀 → 走历史解密路径，旧数据平滑迁移
        assertThat(EncryptTypeHandler.decrypt(legacy)).isEqualTo("legacy-value");
    }

    @Test
    void setNonNullParameterEncryptsValue() throws Exception {
        PreparedStatement ps = mock(PreparedStatement.class);

        handler.setNonNullParameter(ps, 1, "hello", JdbcType.VARCHAR);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(ps).setString(org.mockito.ArgumentMatchers.eq(1), captor.capture());
        assertThat(captor.getValue()).isNotEqualTo("hello");
        assertThat(captor.getValue()).startsWith(EncryptTypeHandler.VERSION_PREFIX);
        assertThat(EncryptTypeHandler.decrypt(captor.getValue())).isEqualTo("hello");
    }

    @Test
    void getNullableResultDecryptsValue() throws Exception {
        String encrypted = EncryptTypeHandler.encrypt("hello");
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("phone")).thenReturn(encrypted);

        assertThat(handler.getNullableResult(rs, "phone")).isEqualTo("hello");
    }

    @Test
    void getNullableResultReturnsNullWhenValueNull() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("phone")).thenReturn(null);

        assertThat(handler.getNullableResult(rs, "phone")).isNull();
        assertThat(handler.getNullableResult(rs, 1)).isNull();
    }
}
