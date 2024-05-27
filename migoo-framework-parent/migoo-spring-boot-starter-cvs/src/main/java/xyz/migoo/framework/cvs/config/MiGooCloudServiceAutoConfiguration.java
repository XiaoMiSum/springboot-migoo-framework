package xyz.migoo.framework.cvs.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import xyz.migoo.framework.cvs.core.client.CVSClientFactory;
import xyz.migoo.framework.cvs.core.client.CVSClientFactoryImpl;

/**
 * 短信配置类
 *
 * @author xioami
 */
@AutoConfiguration
public class MiGooCloudServiceAutoConfiguration {

    @Bean
    public CVSClientFactory cvsClientFactory() {
        return new CVSClientFactoryImpl();
    }

}
