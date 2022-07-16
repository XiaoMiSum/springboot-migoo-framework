package xyz.migoo.framework.common.util.res;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;

import javax.crypto.Cipher;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * RSA签名验签类
 *
 * @author xiaomi
 */
public class RSAEncrypt {
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

    public static String sign(String content, String privateKey) throws Exception {
        return encryptByPrivateKey(content, privateKey);
    }

    public static boolean verify(String content, String sign, String publicKey) throws Exception {
        return verify(content, sign, publicKey, NONE_WITH_RSA);
    }

    public static String sign(String content, String privateKey, String type) throws Exception {
        return encryptByPrivateKey(content, privateKey, type);
    }

    public static String sign(String content, PrivateKey privateKey, String type) throws Exception {
        return encryptByPrivateKey(content, privateKey, type);
    }

    public static boolean verify(String content, String sign, String publicKey, String type) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(Base64.decode(publicKey)));
        Signature signature = Signature.getInstance(type);
        signature.initVerify(pubKey);
        signature.update(content.getBytes(StandardCharsets.UTF_8));
        return signature.verify(Base64.decode(sign));
    }

    public static boolean verify(String content, String sign, PublicKey publicKey, String type) throws Exception {
        Signature signature = Signature.getInstance(type);
        signature.initVerify(publicKey);
        signature.update(content.getBytes(StandardCharsets.UTF_8));
        return signature.verify(Base64.decode(sign));
    }

    /**
     * 私钥加密
     *
     * @param content    加密明文
     * @param privateKey 私钥
     * @return
     */
    public static String encryptByPrivateKey(String content, String privateKey) throws Exception {
        return encryptByPrivateKey(content, privateKey, NONE_WITH_RSA);
    }

    public static String encryptByPrivateKey(String content, String privateKey, String type) throws Exception {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decode(privateKey));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey priKey = keyFactory.generatePrivate(keySpec);
        Signature signature = Signature.getInstance(type);
        signature.initSign(priKey);
        signature.update(content.getBytes(StandardCharsets.UTF_8));
        byte[] signed = signature.sign();
        return Base64.encode(signed);
    }

    public static String encryptByPrivateKey(String content, PrivateKey privateKey, String type) throws Exception {
        Signature signature = Signature.getInstance(type);
        signature.initSign(privateKey);
        signature.update(content.getBytes(StandardCharsets.UTF_8));
        byte[] signed = signature.sign();
        return Base64.encode(signed);
    }

    /**
     * 公钥加密
     *
     * @param content   加密明文
     * @param publicKey 公钥
     * @return
     */
    public static String encryptByPublicKey(String content, String publicKey) throws Exception {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decode(publicKey));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(keySpec);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        byte[] signed = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
        return Base64.encode(signed);
    }

    public static String encryptByPublicKey(String content, String publicKey, String algorithm) throws Exception {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decode(publicKey));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(keySpec);
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        byte[] signed = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
        return Base64.encode(signed);
    }

    /**
     * 私钥解密
     *
     * @param content    加密后的字符串
     * @param privateKey 私钥
     * @return
     */
    public static String decryptByPrivateKey(String content, String privateKey) throws Exception {
        // 取得私钥
        return decryptByPrivateKey(content, getPrivateKey(privateKey));
    }

    public static String decryptByPrivateKey(String content, PrivateKey privateKey) throws Exception {
        // 对数据解密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] signed = cipher.doFinal(Base64.decode(content));
        return new String(signed, StandardCharsets.UTF_8);
    }

    /**
     * 公钥解密
     *
     * @param content   加密后的字符串
     * @param publicKey 公钥
     * @return
     */
    public static String decryptByPublicKey(String content, String publicKey) throws Exception {
        // 取得私钥
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decode(publicKey));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(keySpec);
        // 对数据解密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, pubKey);
        byte[] signed = cipher.doFinal(Base64.decode(content));
        return new String(signed, StandardCharsets.UTF_8);
    }

    public static String publicFileToString(String path) throws Exception {
        InputStream inputStream = Files.newInputStream(Paths.get(path));
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate oCert = (X509Certificate) cf.generateCertificate(inputStream);
        PublicKey publicKey = oCert.getPublicKey();
        return Base64.encode(publicKey.getEncoded());
    }

    public static String privateFileToString(String path, String pwd) throws Exception {
        FileInputStream fis2 = new FileInputStream(path);
        KeyStore ks = KeyStore.getInstance("PKCS12");
        char[] password = StrUtil.isEmpty(pwd) ? null : pwd.toCharArray();
        ks.load(fis2, password);
        String alias = ks.aliases().nextElement();
        PrivateKey privateKey = (PrivateKey) ks.getKey(alias, password);
        return Base64.encode(privateKey.getEncoded());
    }

    public static PrivateKey getPrivateKey(String privateKey) throws Exception {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decode(privateKey));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    public static PublicKey getPublicKey(String publicKey) throws Exception {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decode(publicKey));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    public static void main(String[] args) throws Exception {
        System.out.println(privateFileToString("/Users/xiaomi/yilian.pfx", "11111111"));
    }


}
