package xyz.migoo.framework.infra.controller.login.vo;

import lombok.Getter;
import lombok.Setter;
import xyz.migoo.framework.security.core.LoginUser;

@Getter
@Setter
public class OnesLoginUser extends LoginUser {

    private Integer userType;

    private String memberNo;

    private Boolean requiredBindAuthenticator;
}
