package xyz.migoo.framework.cvs.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

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
