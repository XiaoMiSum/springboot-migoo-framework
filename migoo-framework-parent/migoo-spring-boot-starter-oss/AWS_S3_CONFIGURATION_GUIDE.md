# AWS S3 上传配置指南

## 📋 目录
- [功能概述](#功能概述)
- [快速开始](#快速开始)
- [配置说明](#配置说明)
- [使用示例](#使用示例)
- [高级特性](#高级特性)
- [常见问题](#常见问题)

---

## 功能概述

`migoo-spring-boot-starter-oss` 模块已经完整支持 AWS S3 对象存储服务，通过标准 S3 协议实现文件上传、下载、删除等功能。

### ✅ 已支持的功能
- ✅ 文件上传到 AWS S3
- ✅ 文件下载和内容获取
- ✅ 文件删除
- ✅ 预签名 URL 生成（临时访问）
- ✅ 多区域支持（所有 AWS 区域）
- ✅ 虚拟主机风格 URL
- ✅ 自定义域名支持

### 🌍 支持的云存储服务
- AWS S3
- MinIO
- 阿里云 OSS
- 腾讯云 COS
- 七牛云 Kodo
- 华为云 OBS
- 其他兼容 S3 协议的存储服务

---

## 快速开始

### 1️⃣ 添加依赖

在您的 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>xyz.migoo.springboot</groupId>
    <artifactId>migoo-spring-boot-starter-oss</artifactId>
    <version>1.3.2</version>
</dependency>
```

### 2️⃣ 获取 AWS 凭证

1. 登录 [AWS 控制台](https://console.aws.amazon.com/)
2. 进入 [IAM 安全凭证页面](https://console.aws.amazon.com/iam/home#/security_credentials)
3. 创建访问密钥（Access Key）
4. 记录 `Access Key ID` 和 `Secret Access Key`

### 3️⃣ 创建 S3 存储桶

1. 进入 [S3 控制台](https://s3.console.aws.amazon.com/s3/)
2. 点击"创建存储桶"
3. 输入存储桶名称（全局唯一）
4. 选择区域（如 `us-east-1`）
5. 配置访问权限

### 4️⃣ 配置客户端

```java
@Configuration
public class OssConfig {
    
    @Autowired
    private FileClientFactory fileClientFactory;
    
    @PostConstruct
    public void initAwsS3Client() {
        S3FileClientConfig config = new S3FileClientConfig();
        
        // AWS S3 配置
        config.setEndpoint("s3.us-east-1.amazonaws.com");  // 区域端点
        config.setBucket("my-bucket-name");                 // 存储桶名称
        config.setAccessKey("AKIAIOSFODNN7EXAMPLE");       // 访问密钥 ID
        config.setAccessSecret("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"); // 访问密钥
        
        // 创建客户端（配置 ID 为 1）
        fileClientFactory.createOrUpdateFileClient(1L, FileStorageEnum.S3.getStorage(), config);
    }
}
```

### 5️⃣ 上传文件

```java
@Service
public class FileService {
    
    @Autowired
    private FileClientFactory fileClientFactory;
    
    public String uploadFile(byte[] content, String fileName) throws Exception {
        FileClient client = fileClientFactory.getFileClient(1L);
        return client.upload(content, fileName, "application/octet-stream");
    }
}
```

---

## 配置说明

### AWS S3 区域端点

| 区域名称 | 区域代码 | 端点地址 |
|---------|---------|---------|
| 美国东部（弗吉尼亚北部） | us-east-1 | `s3.us-east-1.amazonaws.com` |
| 美国东部（俄亥俄） | us-east-2 | `s3.us-east-2.amazonaws.com` |
| 美国西部（加利福尼亚北部） | us-west-1 | `s3.us-west-1.amazonaws.com` |
| 美国西部（俄勒冈） | us-west-2 | `s3.us-west-2.amazonaws.com` |
| 亚太地区（东京） | ap-northeast-1 | `s3.ap-northeast-1.amazonaws.com` |
| 亚太地区（首尔） | ap-northeast-2 | `s3.ap-northeast-2.amazonaws.com` |
| 亚太地区（新加坡） | ap-southeast-1 | `s3.ap-southeast-1.amazonaws.com` |
| 亚太地区（悉尼） | ap-southeast-2 | `s3.ap-southeast-2.amazonaws.com` |
| 欧洲（法兰克福） | eu-central-1 | `s3.eu-central-1.amazonaws.com` |
| 欧洲（爱尔兰） | eu-west-1 | `s3.eu-west-1.amazonaws.com` |

更多区域请参考：[AWS 服务端点](https://docs.aws.amazon.com/general/latest/gr/s3.html)

### 配置参数详解

```java
S3FileClientConfig config = new S3FileClientConfig();

// 【必填】区域端点
config.setEndpoint("s3.us-east-1.amazonaws.com");

// 【必填】存储桶名称
config.setBucket("my-bucket-name");

// 【必填】访问密钥 ID
config.setAccessKey("AKIAIOSFODNN7EXAMPLE");

// 【必填】访问密钥
config.setAccessSecret("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");

// 【可选】自定义域名（如使用 CloudFront CDN）
config.setDomain("https://cdn.example.com");
```

---

## 使用示例

### 示例 1：基础文件上传

```java
@Service
public class FileUploadService {
    
    @Autowired
    private FileClientFactory fileClientFactory;
    
    /**
     * 上传文件到 S3
     */
    public String upload(MultipartFile file) throws Exception {
        FileClient client = fileClientFactory.getFileClient(1L);
        
        // 生成文件路径
        String fileName = "uploads/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        
        // 上传文件
        String url = client.upload(
            file.getBytes(), 
            fileName, 
            file.getContentType()
        );
        
        return url;
    }
}
```

### 示例 2：按日期组织文件

```java
public String uploadWithDateStructure(MultipartFile file) throws Exception {
    FileClient client = fileClientFactory.getFileClient(1L);
    
    // 生成日期路径：2024/11/20/filename.jpg
    LocalDate today = LocalDate.now();
    String datePath = String.format("%d/%02d/%02d/%s", 
        today.getYear(), 
        today.getMonthValue(), 
        today.getDayOfMonth(),
        file.getOriginalFilename()
    );
    
    return client.upload(file.getBytes(), datePath, file.getContentType());
}
```

### 示例 3：按用户分类存储

```java
public String uploadUserFile(Long userId, MultipartFile file) throws Exception {
    FileClient client = fileClientFactory.getFileClient(1L);
    
    // 用户专属目录：users/123/profile.jpg
    String userPath = String.format("users/%d/%s", userId, file.getOriginalFilename());
    
    return client.upload(file.getBytes(), userPath, file.getContentType());
}
```

### 示例 4：生成预签名 URL

```java
/**
 * 生成可临时访问的 URL（适用于私有文件）
 */
public String getTemporaryUrl(String filePath) throws Exception {
    FileClient client = fileClientFactory.getFileClient(1L);
    
    // 获取预签名 URL（10 分钟有效）
    FilePresignedUrlRespDTO presignedUrl = client.getPresignedObjectUrl(filePath);
    
    return presignedUrl.getUrl();
}
```

### 示例 5：下载文件

```java
/**
 * 从 S3 下载文件
 */
public byte[] downloadFile(String filePath) throws Exception {
    FileClient client = fileClientFactory.getFileClient(1L);
    return client.getContent(filePath);
}
```

### 示例 6：删除文件

```java
/**
 * 从 S3 删除文件
 */
public void deleteFile(String filePath) throws Exception {
    FileClient client = fileClientFactory.getFileClient(1L);
    client.delete(filePath);
}
```

### 示例 7：图片处理与上传

```java
/**
 * 压缩图片并上传
 */
public String uploadCompressedImage(MultipartFile image) throws Exception {
    // 1. 读取原图
    BufferedImage originalImage = ImageIO.read(image.getInputStream());
    
    // 2. 压缩图片（示例：缩放到 800x600）
    BufferedImage resized = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics = resized.createGraphics();
    graphics.drawImage(originalImage, 0, 0, 800, 600, null);
    graphics.dispose();
    
    // 3. 转换为字节数组
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(resized, "jpg", baos);
    byte[] compressedBytes = baos.toByteArray();
    
    // 4. 上传到 S3
    FileClient client = fileClientFactory.getFileClient(1L);
    String fileName = "images/" + System.currentTimeMillis() + ".jpg";
    return client.upload(compressedBytes, fileName, "image/jpeg");
}
```

---

## 高级特性

### 1. 多区域配置

支持同时配置多个 AWS 区域：

```java
@Configuration
public class MultiRegionOssConfig {
    
    @Autowired
    private FileClientFactory fileClientFactory;
    
    @PostConstruct
    public void init() {
        // 美国东部
        S3FileClientConfig usEast = new S3FileClientConfig();
        usEast.setEndpoint("s3.us-east-1.amazonaws.com");
        usEast.setBucket("my-bucket-us-east");
        usEast.setAccessKey("YOUR_ACCESS_KEY");
        usEast.setAccessSecret("YOUR_SECRET_KEY");
        fileClientFactory.createOrUpdateFileClient(10L, FileStorageEnum.S3.getStorage(), usEast);
        
        // 亚太新加坡
        S3FileClientConfig apSoutheast = new S3FileClientConfig();
        apSoutheast.setEndpoint("s3.ap-southeast-1.amazonaws.com");
        apSoutheast.setBucket("my-bucket-singapore");
        apSoutheast.setAccessKey("YOUR_ACCESS_KEY");
        apSoutheast.setAccessSecret("YOUR_SECRET_KEY");
        fileClientFactory.createOrUpdateFileClient(20L, FileStorageEnum.S3.getStorage(), apSoutheast);
    }
}

// 使用时指定配置 ID
FileClient usClient = fileClientFactory.getFileClient(10L);  // 美国
FileClient sgClient = fileClientFactory.getFileClient(20L);  // 新加坡
```

### 2. 自定义域名（CDN 加速）

配合 CloudFront 使用自定义域名：

```java
S3FileClientConfig config = new S3FileClientConfig();
config.setEndpoint("s3.us-east-1.amazonaws.com");
config.setBucket("my-bucket");
config.setAccessKey("YOUR_ACCESS_KEY");
config.setAccessSecret("YOUR_SECRET_KEY");

// 设置 CloudFront CDN 域名
config.setDomain("https://cdn.example.com");

// 上传后返回的 URL 将使用自定义域名
// 例如：https://cdn.example.com/path/to/file.jpg
```

### 3. 私有文件访问控制

```java
/**
 * 上传私有文件并生成临时访问链接
 */
public String uploadPrivateFile(MultipartFile file) throws Exception {
    FileClient client = fileClientFactory.getFileClient(1L);
    
    // 上传文件
    String filePath = "private/" + file.getOriginalFilename();
    client.upload(file.getBytes(), filePath, file.getContentType());
    
    // 生成临时访问链接（10 分钟有效）
    FilePresignedUrlRespDTO presignedUrl = client.getPresignedObjectUrl(filePath);
    
    return presignedUrl.getUrl();
}
```

### 4. 批量文件操作

```java
/**
 * 批量上传文件
 */
public List<String> batchUpload(List<MultipartFile> files) {
    FileClient client = fileClientFactory.getFileClient(1L);
    List<String> urls = new ArrayList<>();
    
    for (MultipartFile file : files) {
        try {
            String fileName = "batch/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String url = client.upload(file.getBytes(), fileName, file.getContentType());
            urls.add(url);
        } catch (Exception e) {
            System.err.println("上传失败: " + file.getOriginalFilename());
        }
    }
    
    return urls;
}
```

---

## 常见问题

### Q1: 如何获取 AWS 访问密钥？

**答：** 访问 [AWS IAM 安全凭证页面](https://console.aws.amazon.com/iam/home#/security_credentials)，创建新的访问密钥。

### Q2: 上传失败，提示 403 Forbidden

**答：** 请检查：
1. Access Key 和 Secret Key 是否正确
2. IAM 用户是否有 S3 写入权限
3. 存储桶策略是否允许上传

### Q3: 如何设置存储桶权限？

**答：** 在 S3 控制台中：
1. 选择存储桶 → 权限
2. 配置"存储桶策略"或"访问控制列表"
3. 示例策略（允许公共读取）：

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadGetObject",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::your-bucket-name/*"
    }
  ]
}
```

### Q4: 支持哪些文件格式？

**答：** 支持所有文件格式，包括但不限于：
- 图片：JPG, PNG, GIF, SVG, WebP
- 文档：PDF, DOCX, XLSX, PPTX
- 视频：MP4, AVI, MOV
- 音频：MP3, WAV, FLAC
- 压缩包：ZIP, RAR, 7Z

### Q5: 文件大小限制？

**答：** 
- 单个文件通过 `upload()` 方法最大支持 5GB
- AWS S3 单个对象最大 5TB（需使用分段上传）

### Q6: 如何实现文件续传？

**答：** 对于大文件，建议使用 AWS S3 的分段上传（Multipart Upload）功能。MinIO 客户端已经内置支持。

### Q7: 如何配置跨域（CORS）？

**答：** 在 S3 存储桶的 CORS 配置中添加：

```json
[
  {
    "AllowedHeaders": ["*"],
    "AllowedMethods": ["GET", "PUT", "POST", "DELETE"],
    "AllowedOrigins": ["*"],
    "ExposeHeaders": ["ETag"]
  }
]
```

### Q8: 费用如何计算？

**答：** AWS S3 费用主要包括：
- 存储费用（按 GB/月）
- 请求费用（PUT、GET 等）
- 数据传输费用（出站流量）

详见：[AWS S3 定价](https://aws.amazon.com/s3/pricing/)

### Q9: 如何提高上传速度？

**答：** 
1. 选择离用户最近的区域
2. 使用 S3 传输加速（Transfer Acceleration）
3. 使用 CloudFront CDN
4. 实现分段上传

### Q10: 支持阿里云 OSS 吗？

**答：** 是的！该模块同时支持：
- AWS S3
- 阿里云 OSS
- 腾讯云 COS
- MinIO
- 华为云 OBS
- 七牛云 Kodo

只需修改 `endpoint` 配置即可。

---

## 🔗 相关链接

- [AWS S3 官方文档](https://docs.aws.amazon.com/s3/)
- [AWS S3 快速入门](https://aws.amazon.com/s3/getting-started/)
- [MinIO Java Client SDK](https://min.io/docs/minio/linux/developers/java/minio-java.html)
- [项目 GitHub](https://github.com/XiaoMiSum/springboot-migoo-framework)

---

## 📝 更新日志

### v1.3.2 (2024-11-20)
- ✅ 完整支持 AWS S3 上传
- ✅ 支持 AWS 所有区域
- ✅ 支持虚拟主机风格 URL
- ✅ 优化区域自动识别
- ✅ 添加 AWS S3 配置文档

---

**如有问题，欢迎提交 Issue！** 🎉
