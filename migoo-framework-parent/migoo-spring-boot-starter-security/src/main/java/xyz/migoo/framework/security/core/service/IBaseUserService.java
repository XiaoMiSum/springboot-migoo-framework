package xyz.migoo.framework.security.core.service;

import xyz.migoo.framework.security.core.BaseUser;
import xyz.migoo.framework.security.core.LoginUser;

public interface IBaseUserService {

    LoginUser toLoginUser(BaseUser<?> baseUser);
}
