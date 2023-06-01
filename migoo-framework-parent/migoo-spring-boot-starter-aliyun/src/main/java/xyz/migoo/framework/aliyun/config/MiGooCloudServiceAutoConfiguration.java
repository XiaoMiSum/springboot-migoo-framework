package xyz.migoo.framework.aliyun.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import xyz.migoo.framework.aliyun.core.client.CloudServiceClientFactory;
import xyz.migoo.framework.aliyun.core.client.impl.CloudServiceClientFactoryImpl;

/**
 * 短信配置类
 *
 * @author xioami
 */
@AutoConfiguration
public class MiGooCloudServiceAutoConfiguration {

    @Bean
    public CloudServiceClientFactory cloudServiceClientFactory() {
        return new CloudServiceClientFactoryImpl();
    }

}
