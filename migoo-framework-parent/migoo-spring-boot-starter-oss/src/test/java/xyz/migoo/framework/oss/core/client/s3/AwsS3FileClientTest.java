package xyz.migoo.framework.oss.core.client.s3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import xyz.migoo.framework.oss.core.client.FileClient;
import xyz.migoo.framework.oss.core.client.FileClientFactory;
import xyz.migoo.framework.oss.core.client.FileClientFactoryImpl;
import xyz.migoo.framework.oss.core.enums.FileStorageEnum;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AWS S3 文件客户端测试
 * 
 * 注意：这些测试需要真实的 AWS S3 凭证才能运行
 * 请在运行前配置正确的 AWS 凭证信息
 * 
 * @author xiaomi
 */
@DisplayName("AWS S3 文件上传测试")
class AwsS3FileClientTest {

    private FileClientFactory fileClientFactory;
    private static final Long CLIENT_ID = 1L;

    // AWS S3 配置 - 请替换为您的实际配置
    private static final String AWS_ENDPOINT = "s3.us-east-1.amazonaws.com";
    private static final String AWS_BUCKET = "your-bucket-name";
    private static final String AWS_ACCESS_KEY = "YOUR_AWS_ACCESS_KEY_ID";
    private static final String AWS_SECRET_KEY = "YOUR_AWS_SECRET_ACCESS_KEY";

    @BeforeEach
    void setUp() {
        fileClientFactory = new FileClientFactoryImpl();
        initAwsS3Client();
    }

    /**
     * 初始化 AWS S3 客户端
     */
    private void initAwsS3Client() {
        S3FileClientConfig config = new S3FileClientConfig();
        config.setEndpoint(AWS_ENDPOINT);
        config.setBucket(AWS_BUCKET);
        config.setAccessKey(AWS_ACCESS_KEY);
        config.setAccessSecret(AWS_SECRET_KEY);

        fileClientFactory.createOrUpdateFileClient(
            CLIENT_ID, 
            FileStorageEnum.S3.getStorage(), 
            config
        );
    }

    @Test
    @DisplayName("测试配置验证 - Endpoint 不能为空")
    void testConfigValidation_EndpointNotNull() {
        S3FileClientConfig config = new S3FileClientConfig();
        config.setBucket("test-bucket");
        config.setAccessKey("test-key");
        config.setAccessSecret("test-secret");

        // endpoint 为空时应该抛出异常
        assertThrows(Exception.class, () -> {
            new S3FileClient(100L, config).init();
        });
    }

    @Test
    @DisplayName("测试配置验证 - Bucket 不能为空")
    void testConfigValidation_BucketNotNull() {
        S3FileClientConfig config = new S3FileClientConfig();
        config.setEndpoint("s3.amazonaws.com");
        config.setAccessKey("test-key");
        config.setAccessSecret("test-secret");

        // bucket 为空时应该抛出异常
        assertThrows(Exception.class, () -> {
            new S3FileClient(100L, config).init();
        });
    }

    @Test
    @DisplayName("测试 AWS S3 区域识别 - us-east-1")
    void testAwsRegionExtraction_UsEast1() {
        S3FileClientConfig config = new S3FileClientConfig();
        config.setEndpoint("s3.us-east-1.amazonaws.com");
        config.setBucket("test-bucket");
        config.setAccessKey("key");
        config.setAccessSecret("secret");

        S3FileClient client = new S3FileClient(100L, config);
        client.init();

        // 验证 domain 构建正确
        assertNotNull(config.getDomain());
        assertTrue(config.getDomain().contains("us-east-1"));
        assertTrue(config.getDomain().contains("test-bucket"));
    }

    @Test
    @DisplayName("测试 AWS S3 区域识别 - ap-southeast-1")
    void testAwsRegionExtraction_ApSoutheast1() {
        S3FileClientConfig config = new S3FileClientConfig();
        config.setEndpoint("s3.ap-southeast-1.amazonaws.com");
        config.setBucket("test-bucket");
        config.setAccessKey("key");
        config.setAccessSecret("secret");

        S3FileClient client = new S3FileClient(100L, config);
        client.init();

        // 验证 domain 构建正确
        assertNotNull(config.getDomain());
        assertTrue(config.getDomain().contains("ap-southeast-1"));
    }

