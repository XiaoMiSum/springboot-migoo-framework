package xyz.migoo.framework.infra.controller.login.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthUserInfoRespVO {

    private String name;

    private String avatar;

    private String[] roles;

    private Set<String> permissions;

    private Boolean requiredBindAuthenticator;

    public AuthUserInfoRespVO setRoles(String... roles) {
        this.roles = roles;
        return this;
    }

}
