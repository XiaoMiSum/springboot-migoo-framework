package xyz.migoo.framework.info.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({InfoProperties.class})
public class MiGooInfoAutoConfiguration {

}
