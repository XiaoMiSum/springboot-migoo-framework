package xyz.migoo.framework.web.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.lang.Thread.ofVirtual;
import static java.util.concurrent.Executors.newThreadPerTaskExecutor;

@Configuration
@ConditionalOnClass(name = "org.apache.catalina.startup.Tomcat")
public class VirtualThreadConfiguration {

    @Bean
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
        return handler -> handler.setExecutor(
                newThreadPerTaskExecutor(ofVirtual().name("virtual-thread-", 1).factory()));
    }
}
