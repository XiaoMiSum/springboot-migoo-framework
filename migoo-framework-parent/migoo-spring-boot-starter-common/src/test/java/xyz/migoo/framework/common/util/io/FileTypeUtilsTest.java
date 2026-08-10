package xyz.migoo.framework.common.util.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link FileTypeUtils} 单元测试
 */
class FileTypeUtilsTest {

    /** PNG 魔数：89 50 4E 47 0D 0A 1A 0A */
    private static final byte[] PNG_MAGIC = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    /** JPEG 魔数：FF D8 FF */
    private static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF,
            (byte) 0xE0, 0x00, 0x10, 0x4A, 0x46};
    /** PDF 魔数：%PDF-1. */
    private static final byte[] PDF_MAGIC = {0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E};
    /** ZIP 魔数：50 4B 03 04 */
    private static final byte[] ZIP_MAGIC = {0x50, 0x4B, 0x03, 0x04, 0x00, 0x00, 0x00, 0x00};

    @TempDir
    Path tempDir;

    @Test
    void getTypeWithPngStreamReturnsPng() throws Exception {
        assertThat(FileTypeUtils.getType(stream(PNG_MAGIC))).isEqualTo("png");
    }

    @Test
    void getTypeWithJpegStreamReturnsJpg() throws Exception {
        assertThat(FileTypeUtils.getType(stream(JPEG_MAGIC))).isEqualTo("jpg");
    }

    @Test
    void getTypeWithPdfStreamReturnsPdf() throws Exception {
        assertThat(FileTypeUtils.getType(stream(PDF_MAGIC))).isEqualTo("pdf");
    }

    @Test
    void getTypeWithZipStreamReturnsZip() throws Exception {
        assertThat(FileTypeUtils.getType(stream(ZIP_MAGIC))).isEqualTo("zip");
    }

    @Test
    void getTypeWithNullStreamReturnsEmpty() throws Exception {
        assertThat(FileTypeUtils.getType((InputStream) null)).isEmpty();
    }

    @Test
    void getTypeWithShortStreamReturnsEmpty() throws Exception {
        // 只读到一个字节，bytesRead < 2 -> 空串
        assertThat(FileTypeUtils.getType(stream((byte) 0x89))).isEmpty();
    }

    @Test
    void getTypeWithUnknownMagicReturnsEmpty() throws Exception {
        // 全 0 字节，无法匹配任何魔数 -> 空串
        assertThat(FileTypeUtils.getType(stream(new byte[8]))).isEmpty();
    }

    @Test
    void getTypeWithPngFileReturnsPng() throws Exception {
        Path file = tempDir.resolve("image.png");
        Files.write(file, PNG_MAGIC);
        assertThat(FileTypeUtils.getType(file.toFile())).isEqualTo("png");
    }

    @Test
    void getTypeWithNonExistentFileReturnsEmpty() throws Exception {
        File file = tempDir.resolve("missing.bin").toFile();
        assertThat(FileTypeUtils.getType(file)).isEmpty();
    }

    @Test
    void getTypeWithNullFileReturnsEmpty() throws Exception {
        assertThat(FileTypeUtils.getType((File) null)).isEmpty();
    }

    @Test
    void getMimeTypeByExtension() {
        Map<String, String> expected = Map.ofEntries(
                Map.entry("jpg", "image/jpeg"),
                Map.entry("jpeg", "image/jpeg"),
                Map.entry("png", "image/png"),
                Map.entry("gif", "image/gif"),
                Map.entry("bmp", "image/bmp"),
                Map.entry("webp", "image/webp"),
                Map.entry("pdf", "application/pdf"),
                Map.entry("doc", "application/msword"),
                Map.entry("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
                Map.entry("xls", "application/vnd.ms-excel"),
                Map.entry("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                Map.entry("ppt", "application/vnd.ms-powerpoint"),
                Map.entry("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
                Map.entry("zip", "application/zip"),
                Map.entry("rar", "application/x-rar-compressed"),
                Map.entry("gz", "application/gzip"),
                Map.entry("mp3", "audio/mpeg"),
                Map.entry("flac", "audio/flac"),
                Map.entry("ogg", "audio/ogg"),
                Map.entry("mp4", "video/mp4"),
                Map.entry("avi", "video/x-msvideo"),
                Map.entry("txt", "text/plain"),
                Map.entry("html", "text/html"),
                Map.entry("htm", "text/html"),
                Map.entry("css", "text/css"),
                Map.entry("js", "application/javascript"),
                Map.entry("json", "application/json"),
                Map.entry("xml", "application/xml")
        );
        expected.forEach((extension, mime) ->
                assertThat(FileTypeUtils.getMimeType(extension)).isEqualTo(mime));
    }

    @Test
    void getMimeTypeWithUppercaseExtension() {
        // switch 内部先 toLowerCase
        assertThat(FileTypeUtils.getMimeType("JPG")).isEqualTo("image/jpeg");
    }

    @Test
    void getMimeTypeWithNullOrUnknownExtension() {
        assertThat(FileTypeUtils.getMimeType(null)).isEqualTo("application/octet-stream");
        assertThat(FileTypeUtils.getMimeType("")).isEqualTo("application/octet-stream");
        assertThat(FileTypeUtils.getMimeType("exe")).isEqualTo("application/octet-stream");
    }

    private static InputStream stream(byte... bytes) {
        return new ByteArrayInputStream(bytes);
    }
}
