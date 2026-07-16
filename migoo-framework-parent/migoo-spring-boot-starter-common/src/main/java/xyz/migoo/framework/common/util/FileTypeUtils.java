package xyz.migoo.framework.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件类型检测工具类
 *
 * @author migoo
 * @since 1.3.16
 */
public final class FileTypeUtils {

    private static final Map<String, String> MAGIC_NUMBER_MAP = new HashMap<>();

    static {
        // 图片类型
        MAGIC_NUMBER_MAP.put("FFD8FF", "jpg");
        MAGIC_NUMBER_MAP.put("89504E47", "png");
        MAGIC_NUMBER_MAP.put("47494638", "gif");
        MAGIC_NUMBER_MAP.put("424D", "bmp");
        MAGIC_NUMBER_MAP.put("52494646", "webp");

        // 文档类型
        MAGIC_NUMBER_MAP.put("255044462D312E", "pdf");
        MAGIC_NUMBER_MAP.put("D0CF11E0A1B11AE1", "doc");
        MAGIC_NUMBER_MAP.put("504B0304", "zip");
        MAGIC_NUMBER_MAP.put("526172211A0700", "rar");
        MAGIC_NUMBER_MAP.put("1F8B08", "gz");

        // 音视频类型
        MAGIC_NUMBER_MAP.put("494433", "mp3");
        MAGIC_NUMBER_MAP.put("FFFB", "mp3");
        MAGIC_NUMBER_MAP.put("FFF3", "mp3");
        MAGIC_NUMBER_MAP.put("664C6143", "flac");
        MAGIC_NUMBER_MAP.put("4F676753", "ogg");
        MAGIC_NUMBER_MAP.put("52494646", "avi");

        // 压缩包
        MAGIC_NUMBER_MAP.put("504B030414000600", "docx");
        MAGIC_NUMBER_MAP.put("504B03041400060008", "xlsx");
        MAGIC_NUMBER_MAP.put("504B0304140006000800", "pptx");
    }

    private FileTypeUtils() {
    }

    /**
     * 获取文件类型
     *
     * @param file 文件
     * @return 文件扩展名，如 "jpg", "png" 等
     */
    public static String getType(File file) throws IOException {
        if (file == null || !file.exists()) {
            return "";
        }
        try (InputStream is = new FileInputStream(file)) {
            return getType(is);
        }
    }

    /**
     * 获取文件类型
     *
     * @param is 输入流
     * @return 文件扩展名
     */
    public static String getType(InputStream is) throws IOException {
        if (is == null) {
            return "";
        }
        byte[] header = new byte[8];
        int bytesRead = is.read(header);
        if (bytesRead < 2) {
            return "";
        }
        StringBuilder magicNumber = new StringBuilder();
        for (byte b : header) {
            magicNumber.append(String.format("%02X", b));
        }
        String magic = magicNumber.toString();
        for (Map.Entry<String, String> entry : MAGIC_NUMBER_MAP.entrySet()) {
            if (magic.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "";
    }

    /**
     * 根据文件扩展名获取 MIME 类型
     */
    public static String getMimeType(String extension) {
        if (extension == null || extension.isEmpty()) {
            return "application/octet-stream";
        }
        return switch (extension.toLowerCase()) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "bmp" -> "image/bmp";
            case "webp" -> "image/webp";
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt" -> "application/vnd.ms-powerpoint";
            case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "zip" -> "application/zip";
            case "rar" -> "application/x-rar-compressed";
            case "gz" -> "application/gzip";
            case "mp3" -> "audio/mpeg";
            case "flac" -> "audio/flac";
            case "ogg" -> "audio/ogg";
            case "mp4" -> "video/mp4";
            case "avi" -> "video/x-msvideo";
            case "txt" -> "text/plain";
            case "html", "htm" -> "text/html";
            case "css" -> "text/css";
            case "js" -> "application/javascript";
            case "json" -> "application/json";
            case "xml" -> "application/xml";
            default -> "application/octet-stream";
        };
    }
}
