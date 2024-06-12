package xyz.migoo.framework.common.util.crypto;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import xyz.migoo.framework.common.exception.ErrorCode;
import xyz.migoo.framework.common.exception.util.ServiceExceptionUtil;

import javax.crypto.Cipher;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * RSA签名验签类
 *
 * @author xiaomi
 */
public class RSA {

    public static final ErrorCode SIGN_INVALID = new ErrorCode(580, "签名生成失败，请检查密钥配置是否正确");
    public static final ErrorCode DATA_DECRYPT_INVALID = new ErrorCode(580, "数据解密失败");
    /**
     * 签名算法
     */
    public static final String NONE_WITH_RSA = "NONEwithRSA";
    public static final String MD5_WITH_RSA = "MD5withRSA";
    public static final String MD2_WITH_RSA = "MD2withRSA";
    public static final String SHA1_WITH_RSA = "SHA1WithRSA";
    public static final String SHA256_WITH_RSA = "SHA256withRSA";
    public static final String SHA384_WITH_RSA = "SHA384withRSA";
    public static final String SHA512_WITH_RSA = "SHA512withRSA";
    public static final String SHA1_WITH_DSA = "SHA1withDSA";

    public static String sign(String content, String privateKey) {
        return sign(content, privateKey, NONE_WITH_RSA);
    }

    public static String sign(String content, PrivateKey privateKey) {
        return sign(content, privateKey, NONE_WITH_RSA);
    }

    public static String sign(String content, String privateKey, String algorithm) {
        return sign(content, toPrivateKey(privateKey), algorithm);
    }

    public static String sign(String content, PrivateKey privateKey, String algorithm) {
        try {
            java.security.Signature signature = java.security.Signature.getInstance(algorithm);
            signature.initSign(privateKey);
            signature.update(content.getBytes(StandardCharsets.UTF_8));
            return Base64.encode(signature.sign());
        } catch (Exception e) {
            throw ServiceExceptionUtil.get(SIGN_INVALID);
        }
    }

    public static void verify(String content, String sign, String publicKey) {
        verify(content, sign, publicKey, NONE_WITH_RSA);
    }

    public static void verify(String content, String sign, PublicKey publicKey) {
        verify(content, sign, publicKey, NONE_WITH_RSA);
    }

    public static void verify(String content, String sign, String publicKey, String algorithm) {
        verify(content, sign, toPublicKey(publicKey), algorithm);
    }

    public static void verify(String content, String sign, PublicKey publicKey, String algorithm) {
        boolean result;
        try {
            java.security.Signature signature = java.security.Signature.getInstance(algorithm);
            signature.initVerify(publicKey);
            signature.update(content.getBytes(StandardCharsets.UTF_8));
            result = signature.verify(Base64.decode(sign));
        } catch (Exception e) {
            throw ServiceExceptionUtil.get(-1, "签名验证异常");
        }
        if (!result) {
            throw ServiceExceptionUtil.get(-1, "签名验证失败");
        }
    }

    /**
     * 公钥加密
     *
     * @param content   明文
     * @param publicKey 公钥
     * @return 密文
     */
    public static String encrypt(String content, String publicKey) {
        return encrypt(content, publicKey, "RSA/ECB/PKCS1Padding");
    }

    public static String encrypt(String content, PublicKey publicKey) {
        return encrypt(content, publicKey, "RSA/ECB/PKCS1Padding");
    }

    public static String encrypt(String content, String publicKey, String algorithm) {
        return encrypt(content, toPublicKey(publicKey), algorithm);
    }

    public static String encrypt(String content, PublicKey publicKey, String algorithm) {
        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] signed = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return Base64.encode(signed);
        } catch (Exception e) {
            throw ServiceExceptionUtil.get(-1, "数据加密失败，请检查公钥配置是否正确");
        }
    }

    /**
     * 私钥解密
     *
     * @param content    密文
     * @param privateKey 私钥
     * @return 明文
     */
    public static String decrypt(String content, String privateKey) {
        return decrypt(content, privateKey, "RSA/ECB/PKCS1Padding");
    }

    public static String decrypt(String content, String privateKey, String algorithm) {
        return decrypt(content, toPrivateKey(privateKey), algorithm);
    }

    public static String decrypt(String content, PrivateKey privateKey) {
        return decrypt(content, privateKey, "RSA/ECB/PKCS1Padding");
    }

    public static String decrypt(String content, PrivateKey privateKey, String algorithm) {
        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] signed = cipher.doFinal(Base64.decode(content));
            return new String(signed, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw ServiceExceptionUtil.get(DATA_DECRYPT_INVALID);
        }
    }

    public static String toBase64String(String publicKey) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(new FileInputStream(publicKey));
            return Base64.encode(cert.getPublicKey().getEncoded());
        } catch (Exception e) {
            throw ServiceExceptionUtil.get(-1, "密钥转换失败，文件不存在或文件已损坏");
        }
    }

    public static String toBase64String(String privateKey, String pwd) {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            char[] password = StrUtil.isEmpty(pwd) ? null : pwd.toCharArray();
            keyStore.load(new FileInputStream(privateKey), password);
            return Base64.encode(keyStore.getKey(keyStore.aliases().nextElement(), password).getEncoded());
        } catch (Exception e) {
            throw ServiceExceptionUtil.get(-1, "密钥转换失败，文件不存在或文件已损坏");
        }
    }

    public static PrivateKey toPrivateKey(String privateKey) {
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decode(privateKey));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw ServiceExceptionUtil.get(-1, "密钥转换失败，请检查密钥配置是否正确");
        }
    }

    public static PublicKey toPublicKey(String publicKey) {
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decode(publicKey));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw ServiceExceptionUtil.get(-1, "密钥转换失败，请检查密钥配置是否正确");
        }
    }
}
