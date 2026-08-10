package xyz.migoo.framework.security.core;

/**
 * 测试用 {@link AuthUserDetails} 具体实现
 * <p>
 * {@link AuthUserDetails} 是抽象类，各测试类通过本类构造测试用户对象
 */
public class TestAuthUser extends AuthUserDetails<TestAuthUser, Long> {

    public TestAuthUser() {
        super();
    }

    /**
     * 便捷工厂方法：构造一个带编号和登录名的用户
     */
    public static TestAuthUser of(Long id, String username) {
        return new TestAuthUser().setId(id).setUsername(username);
    }

    /**
     * 便捷工厂方法：构造一个完整用户
     */
    public static TestAuthUser full(Long id, String username, String password) {
        return new TestAuthUser()
                .setId(id)
                .setUsername(username)
                .setName(username)
                .setPassword(password)
                .setEnabled(true);
    }
}
