package xyz.migoo.examples.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 安全登录示例入口
 *
 * <p>演示 migoo-spring-boot-starter-security 的 JWT 模式完整链路：
 * 登录换 token → 带 token 访问受保护接口 → 角色校验 → 未认证 401。</p>
 *
 * @author xiaomi
 */
@SpringBootApplication
public class ExampleSecurityApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleSecurityApplication.class, args);
    }
}
