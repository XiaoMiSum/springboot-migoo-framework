package xyz.migoo.framework.security.core;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
     * 部门编号
     */
    private Long teamId;
    /**
     * 复合名称：username(email)
     */
    private String compoundName;
    /**
     * 最后更新时间
     */
    private Date updateTime;
    /**
     * 登录名
     */
    private String loginName;
    /**
     * 用户名
     */
    private String name;
    /**
     * 用户名
     */
    private String email;
    /**
     * 密码
     */
    private String password;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 启用标识
     */
    private Boolean enabled;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getUsername() {
        return loginName;
    }

    public LoginUser setUsername(String username) {
        this.loginName = username;
        return this;
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @JsonIgnore
    public String getCompoundName() {
        return String.format("%s(%s)", StrUtil.isBlank(name) ? loginName : name, email);
    }
}
