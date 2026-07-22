package xyz.migoo.framework.common.util;

import lombok.SneakyThrows;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * 文件工具类
 *
 * @author xiaomi
 */
public class FileUtils {

    /**
     * 创建临时文件
     * 该文件会在 JVM 退出时，进行删除
     *
     * @param data 文件内容
     * @return 文件
     */
    @SneakyThrows
    public static File createTempFile(String data) {
        // 创建文件，通过 UUID 保证唯一
        File file = File.createTempFile(UUID.randomUUID().toString().replace("-", ""), null);
        // 标记 JVM 退出时，自动删除
        file.deleteOnExit();
        // 写入内容
        Files.writeString(file.toPath(), data);
        return file;
    }
    
    /**
     * 创建临时文件
     * 该文件会在 JVM 退出时，进行删除
     *
     * @param data 文件内容
     * @return 文件
     */
    @SneakyThrows
    public static File createTempFile(byte[] data) {
        File file = createTempFile();
        // 写入内容
        Files.write(file.toPath(), data);
        return file;
    }

    /**
     * 创建临时文件，无内容
     * 该文件会在 JVM 退出时，进行删除
     *
     * @return 文件
     */
    @SneakyThrows
    public static File createTempFile() {
        // 创建文件，通过 UUID 保证唯一
        File file = File.createTempFile(UUID.randomUUID().toString().replace("-", ""), null);
        // 标记 JVM 退出时，自动删除
        file.deleteOnExit();
        return file;
    }

    /**
     * 生成文件路径
     *
     * @param content      文件内容
     * @param originalName 原始文件名
     * @return path，唯一不可重复
     */
    public static String generatePath(byte[] content, String originalName) {
        String sha256Hex = sha256Hex(content);
        // 情况一：如果存在 name，则优先使用 name 的后缀
        if (StringUtils.hasText(originalName)) {
            String extName = getExtName(originalName);
            return !StringUtils.hasText(extName) ? sha256Hex : sha256Hex + "." + extName;
        }
        // 情况二：基于 content 计算
        try {
            String contentType = xyz.migoo.framework.common.util.io.FileTypeUtils.getType(new ByteArrayInputStream(content));
            return sha256Hex + '.' + contentType;
        } catch (Exception e) {
            return sha256Hex;
        }
    }

    /**
     * 计算 SHA-256 哈希
     */
    private static String sha256Hex(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    /**
     * 获取文件扩展名
     */
    private static String getExtName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }

}
