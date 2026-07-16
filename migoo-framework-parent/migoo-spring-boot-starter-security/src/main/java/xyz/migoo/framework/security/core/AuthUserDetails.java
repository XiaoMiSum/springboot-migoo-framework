package xyz.migoo.framework.security.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
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
public abstract class AuthUserDetails<Sub extends AuthUserDetails<Sub, ID>, ID> implements UserDetails {
    /**
     * 用户编号
     */
    private ID id;
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

    private Collection<? extends GrantedAuthority> authorities;

    private String totpSecret;

    private boolean twoFactorEnabled;

    private boolean twoFactorBound;

    private Map<String, Object> attrs;


    public AuthUserDetails() {
    }

    public Sub setId(ID id) {
        this.id = id;
        return (Sub) this;
    }

    public Sub setName(String name) {
        this.name = name;
        return (Sub) this;
    }

    public Sub setAttrs(Map<String, Object> attrs) {
        this.attrs = attrs;
        return (Sub) this;
    }

    @Override
    @JsonIgnore
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities != null ? authorities : Collections.emptyList();
    }

    public Sub setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
        return (Sub) this;
    }

    public Sub setTotpSecret(String totpSecret) {
        this.totpSecret = totpSecret;
        return (Sub) this;
    }

    public Sub setTwoFactorEnabled(boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
        return (Sub) this;
    }

    public Sub setTwoFactorBound(boolean twoFactorBound) {
        this.twoFactorBound = twoFactorBound;
        return (Sub) this;
    }

    @Override
    @NonNull
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


