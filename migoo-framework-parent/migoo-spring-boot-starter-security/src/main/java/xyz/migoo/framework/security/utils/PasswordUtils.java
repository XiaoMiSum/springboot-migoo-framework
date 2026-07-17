package xyz.migoo.framework.security.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码工具类
 * <p>
 * 提供密码加密和校验的静态方法
 *
 * @author xiaomi
 */
@Component
public class PasswordUtils {

    private static PasswordEncoder passwordEncoder;

    @Autowired
    public PasswordUtils(PasswordEncoder passwordEncoder) {
        PasswordUtils.passwordEncoder = passwordEncoder;
    }

    public static String encode(String password) {
        return passwordEncoder.encode(password);
    }

    public static boolean verify(String ori, String hashed) {
        return passwordEncoder.matches(ori, hashed);
    }
}
