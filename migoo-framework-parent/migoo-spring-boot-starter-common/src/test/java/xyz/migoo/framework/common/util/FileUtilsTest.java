package xyz.migoo.framework.common.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FileUtils 单元测试
 */
class FileUtilsTest {

    private final List<File> tempFiles = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        for (File file : tempFiles) {
            try {
                Files.deleteIfExists(file.toPath());
            } catch (Exception ignored) {
                // 忽略清理失败
            }
        }
    }

    @Test
    void createTempFile_string() throws Exception {
        File file = FileUtils.createTempFile("hello");
        tempFiles.add(file);
        assertThat(file).exists();
        assertThat(Files.readString(file.toPath())).isEqualTo("hello");
    }

    @Test
    void createTempFile_byteArray() throws Exception {
        byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
        File file = FileUtils.createTempFile(data);
        tempFiles.add(file);
        assertThat(file).exists();
        assertThat(Files.readAllBytes(file.toPath())).isEqualTo(data);
    }

    @Test
    void createTempFile_empty() {
        File file = FileUtils.createTempFile();
        tempFiles.add(file);
        assertThat(file).exists();
        assertThat(file.length()).isZero();
    }

    @Test
    void generatePath_withOriginalName() {
        byte[] content = "hello".getBytes(StandardCharsets.UTF_8);
        assertThat(FileUtils.generatePath(content, "photo.png"))
                .isEqualTo(sha256Hex(content) + ".png");
    }

    @Test
    void generatePath_nameWithoutExtension() {
        byte[] content = "hello".getBytes(StandardCharsets.UTF_8);
        // 文件名无扩展名 -> 仅返回 sha256
        assertThat(FileUtils.generatePath(content, "noext"))
                .isEqualTo(sha256Hex(content));
    }

    @Test
    void generatePath_pngMagicNumberDetectedByContent() {
        // PNG 魔数：FileTypeUtils 根据文件头识别为 png
        byte[] png = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3, 4};
        assertThat(FileUtils.generatePath(png, null))
                .isEqualTo(sha256Hex(png) + ".png");
    }

    @Test
    void generatePath_unknownContentType() {
        // 实际源码行为：FileTypeUtils.getType 对未知类型返回 ""（不抛异常），
        // generatePath 拼接 '.' + ""，结果为 "sha256."（带一个尾随点）
        byte[] unknown = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        assertThat(FileUtils.generatePath(unknown, null))
                .isEqualTo(sha256Hex(unknown) + ".");
    }

    @Test
    void generatePath_emptyContent() {
        // 空内容：FileTypeUtils 读取字节数 < 2 返回 ""，同样带尾随点
        assertThat(FileUtils.generatePath(new byte[0], null))
                .isEqualTo(sha256Hex(new byte[0]) + ".");
    }

    private static String sha256Hex(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(data));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
