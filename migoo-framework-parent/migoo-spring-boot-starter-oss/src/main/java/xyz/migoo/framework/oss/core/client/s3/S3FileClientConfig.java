package xyz.migoo.framework.oss.core.client.s3;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.URL;
import xyz.migoo.framework.oss.core.client.FileClientConfig;

/**
 * S3 文件客户端的配置类
 *
 * @author xiaomi
 */
@Data
public class S3FileClientConfig implements FileClientConfig {

    public static final String ENDPOINT_QINIU = "qiniucs.com";
    public static final String ENDPOINT_ALIYUN = "aliyuncs.com";
    public static final String ENDPOINT_TENCENT = "myqcloud.com";
    public static final String ENDPOINT_AWS = "amazonaws.com";

    /**
     * 节点地址
     * 1. MinIO：<a href="https://www.iocoder.cn/Spring-Boot/MinIO">...</a> 。例如说，<a href="http://127.0.0.1:9000">...</a>
     * 2. 阿里云：https://help.aliyun.com/document_detail/31837.html
     * 3. 腾讯云：<a href="https://cloud.tencent.com/document/product/436/6224">...</a>
     * 4. 七牛云：<a href="https://developer.qiniu.com/kodo/4088/s3-access-domainname">...</a>
     * 5. 华为云：<a href="https://developer.huaweicloud.com/endpoint?OBS">...</a>
     * 6. AWS S3：https://docs.aws.amazon.com/general/latest/gr/s3.html 。例如说，s3.us-east-1.amazonaws.com
     */
    @NotNull(message = "endpoint 不能为空")
    private String endpoint;
    /**
     * 自定义域名
     * 1. MinIO：通过 Nginx 配置
     * 2. 阿里云：<a href="https://help.aliyun.com/document_detail/31836.html">...</a>
     * 3. 腾讯云：<a href="https://cloud.tencent.com/document/product/436/11142">...</a>
     * 4. 七牛云：<a href="https://developer.qiniu.com/kodo/8556/set-the-custom-source-domain-name">...</a>
     * 5. 华为云：<a href="https://support.huaweicloud.com/usermanual-obs/obs_03_0032.html">...</a>
     * 6. AWS S3：https://docs.aws.amazon.com/AmazonS3/latest/userguide/website-hosting-custom-domain-walkthrough.html
     */
    @URL(message = "domain 必须是 URL 格式")
    private String domain;
    /**
     * 存储 Bucket
     */
    @NotNull(message = "bucket 不能为空")
    private String bucket;

    /**
     * 访问 Key
     * 1. MinIO：<a href="https://www.iocoder.cn/Spring-Boot/MinIO">...</a>
     * 2. 阿里云：<a href="https://ram.console.aliyun.com/manage/ak">...</a>
     * 3. 腾讯云：<a href="https://console.cloud.tencent.com/cam/capi">...</a>
     * 4. 七牛云：<a href="https://portal.qiniu.com/user/key">...</a>
     * 5. 华为云：<a href="https://support.huaweicloud.com/qs-obs/obs_qs_0005.html">...</a>
     * 6. AWS S3：https://console.aws.amazon.com/iam/home#/security_credentials
     */
    @NotNull(message = "accessKey 不能为空")
    private String accessKey;
    /**
     * 访问 Secret
     */
    @NotNull(message = "accessSecret 不能为空")
    private String accessSecret;

    @SuppressWarnings("RedundantIfStatement")
    @AssertTrue(message = "domain 不能为空")
    @JsonIgnore
    public boolean isDomainValid() {
        // 如果是七牛，必须带有 domain
        if (StrUtil.contains(endpoint, ENDPOINT_QINIU) && StrUtil.isEmpty(domain)) {
            return false;
        }
        return true;
    }

}
