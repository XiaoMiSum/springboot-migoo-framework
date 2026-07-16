package xyz.migoo.framework.mybatis.core.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.security.MessageDigest;

/**
 * 字段加密的 TypeHandler 实现类，基于 AES 实现
 * 可通过 mybatis-plus.encryptor.password 配置项，设置密钥
 *
 * @author xiaomi
 */
public class EncryptTypeHandler extends BaseTypeHandler<String> {

    private static final String ENCRYPTOR_PROPERTY_NAME = "mybatis-plus.encryptor.password";
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/ECB/PKCS5Padding";

    private static volatile Cipher encryptCipher;
    private static volatile Cipher decryptCipher;
    private static volatile String password;

    private static String decrypt(String value) {
        if (value == null) {
            return null;
        }
        try {
            return new String(getDecryptCipher().doFinal(Base64.getDecoder().decode(value)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decrypt failed", e);
        }
    }

    public static String encrypt(String rawValue) {
        if (rawValue == null) {
            return null;
        }
        try {
            return Base64.getEncoder().encodeToString(getEncryptCipher().doFinal(rawValue.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("Encrypt failed", e);
        }
    }

    private static synchronized Cipher getEncryptCipher() throws Exception {
        if (encryptCipher != null) {
            return encryptCipher;
        }
        String pwd = getPassword();
        SecretKeySpec keySpec = new SecretKeySpec(generateKey(pwd), AES_ALGORITHM);
        encryptCipher = Cipher.getInstance(AES_TRANSFORMATION);
        encryptCipher.init(Cipher.ENCRYPT_MODE, keySpec);
        return encryptCipher;
    }

    private static synchronized Cipher getDecryptCipher() throws Exception {
        if (decryptCipher != null) {
            return decryptCipher;
        }
        String pwd = getPassword();
        SecretKeySpec keySpec = new SecretKeySpec(generateKey(pwd), AES_ALGORITHM);
        decryptCipher = Cipher.getInstance(AES_TRANSFORMATION);
        decryptCipher.init(Cipher.DECRYPT_MODE, keySpec);
        return decryptCipher;
    }

    private static String getPassword() {
        if (password != null) {
            return password;
        }
        // 从系统属性或环境变量获取密码
        password = System.getProperty(ENCRYPTOR_PROPERTY_NAME);
        if (password == null) {
            password = System.getenv(ENCRYPTOR_PROPERTY_NAME);
        }
        if (password == null || password.isEmpty()) {
            throw new RuntimeException("Password not configured. Please set " + ENCRYPTOR_PROPERTY_NAME);
        }
        return password;
    }

    /**
     * 生成 AES 密钥
     */
    private static byte[] generateKey(String password) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
        byte[] key = new byte[16];
        System.arraycopy(digest, 0, key, 0, Math.min(digest.length, 16));
        return key;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, encrypt(parameter));
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return decrypt(value);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return decrypt(value);
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return decrypt(value);
    }

}
