package xyz.migoo.framework.tomcat.config;

import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.lang.Thread.ofVirtual;
import static java.util.concurrent.Executors.newThreadPerTaskExecutor;

/**
 * Tomcat 配置
 */
@Configuration
public class TomcatConfiguration {

    @Bean
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
        // 创建 OfVirtual，指定虚拟线程名称的前缀，以及线程编号起始值
        return handler -> handler.setExecutor(newThreadPerTaskExecutor(ofVirtual().name("virtual-thread-", 1).factory()));
    }
}