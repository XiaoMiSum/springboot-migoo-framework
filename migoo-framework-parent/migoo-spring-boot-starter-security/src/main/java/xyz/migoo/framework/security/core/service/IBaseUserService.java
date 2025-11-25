package xyz.migoo.framework.security.core.service;

import xyz.migoo.framework.security.core.AuthUserDetails;
import xyz.migoo.framework.security.core.BaseUser;

public interface IBaseUserService {

    AuthUserDetails toLoginUser(BaseUser<?> baseUser);
}
