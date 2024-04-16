package xyz.migoo.franework.infra.dal.dataobject;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseUser extends IdEnhanceDO {

    private String password;

    private String name;

    private String avatar;

    private Integer status;

    private String secretKey;

    private Integer bindAuthenticator;

    private Integer requiredVerifyAuthenticator;

}
