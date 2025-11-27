package xyz.migoo.framework.security.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Map;

/**
 * 登录用户信息，如果不够可以在自己项目中继承扩展该对象
 *
 * @author xiaomi
 * Created on 2021/11/20 11:58
 */
@Getter
@AllArgsConstructor
@SuppressWarnings("unchecked")
public abstract class AuthUserDetails<Sub extends AuthUserDetails<Sub>> implements UserDetails {
    /**
     * 用户姓名
     */
    private String name;

    /**
     * 登录名
     */
    private String username;

    /**
     * 密码
     */
    private String password;
    /**
     * 启用标识
     */
    private boolean enabled;

    private String securityCode;

    private boolean requiredVerifyAuthenticator;

    private boolean requiredBindAuthenticator;

    private Map<String, Object> attrs;

    public AuthUserDetails() {
    }

    public abstract Object getId();

    public Sub setName(String name) {
        this.name = name;
        return (Sub) this;
    }

    public Sub setSecurityCode(String securityCode) {
        this.securityCode = securityCode;
        return (Sub) this;
    }

    public Sub setRequiredVerifyAuthenticator(boolean requiredVerifyAuthenticator) {
        this.requiredVerifyAuthenticator = requiredVerifyAuthenticator;
        return (Sub) this;
    }

    public Sub setRequiredBindAuthenticator(boolean requiredBindAuthenticator) {
        this.requiredBindAuthenticator = requiredBindAuthenticator;
        return (Sub) this;
    }

    public Sub etAttrs(Map<String, Object> attrs) {
        this.attrs = attrs;
        return (Sub) this;
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public Sub setUsername(String username) {
        this.username = username;
        return (Sub) this;
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return password;
    }

    public Sub setPassword(String password) {
        this.password = password;
        return (Sub) this;
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

    public Sub setEnabled(boolean enabled) {
        this.enabled = enabled;
        return (Sub) this;
    }
}


