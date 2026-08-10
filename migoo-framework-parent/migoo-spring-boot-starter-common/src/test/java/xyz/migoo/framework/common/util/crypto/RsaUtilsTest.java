package xyz.migoo.framework.common.util.crypto;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import xyz.migoo.framework.common.exception.ServiceException;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link RsaUtils} 单元测试
 */
class RsaUtilsTest {

    private static String privateKeyStr;
    private static String publicKeyStr;
    private static PrivateKey privateKey;
    private static PublicKey publicKey;

    @BeforeAll
    static void setUp() throws Exception {
        // 只生成一次 2048 位 RSA 密钥对，供所有用例复用
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();
        privateKeyStr = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        publicKeyStr = Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    @Test
    void signAndVerifyWithDefaultAlgorithm() {
        String content = "hello migoo";
        String sign = RsaUtils.sign(content, privateKeyStr);
        // 默认算法 NONEwithRSA 签名后验签成功
        assertThat(RsaUtils.verify(content, sign, publicKeyStr)).isTrue();
    }

    @Test
    void verifyWithDifferentContentReturnsFalse() {
        String sign = RsaUtils.sign("hello", privateKeyStr);
        assertThat(RsaUtils.verify("world", sign, publicKeyStr)).isFalse();
    }

    @Test
    void verifyWithNullSignReturnsFalse() {
        assertThat(RsaUtils.verify("hello", null, publicKeyStr)).isFalse();
    }

    @Test
    void verifyWithEmptySignReturnsFalse() {
        assertThat(RsaUtils.verify("hello", "", publicKeyStr)).isFalse();
    }

    @Test
    void signAndVerifyWithSha256() {
        String content = "hello migoo";
        String sign = RsaUtils.sign(content, privateKeyStr, RsaUtils.SHA256_WITH_RSA);
        assertThat(RsaUtils.verify(content, sign, publicKeyStr, RsaUtils.SHA256_WITH_RSA)).isTrue();
        assertThat(RsaUtils.verify("other", sign, publicKeyStr, RsaUtils.SHA256_WITH_RSA)).isFalse();
    }

    @Test
    void encryptDecryptRoundtripWithDefaultAlgorithm() {
        String plain = "加密解密回环测试";
        String cipher = RsaUtils.encrypt(plain, publicKey);
        assertThat(cipher).isNotEqualTo(plain);
        // 默认算法 RSA/ECB/PKCS1Padding
        assertThat(RsaUtils.decrypt(cipher, privateKey)).isEqualTo(plain);
    }

    @Test
    void encryptDecryptRoundtripWithExplicitAlgorithm() {
        String algorithm = "RSA/ECB/PKCS1Padding";
        String plain = "hello";
        String cipher = RsaUtils.encrypt(plain, publicKey, algorithm);
        assertThat(RsaUtils.decrypt(cipher, privateKey, algorithm)).isEqualTo(plain);
    }

    @Test
    void signWithInvalidPrivateKeyThrowsServiceException() {
        // 私钥字符串无法转换（toPrivateKey 在 try 之外调用）-> 错误码 -1（密钥转换失败）
        assertThatThrownBy(() -> RsaUtils.sign("hello", "invalid-private-key"))
                .isInstanceOfSatisfying(ServiceException.class,
                        e -> assertThat(e.getCode()).isEqualTo(-1));
    }

    @Test
    void decryptWithInvalidKeyThrowsServiceException() {
        // 私钥字符串无法转换（toPrivateKey 在 try 之外调用）-> 错误码 -1（密钥转换失败）
        assertThatThrownBy(() -> RsaUtils.decrypt("cipher", "invalid-private-key"))
                .isInstanceOfSatisfying(ServiceException.class,
                        e -> assertThat(e.getCode()).isEqualTo(-1));
    }

    @Test
    void decryptWithInvalidCipherThrowsDataDecryptInvalid() {
        // 密钥有效但密文非法（Base64 解码失败）-> 错误码 580（DATA_DECRYPT_INVALID）
        assertThatThrownBy(() -> RsaUtils.decrypt("not-base64!", privateKeyStr))
                .isInstanceOfSatisfying(ServiceException.class,
                        e -> assertThat(e.getCode()).isEqualTo(580));
    }

    @Test
    void toPrivateKeyWithValidBase64ReturnsPrivateKey() {
        assertThat(RsaUtils.toPrivateKey(privateKeyStr)).isInstanceOf(PrivateKey.class);
    }

    @Test
    void toPublicKeyWithValidBase64ReturnsPublicKey() {
        assertThat(RsaUtils.toPublicKey(publicKeyStr)).isInstanceOf(PublicKey.class);
    }

    @Test
    void toPrivateKeyWithGarbageThrowsServiceException() {
        assertThatThrownBy(() -> RsaUtils.toPrivateKey("garbage"))
                .isInstanceOfSatisfying(ServiceException.class,
                        e -> assertThat(e.getCode()).isEqualTo(-1));
    }

    @Test
    void toPublicKeyWithGarbageThrowsServiceException() {
        assertThatThrownBy(() -> RsaUtils.toPublicKey("garbage"))
                .isInstanceOfSatisfying(ServiceException.class,
                        e -> assertThat(e.getCode()).isEqualTo(-1));
    }
}
