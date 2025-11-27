package xyz.migoo.framework.infra.service.sys.user;

import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.infra.controller.login.vo.PasswordVO;
import xyz.migoo.framework.infra.controller.sys.user.vo.UserQueryReqVO;
import xyz.migoo.framework.infra.convert.AuthConvert;
import xyz.migoo.framework.infra.dal.dataobject.sys.User;
import xyz.migoo.framework.security.core.BaseUser;
import xyz.migoo.framework.security.core.MiGooUserDetails;
import xyz.migoo.framework.security.core.service.IBaseUserService;

import java.util.List;

public interface UserService extends IBaseUserService<MiGooUserDetails, Long> {

    @Override
    default MiGooUserDetails toLoginUser(BaseUser<Long> baseUser) {
        return AuthConvert.INSTANCE.convert(baseUser);
    }

    PageResult<User> getPage(UserQueryReqVO req);

    List<User> get(Integer... status);

    User get(String username);

    User get(Long id);

    void add(User user);

    void update(User user);

    void remove(Long id);

    void verify(String phone);


    void update(PasswordVO password);


    void resetAuthenticator(Long id);
}
