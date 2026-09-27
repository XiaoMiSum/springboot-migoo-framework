package xyz.migoo.framework.mybatis.core.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

/**
 * 字段加密 TypeHandler：AES-256-GCM（随机 IV，前缀 {@value #VERSION_PREFIX} 标识）
 *
 * <p>密钥来源（按优先级）：</p>
 * <ol>
 *   <li>JVM 系统属性 {@value #PROPERTY_NAME}（{@code -Dmybatis-plus.encryptor.password=xxx}）</li>
 *   <li>环境变量 {@value #ENV_NAME}（合法可设置的环境变量名）</li>
 *   <li>环境变量 {@value #LEGACY_ENV_NAME}（历史遗留的带点环境变量名，兼容保留）</li>
 * </ol>
 *
 * <p>密钥派生使用 PBKDF2WithHmacSHA256（12 万轮，固定 salt），取代历史的 MD5 单轮派生。</p>
 *
 * <p><strong>兼容性</strong>：新写入的密文一律为 {@value #VERSION_PREFIX} 前缀的 GCM 格式；
 * 无前缀的历史密文（AES/ECB + MD5 派生密钥）仍可解密，便于平滑迁移——
 * 历史数据被重写（UPDATE 触发重新加密）后自动升级为新格式。</p>
 *
 * <p><strong>线程安全</strong>：{@link Cipher} 非线程安全，本类使用 {@link ThreadLocal} 按线程隔离；
 * 派生后的密钥缓存后只读共享。</p>
 *
 * @author xiaomi
 */
public class EncryptTypeHandler extends BaseTypeHandler<String> {

    /**
     * 密钥的 JVM 系统属性名（历史命名，保持兼容）
     */
    public static final String PROPERTY_NAME = "mybatis-plus.encryptor.password";

    /**
     * 密钥的环境变量名（推荐，OS 可正常设置）
     */
    public static final String ENV_NAME = "MIGOO_ENCRYPTOR_PASSWORD";

    /**
     * 历史环境变量名（带点，主流 OS 无法设置，仅为兼容保留）
     */
    public static final String LEGACY_ENV_NAME = "mybatis-plus.encryptor.password";

    /**
     * 新格式密文前缀
     */
    public static final String VERSION_PREFIX = "v1:";

    private static final String GCM_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String LEGACY_TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_BITS = 128;
    private static final int PBKDF2_ITERATIONS = 120_000;
    private static final int AES_KEY_BITS = 256;
    /**
     * 固定 salt：密钥本身是低熵口令派生，salt 的作用是固化派生参数而非防字典；
     * 真正的安全性依赖口令强度（建议 ≥32 位随机串）
     */
    private static final byte[] PBKDF2_SALT = "migoo-framework-encryptor-v1".getBytes(StandardCharsets.UTF_8);

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final ThreadLocal<Cipher> ENCRYPT_CIPHER = ThreadLocal.withInitial(() -> newCipher(GCM_TRANSFORMATION));
    private static final ThreadLocal<Cipher> DECRYPT_CIPHER = ThreadLocal.withInitial(() -> newCipher(GCM_TRANSFORMATION));
    private static final ThreadLocal<Cipher> LEGACY_DECRYPT_CIPHER = ThreadLocal.withInitial(() -> newCipher(LEGACY_TRANSFORMATION));

    private static volatile String password;
    private static volatile SecretKeySpec aesKey;
    private static volatile SecretKeySpec legacyAesKey;

    private static Cipher newCipher(String transformation) {
        try {
            return Cipher.getInstance(transformation);
        } catch (Exception e) {
            throw new IllegalStateException("[newCipher][初始化 Cipher 失败] transformation=" + transformation, e);
        }
    }

