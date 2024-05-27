package xyz.migoo.framework.sms.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import xyz.migoo.framework.sms.core.client.SmsClientFactory;
import xyz.migoo.framework.sms.core.client.SmsClientFactoryImpl;

/**
 * 短信配置类
 *
 * @author xioami
 */
@AutoConfiguration
public class MiGooSmsAutoConfiguration {

    @Bean
    public SmsClientFactory smsClientFactory() {
        return new SmsClientFactoryImpl();
    }

}
