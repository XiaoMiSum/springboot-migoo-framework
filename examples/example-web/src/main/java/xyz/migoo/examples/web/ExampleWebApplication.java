package xyz.migoo.examples.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 最小可用 Web 应用入口
 *
 * <p>演示 migoo-spring-boot-starter-web 的开箱能力：统一响应 {@code Result}、
 * 全局异常处理、CORS 四档来源限制（见 application.yml）。</p>
 *
 * @author xiaomi
 */
@SpringBootApplication
public class ExampleWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleWebApplication.class, args);
    }
}
