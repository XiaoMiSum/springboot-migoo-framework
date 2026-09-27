package xyz.migoo.framework.security.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码工具类
 * <p>
 * 提供密码加密和校验的静态方法。
 * <p>
 * <strong>装配说明</strong>：本类不带 {@code @Component}（框架不启用组件扫描，该注解不可达），
 * 由 {@code MiGooSecurityAutoConfiguration} 注册为 Bean 并注入 {@link PasswordEncoder}；
 * 若应用自行创建（如单测直接 {@code new}），以最后一次注入的编码器为准。
 *
 * @author xiaomi
 */
@Slf4j
public class PasswordUtils {

    private static volatile PasswordEncoder passwordEncoder;

    public PasswordUtils(PasswordEncoder passwordEncoder) {
        PasswordUtils.passwordEncoder = passwordEncoder;
    }

    public static String encode(String password) {
        PasswordEncoder encoder = passwordEncoder;
        if (encoder == null) {
            throw new IllegalStateException("[encode][PasswordEncoder 未初始化] "
                    + "请通过 MiGooSecurityAutoConfiguration 装配 PasswordUtils Bean，或自行 new PasswordUtils(encoder)");
        }
        return encoder.encode(password);
    }

    public static boolean verify(String ori, String hashed) {
        PasswordEncoder encoder = passwordEncoder;
        if (encoder == null) {
            log.warn("[verify][PasswordEncoder 未初始化，返回 false]");
            return false;
        }
        return encoder.matches(ori, hashed);
    }
}