    @Test
    @DisplayName("测试 AWS S3 全球端点默认区域")
    void testAwsRegionExtraction_GlobalEndpoint() {
        S3FileClientConfig config = new S3FileClientConfig();
        config.setEndpoint("s3.amazonaws.com");
        config.setBucket("test-bucket");
        config.setAccessKey("key");
        config.setAccessSecret("secret");

        S3FileClient client = new S3FileClient(100L, config);
        client.init();

        // 验证使用默认区域 us-east-1
        assertNotNull(config.getDomain());
        assertTrue(config.getDomain().contains("us-east-1"));
    }

    @Test
    @DisplayName("测试域名构建 - 虚拟主机风格")
    void testDomainBuilding_VirtualHostedStyle() {
        S3FileClientConfig config = new S3FileClientConfig();
        config.setEndpoint("s3.us-west-2.amazonaws.com");
        config.setBucket("my-test-bucket");
        config.setAccessKey("key");
        config.setAccessSecret("secret");

        S3FileClient client = new S3FileClient(100L, config);
        client.init();

        // 验证 URL 格式：https://bucket-name.s3.region.amazonaws.com
        String expectedDomain = "https://my-test-bucket.s3.us-west-2.amazonaws.com";
        assertEquals(expectedDomain, config.getDomain());
    }

    @Test
    @DisplayName("测试自定义域名配置")
    void testCustomDomainConfiguration() {
        S3FileClientConfig config = new S3FileClientConfig();
        config.setEndpoint("s3.us-east-1.amazonaws.com");
        config.setBucket("test-bucket");
        config.setAccessKey("key");
        config.setAccessSecret("secret");
        config.setDomain("https://cdn.example.com");  // 自定义域名

        S3FileClient client = new S3FileClient(100L, config);
        client.init();

        // 验证自定义域名不会被覆盖
        assertEquals("https://cdn.example.com", config.getDomain());
    }

    @Test
    @Disabled("需要真实的 AWS S3 凭证才能运行")
    @DisplayName("集成测试 - 上传文本文件到 AWS S3")
    void testUploadTextFile() throws Exception {
        FileClient client = fileClientFactory.getFileClient(CLIENT_ID);
        assertNotNull(client, "FileClient 不应为 null");

        // 准备测试数据
        String testContent = "Hello, AWS S3! This is a test file uploaded at " + 
                           LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        byte[] content = testContent.getBytes(StandardCharsets.UTF_8);
        
        // 生成唯一文件名
        String fileName = "test/junit-test-" + System.currentTimeMillis() + ".txt";

        // 上传文件
        String fileUrl = client.upload(content, fileName, "text/plain");

        // 验证结果
        assertNotNull(fileUrl, "上传后应返回文件 URL");
        assertTrue(fileUrl.contains(AWS_BUCKET), "URL 应包含 bucket 名称");
        assertTrue(fileUrl.contains(fileName), "URL 应包含文件名");
        
        System.out.println("✅ 文件上传成功: " + fileUrl);

        // 清理：删除测试文件
        client.delete(fileName);
        System.out.println("✅ 测试文件已清理: " + fileName);
    }

    @Test
    @Disabled("需要真实的 AWS S3 凭证才能运行")
    @DisplayName("集成测试 - 上传并下载文件")
    void testUploadAndDownload() throws Exception {
        FileClient client = fileClientFactory.getFileClient(CLIENT_ID);

        // 上传文件
        String originalContent = "Test content for download: " + System.currentTimeMillis();
        byte[] uploadContent = originalContent.getBytes(StandardCharsets.UTF_8);
        String fileName = "test/download-test-" + System.currentTimeMillis() + ".txt";
        
        String fileUrl = client.upload(uploadContent, fileName, "text/plain");
        assertNotNull(fileUrl);
        System.out.println("✅ 文件上传成功: " + fileUrl);

        // 下载文件
        byte[] downloadContent = client.getContent(fileName);
        assertNotNull(downloadContent, "下载的内容不应为 null");
        
        String downloadedText = new String(downloadContent, StandardCharsets.UTF_8);
        assertEquals(originalContent, downloadedText, "下载的内容应与上传的内容一致");
        System.out.println("✅ 文件下载成功，内容匹配");

        // 清理
        client.delete(fileName);
        System.out.println("✅ 测试文件已清理");
    }

