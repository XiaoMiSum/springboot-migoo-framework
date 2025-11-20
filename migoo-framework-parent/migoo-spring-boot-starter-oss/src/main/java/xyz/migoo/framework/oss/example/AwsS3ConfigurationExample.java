package xyz.migoo.framework.oss.example;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import lombok.Data;
import xyz.migoo.framework.oss.core.client.FileClientFactory;
import xyz.migoo.framework.oss.core.client.s3.S3FileClientConfig;
import xyz.migoo.framework.oss.core.enums.FileStorageEnum;

/**
 * AWS S3 配置示例
 * 
 * 本示例展示如何通过 application.yml 配置 AWS S3
 * 
 * @author xiaomi
 */
public class AwsS3ConfigurationExample {

    /**
     * 方式一：通过 Java 代码配置
     */
    @Configuration
    public static class AwsS3JavaConfig {
        
        @Autowired
        private FileClientFactory fileClientFactory;

        @PostConstruct
        public void initAwsS3() {
            S3FileClientConfig config = new S3FileClientConfig();
            
            // AWS S3 配置
            config.setEndpoint("s3.us-east-1.amazonaws.com");
            config.setBucket("my-application-files");
            config.setAccessKey("AKIAIOSFODNN7EXAMPLE");
            config.setAccessSecret("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
            
            // 可选：配置自定义域名（如 CloudFront）
            // config.setDomain("https://cdn.example.com");
            
            fileClientFactory.createOrUpdateFileClient(1L, FileStorageEnum.S3.getStorage(), config);
        }
    }

    /**
     * 方式二：通过 application.yml 配置
     * 
     * 在 application.yml 中添加：
     * 
     * aws:
     *   s3:
     *     endpoint: s3.us-east-1.amazonaws.com
     *     bucket: my-application-files
     *     access-key: AKIAIOSFODNN7EXAMPLE
     *     access-secret: wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
     *     domain: https://cdn.example.com  # 可选
     */
    @Data
    @Component
    @ConfigurationProperties(prefix = "aws.s3")
    public static class AwsS3Properties {
        private String endpoint = "s3.us-east-1.amazonaws.com";
        private String bucket;
        private String accessKey;
        private String accessSecret;
        private String domain;
    }

    /**
     * 使用配置类初始化
     */
    @Configuration
    public static class AwsS3PropertiesConfig {
        
        @Autowired
        private FileClientFactory fileClientFactory;
        
        @Autowired
        private AwsS3Properties awsS3Properties;

        @PostConstruct
        public void init() {
            S3FileClientConfig config = new S3FileClientConfig();
            config.setEndpoint(awsS3Properties.getEndpoint());
            config.setBucket(awsS3Properties.getBucket());
            config.setAccessKey(awsS3Properties.getAccessKey());
            config.setAccessSecret(awsS3Properties.getAccessSecret());
            
            if (awsS3Properties.getDomain() != null) {
                config.setDomain(awsS3Properties.getDomain());
            }
            
            fileClientFactory.createOrUpdateFileClient(1L, FileStorageEnum.S3.getStorage(), config);
        }
    }

    /**
     * 方式三：多环境配置
     * 
     * application-dev.yml (开发环境):
     * aws:
     *   s3:
     *     endpoint: http://localhost:9000  # 本地 MinIO
     *     bucket: dev-bucket
     *     access-key: minioadmin
     *     access-secret: minioadmin
     * 
     * application-prod.yml (生产环境):
     * aws:
     *   s3:
     *     endpoint: s3.us-east-1.amazonaws.com  # AWS S3
     *     bucket: prod-bucket
     *     access-key: ${AWS_ACCESS_KEY}
     *     access-secret: ${AWS_SECRET_KEY}
     */

    /**
     * 方式四：多区域配置
     * 
     * application.yml:
     * aws:
     *   s3:
     *     regions:
     *       - id: 10
     *         name: us-east
     *         endpoint: s3.us-east-1.amazonaws.com
     *         bucket: bucket-us-east
     *         access-key: ${US_ACCESS_KEY}
     *         access-secret: ${US_SECRET_KEY}
     *       - id: 20
     *         name: ap-southeast
     *         endpoint: s3.ap-southeast-1.amazonaws.com
     *         bucket: bucket-singapore
     *         access-key: ${SG_ACCESS_KEY}
     *         access-secret: ${SG_SECRET_KEY}
     */
    @Data
    @Component
    @ConfigurationProperties(prefix = "aws.s3")
    public static class MultiRegionAwsS3Properties {
        private java.util.List<RegionConfig> regions;

        @Data
        public static class RegionConfig {
            private Long id;
            private String name;
            private String endpoint;
            private String bucket;
            private String accessKey;
            private String accessSecret;
            private String domain;
        }
    }

    /**
     * 多区域配置初始化
     */
    @Configuration
    public static class MultiRegionAwsS3Config {
        
        @Autowired
        private FileClientFactory fileClientFactory;
        
        @Autowired
        private MultiRegionAwsS3Properties multiRegionProperties;

        @PostConstruct
        public void init() {
            if (multiRegionProperties.getRegions() != null) {
                for (MultiRegionAwsS3Properties.RegionConfig region : multiRegionProperties.getRegions()) {
                    S3FileClientConfig config = new S3FileClientConfig();
                    config.setEndpoint(region.getEndpoint());
                    config.setBucket(region.getBucket());
                    config.setAccessKey(region.getAccessKey());
                    config.setAccessSecret(region.getAccessSecret());
                    
                    if (region.getDomain() != null) {
                        config.setDomain(region.getDomain());
                    }
                    
                    fileClientFactory.createOrUpdateFileClient(
                        region.getId(), 
                        FileStorageEnum.S3.getStorage(), 
                        config
                    );
                }
            }
        }
    }
}
