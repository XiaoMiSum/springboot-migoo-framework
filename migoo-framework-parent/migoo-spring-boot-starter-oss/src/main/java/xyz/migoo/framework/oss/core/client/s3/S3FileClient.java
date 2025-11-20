package xyz.migoo.framework.oss.core.client.s3;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import io.minio.*;
import io.minio.http.Method;
import xyz.migoo.framework.oss.core.client.AbstractFileClient;

import java.io.ByteArrayInputStream;
import java.util.concurrent.TimeUnit;

/**
 * 基于 S3 协议的文件客户端，实现 MinIO、阿里云、腾讯云、七牛云、华为云、AWS S3 等云服务
 * <p>
 * S3 协议的客户端，采用 MinIO 提供的 io.minio 库
 * 支持标准 S3 协议的所有云存储服务
 *
 * @author xiaomi
 */
public class S3FileClient extends AbstractFileClient<S3FileClientConfig> {

    private MinioClient client;

    public S3FileClient(Long id, S3FileClientConfig config) {
        super(id, config);
    }

    @Override
    protected void doInit() {
        // 补全 domain
        if (StrUtil.isEmpty(config.getDomain())) {
            config.setDomain(buildDomain());
        }
        // 初始化客户端
        client = MinioClient.builder()
                .endpoint(buildEndpointURL()) // Endpoint URL
                .region(buildRegion()) // Region
                .credentials(config.getAccessKey(), config.getAccessSecret()) // 认证密钥
                .build();
    }

    /**
     * 基于 endpoint 构建调用云服务的 URL 地址
     *
     * @return URI 地址
     */
    private String buildEndpointURL() {
        // 如果已经是 http 或者 https，则不进行拼接.主要适配 MinIO
        if (HttpUtil.isHttp(config.getEndpoint()) || HttpUtil.isHttps(config.getEndpoint())) {
            return config.getEndpoint();
        }
        return StrUtil.format("https://{}", config.getEndpoint());
    }

    /**
     * 基于 bucket + endpoint 构建访问的 Domain 地址
     *
     * @return Domain 地址
     */
    private String buildDomain() {
        // 如果已经是 http 或者 https，则不进行拼接.主要适配 MinIO
        if (HttpUtil.isHttp(config.getEndpoint()) || HttpUtil.isHttps(config.getEndpoint())) {
            return StrUtil.format("{}/{}", config.getEndpoint(), config.getBucket());
        }
        // AWS S3 域名构建：支持虚拟主机风格和路径风格
        if (config.getEndpoint().contains(S3FileClientConfig.ENDPOINT_AWS)) {
            // 使用虚拟主机风格：https://bucket-name.s3.region.amazonaws.com
            // https://amzn-s3-demo-bucket.s3.us-west-2.amazonaws.com
            String region = buildRegion();
            return StrUtil.format("https://{}.s3.{}.{}", config.getBucket(), region, S3FileClientConfig.ENDPOINT_AWS);
        }
        // 阿里云、腾讯云、华为云都适合。七牛云比较特殊，必须有自定义域名
        return StrUtil.format("https://{}.{}", config.getBucket(), config.getEndpoint());
    }

    /**
     * 基于 bucket 构建 region 地区
     *
     * @return region 地区
     */
    private String buildRegion() {
        // AWS S3 必须有 region，从 endpoint 中提取
        if (config.getEndpoint().contains(S3FileClientConfig.ENDPOINT_AWS)) {
            // 从 s3.us-east-1.amazonaws.com 中提取 us-east-1
            if (config.getEndpoint().startsWith("s3.")) {
                String[] parts = config.getEndpoint().split("\\.");
                if (parts.length >= 2 && !parts[1].equals("amazonaws")) {
                    return parts[1]; // 返回区域，如 us-east-1
                }
            }
            // 如果是 s3.amazonaws.com，默认为 us-east-1
            return "us-east-1";
        }
        // 阿里云必须有 region，否则会报错
        if (config.getEndpoint().contains(S3FileClientConfig.ENDPOINT_ALIYUN)) {
            return StrUtil.subBefore(config.getEndpoint(), '.', false)
                    .replaceAll("-internal", "")// 去除内网 Endpoint 的后缀
                    .replaceAll("https://", "");
        }
        // 腾讯云必须有 region，否则会报错
        if (config.getEndpoint().contains(S3FileClientConfig.ENDPOINT_TENCENT)) {
            return StrUtil.subAfter(config.getEndpoint(), "cos.", false)
                    .replaceAll("." + S3FileClientConfig.ENDPOINT_TENCENT, ""); // 去除 Endpoint
        }
        return null;
    }

    @Override
    public String upload(byte[] content, String path, String type) throws Exception {
        // 执行上传
        client.putObject(PutObjectArgs.builder()
                .bucket(config.getBucket()) // bucket 必须传递
                .contentType(type)
                .object(path) // 相对路径作为 key
                .stream(new ByteArrayInputStream(content), content.length, -1) // 文件内容
                .build());
        // 拼接返回路径
        return config.getDomain() + "/" + path;
    }

    @Override
    public void delete(String path) throws Exception {
        client.removeObject(RemoveObjectArgs.builder()
                .bucket(config.getBucket()) // bucket 必须传递
                .object(path) // 相对路径作为 key
                .build());
    }

    @Override
    public byte[] getContent(String path) throws Exception {
        GetObjectResponse response = client.getObject(GetObjectArgs.builder()
                .bucket(config.getBucket()) // bucket 必须传递
                .object(path) // 相对路径作为 key
                .build());
        return IoUtil.readBytes(response);
    }

    @Override
    public FilePresignedUrlRespDTO getPresignedObjectUrl(String path) throws Exception {
        String uploadUrl = client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .method(Method.PUT)
                .bucket(config.getBucket())
                .object(path)
                .expiry(10, TimeUnit.MINUTES) // 过期时间（秒数）取值范围：1 秒 ~ 7 天
                .build()
        );
        return new FilePresignedUrlRespDTO(uploadUrl, config.getDomain() + "/" + path);
    }

}