    @Test
    @Disabled("需要真实的 AWS S3 凭证才能运行")
    @DisplayName("集成测试 - 获取预签名 URL")
    void testGetPresignedUrl() throws Exception {
        FileClient client = fileClientFactory.getFileClient(CLIENT_ID);

        // 先上传一个文件
        String content = "Test file for presigned URL";
        String fileName = "test/presigned-test-" + System.currentTimeMillis() + ".txt";
        
        client.upload(content.getBytes(StandardCharsets.UTF_8), fileName, "text/plain");
        System.out.println("✅ 测试文件已上传");

        // 获取预签名 URL
        FilePresignedUrlRespDTO presignedUrl = client.getPresignedObjectUrl(fileName);
        
        assertNotNull(presignedUrl, "预签名 URL 响应不应为 null");
        assertNotNull(presignedUrl.getUploadUrl(), "上传 URL 不应为 null");
        assertNotNull(presignedUrl.getUrl(), "访问 URL 不应为 null");
        
        System.out.println("✅ 预签名上传 URL: " + presignedUrl.getUploadUrl());
        System.out.println("✅ 预签名访问 URL: " + presignedUrl.getUrl());

        // 清理
        client.delete(fileName);
        System.out.println("✅ 测试文件已清理");
    }

    @Test
    @Disabled("需要真实的 AWS S3 凭证才能运行")
    @DisplayName("集成测试 - 上传图片文件")
    void testUploadImageFile() throws Exception {
        FileClient client = fileClientFactory.getFileClient(CLIENT_ID);

        // 模拟图片内容（实际应该是真实的图片字节）
        byte[] fakeImageContent = "FAKE_IMAGE_CONTENT".getBytes(StandardCharsets.UTF_8);
        String fileName = "images/test-image-" + System.currentTimeMillis() + ".jpg";

        // 上传图片
        String fileUrl = client.upload(fakeImageContent, fileName, "image/jpeg");
        
        assertNotNull(fileUrl);
        assertTrue(fileUrl.contains(".jpg"), "URL 应包含文件扩展名");
        System.out.println("✅ 图片上传成功: " + fileUrl);

        // 清理
        client.delete(fileName);
        System.out.println("✅ 测试文件已清理");
    }

    @Test
    @Disabled("需要真实的 AWS S3 凭证才能运行")
    @DisplayName("集成测试 - 批量上传文件")
    void testBatchUpload() throws Exception {
        FileClient client = fileClientFactory.getFileClient(CLIENT_ID);

        String[] fileNames = new String[3];
        
        try {
            // 批量上传 3 个文件
            for (int i = 0; i < 3; i++) {
                String content = "Batch file " + i + " content";
                String fileName = "test/batch-" + System.currentTimeMillis() + "-" + i + ".txt";
                fileNames[i] = fileName;
                
                String fileUrl = client.upload(content.getBytes(StandardCharsets.UTF_8), fileName, "text/plain");
                assertNotNull(fileUrl);
                System.out.println("✅ 文件 " + i + " 上传成功: " + fileUrl);
            }
            
            System.out.println("✅ 批量上传完成");
            
        } finally {
            // 清理所有上传的文件
            for (String fileName : fileNames) {
                if (fileName != null) {
                    client.delete(fileName);
                    System.out.println("✅ 清理文件: " + fileName);
                }
            }
        }
    }

    @Test
    @DisplayName("测试 FileClient 获取")
    void testGetFileClient() {
        FileClient client = fileClientFactory.getFileClient(CLIENT_ID);
        assertNotNull(client, "应该能够获取到 FileClient 实例");
        assertEquals(CLIENT_ID, client.getId(), "Client ID 应该匹配");
    }

    @Test
    @DisplayName("测试不存在的 FileClient")
    void testGetNonExistentFileClient() {
        FileClient client = fileClientFactory.getFileClient(9999L);
        assertNull(client, "不存在的 Client ID 应该返回 null");
    }

    @Test
    @DisplayName("测试配置更新")
    void testConfigUpdate() {
        // 创建初始配置
        S3FileClientConfig config1 = new S3FileClientConfig();
        config1.setEndpoint("s3.us-east-1.amazonaws.com");
        config1.setBucket("bucket-1");
        config1.setAccessKey("key1");
        config1.setAccessSecret("secret1");
        
        fileClientFactory.createOrUpdateFileClient(2L, FileStorageEnum.S3.getStorage(), config1);
        
        FileClient client = fileClientFactory.getFileClient(2L);
        assertNotNull(client);

        // 更新配置
        S3FileClientConfig config2 = new S3FileClientConfig();
        config2.setEndpoint("s3.us-west-2.amazonaws.com");
        config2.setBucket("bucket-2");
        config2.setAccessKey("key2");
        config2.setAccessSecret("secret2");
        
        fileClientFactory.createOrUpdateFileClient(2L, FileStorageEnum.S3.getStorage(), config2);
        
        // 验证客户端仍然可用
        FileClient updatedClient = fileClientFactory.getFileClient(2L);
        assertNotNull(updatedClient);
        assertEquals(2L, updatedClient.getId());
    }
}