    /**
     * 加密为 v1 GCM 格式：{@code v1:Base64(iv || ciphertext)}
     *
     * @param rawValue 明文
     * @return 密文；rawValue 为 null 时返回 null
     */
    public static String encrypt(String rawValue) {
        if (rawValue == null) {
            return null;
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);
            Cipher cipher = ENCRYPT_CIPHER.get();
            cipher.init(Cipher.ENCRYPT_MODE, getAesKey(), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] ciphertext = cipher.doFinal(rawValue.getBytes(StandardCharsets.UTF_8));
            byte[] out = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(ciphertext, 0, out, iv.length, ciphertext.length);
            return VERSION_PREFIX + Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new IllegalStateException("[encrypt][字段加密失败]", e);
        }
    }

    /**
     * 解密：v1 前缀走 GCM；无前缀走历史 ECB + MD5 派生（兼容旧数据）
     *
     * @param value 密文
     * @return 明文；value 为 null 时返回 null
     */
    public static String decrypt(String value) {
        if (value == null) {
            return null;
        }
        try {
            if (value.startsWith(VERSION_PREFIX)) {
                byte[] payload = Base64.getDecoder().decode(value.substring(VERSION_PREFIX.length()));
                Cipher cipher = DECRYPT_CIPHER.get();
                cipher.init(Cipher.DECRYPT_MODE, getAesKey(),
                        new GCMParameterSpec(GCM_TAG_BITS, payload, 0, GCM_IV_LENGTH));
                return new String(cipher.doFinal(payload, GCM_IV_LENGTH, payload.length - GCM_IV_LENGTH),
                        StandardCharsets.UTF_8);
            }
            return decryptLegacy(value);
        } catch (Exception e) {
            throw new IllegalStateException("[decrypt][字段解密失败]", e);
        }
    }

    /**
     * 历史格式解密（AES/ECB/PKCS5Padding + MD5 单轮派生密钥），仅用于兼容读取旧密文
     */
    private static String decryptLegacy(String value) throws Exception {
        Cipher cipher = LEGACY_DECRYPT_CIPHER.get();
        cipher.init(Cipher.DECRYPT_MODE, getLegacyAesKey());
        byte[] plain = cipher.doFinal(Base64.getDecoder().decode(value));
        return new String(plain, StandardCharsets.UTF_8);
    }

    private static String getPassword() {
        String pwd = password;
        if (pwd != null && !pwd.isEmpty()) {
            return pwd;
        }
        pwd = System.getProperty(PROPERTY_NAME);
        if (pwd == null || pwd.isEmpty()) {
            pwd = System.getenv(ENV_NAME);
        }
        if (pwd == null || pwd.isEmpty()) {
            pwd = System.getenv(LEGACY_ENV_NAME);
        }
        if (pwd == null || pwd.isEmpty()) {
            throw new IllegalStateException("[getPassword][加密密钥未配置] 请通过 JVM 系统属性 -D"
                    + PROPERTY_NAME + "=xxx 或环境变量 " + ENV_NAME + " 配置（建议 ≥32 位随机串）");
        }
        password = pwd;
        return pwd;
    }

    /**
     * PBKDF2 派生 AES-256 密钥（新格式）
     */
    private static SecretKeySpec getAesKey() {
        SecretKeySpec key = aesKey;
        if (key != null) {
            return key;
        }
        synchronized (EncryptTypeHandler.class) {
            if (aesKey == null) {
                aesKey = deriveKey(getPassword());
            }
            return aesKey;
        }
    }

    /**
     * 历史 MD5 单轮派生（仅解密旧数据）
     */
    private static SecretKeySpec getLegacyAesKey() {
        SecretKeySpec key = legacyAesKey;
        if (key != null) {
            return key;
        }
        synchronized (EncryptTypeHandler.class) {
            if (legacyAesKey == null) {
                try {
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    byte[] digest = md.digest(getPassword().getBytes(StandardCharsets.UTF_8));
                    byte[] keyBytes = new byte[16];
                    System.arraycopy(digest, 0, keyBytes, 0, Math.min(digest.length, 16));
                    legacyAesKey = new SecretKeySpec(keyBytes, "AES");
                } catch (Exception e) {
                    throw new IllegalStateException("[getLegacyAesKey][派生历史密钥失败]", e);
                }
            }
            return legacyAesKey;
        }
    }

    private static SecretKeySpec deriveKey(String pwd) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            PBEKeySpec spec = new PBEKeySpec(pwd.toCharArray(), PBKDF2_SALT, PBKDF2_ITERATIONS, AES_KEY_BITS);
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            spec.clearPassword();
            return new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("[deriveKey][PBKDF2 派生密钥失败]", e);
        }
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, encrypt(parameter));
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return decrypt(rs.getString(columnName));
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return decrypt(rs.getString(columnIndex));
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return decrypt(cs.getString(columnIndex));
    }
}
