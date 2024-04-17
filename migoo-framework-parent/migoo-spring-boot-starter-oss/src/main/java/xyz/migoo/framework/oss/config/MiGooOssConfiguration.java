package xyz.migoo.framework.oss.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.migoo.framework.oss.core.client.FileClientFactory;
import xyz.migoo.framework.oss.core.client.FileClientFactoryImpl;

@Configuration(proxyBeanMethods = false)
public class MiGooOssConfiguration {

    @Bean
    public FileClientFactory fileClientFactory() {
        return new FileClientFactoryImpl();
    }
}
