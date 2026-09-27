package xyz.migoo.examples.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import xyz.migoo.framework.security.core.AuthUserDetails;
import xyz.migoo.framework.security.core.authentication.UserDetailsBridge;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存版用户加载桥接（应用必须实现 {@link UserDetailsBridge}）
 *
 * <p>框架与用户存储解耦：登录时经 {@link #loadByUsername} 查用户，
 * 每次请求校验 token 时经 {@link #loadByUserId} 实时加载（账号禁用/权限变更立即生效）。
 * 示例用内存 Map 模拟数据库；生产环境替换为 MyBatis/JPA 查询即可，接口不变。</p>
 *
 * <p>撤销钩子（clean / revokeByUserId / isTokenRevoked / isUserRevoked）为可选实现，
 * 不实现则不做 token 主动失效；需要 Redis 黑名单时参见 docs/security.md「应用层对接」。</p>
 *
 * @author xiaomi
 */
@Component
public class InMemoryUserDetailsBridge implements UserDetailsBridge {

    private final Map<String, DemoUserDetails> users = new ConcurrentHashMap<>();

    public InMemoryUserDetailsBridge(PasswordEncoder passwordEncoder) {
        // 示例内置两个账号（密码经框架默认的 BCryptPasswordEncoder 编码，密码错误 → 401）
        users.put("admin", new DemoUserDetails()
                .setId(1L).setName("管理员").setUsername("admin")
                .setPassword(passwordEncoder.encode("demo123")).setEnabled(true)
                .setAuthorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        users.put("user", new DemoUserDetails()
                .setId(2L).setName("普通用户").setUsername("user")
                .setPassword(passwordEncoder.encode("demo123")).setEnabled(true)
                .setAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    @Override
    public AuthUserDetails<?, ?> loadByUsername(String username) {
        return users.get(username);
    }

    @Override
    public AuthUserDetails<?, ?> loadByUserId(String userId) {
        return users.values().stream()
                .filter(user -> userId.equals(String.valueOf(user.getId())))
                .findFirst()
                .orElse(null);
    }
}
