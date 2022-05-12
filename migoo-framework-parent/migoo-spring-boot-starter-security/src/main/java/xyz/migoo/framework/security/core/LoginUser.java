package xyz.migoo.framework.security.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;

/**
 * 登录用户信息，如果不够可以在自己项目中继承扩展该对象
 *
 * @author xiaomi
 * Created on 2021/11/20 11:58
 */
@Data
public class LoginUser implements UserDetails {

    /**
     * 用户编号
     */
    private Long id;
    /**
     * 最后更新时间
     */
    private Date updateTime;

    /**
     * 用户姓名
     */
    private String name;

    /**
     * 登录名
     */
    private String username;

    /**
     * 登录客户端
     */
    private Client client;

    /**
     * 密码
     */
    private String password;
    /**
     * 启用标识
     */
    private Boolean enabled;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return username;
    }

    public LoginUser setUsername(String username) {
        this.username = username;
        return this;
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public enum Client {
        /**
         * 客户端类型
         */
        MEMBER_CLIENT,
        ADMIN_CLIENT;

        public static boolean isAdminClient(Client client) {
            return Objects.equal(ADMIN_CLIENT, client);
        }
    }
}


