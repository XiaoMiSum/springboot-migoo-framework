package xyz.migoo.framework.security.core.service;

import xyz.migoo.framework.security.core.BaseUser;

public interface IBaseUserService<T, U> {

    T toLoginUser(BaseUser<U> baseUser);
}
