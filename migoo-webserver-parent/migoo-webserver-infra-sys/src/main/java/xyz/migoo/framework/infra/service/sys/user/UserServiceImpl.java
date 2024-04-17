package xyz.migoo.framework.infra.service.sys.user;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import xyz.migoo.framework.common.exception.util.ServiceExceptionUtil;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.infra.controller.login.vo.PasswordVO;
import xyz.migoo.framework.infra.controller.sys.user.vo.UserQueryReqVO;
import xyz.migoo.framework.infra.convert.AuthConvert;
import xyz.migoo.framework.infra.dal.dataobject.sys.User;
import xyz.migoo.framework.infra.dal.mapper.sys.UserMapper;
import xyz.migoo.framework.infra.enums.ErrorCodeConstants;
import xyz.migoo.framework.infra.util.PasswordUtils;
import xyz.migoo.framework.security.core.BaseUser;
import xyz.migoo.framework.security.core.LoginUser;
import xyz.migoo.framework.security.core.util.GoogleAuthenticator;

import java.util.List;
import java.util.Objects;

import static xyz.migoo.framework.common.enums.NumberConstants.N_0;
import static xyz.migoo.framework.infra.enums.BindAuthenticatorEnum.INIT;
import static xyz.migoo.framework.infra.enums.ErrorCodeConstants.USER_ORIGINAL_PASSWORD_UNCONFORMITY;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper mapper;
    @Value("${migoo.security.token-secret}")
    private String secret;

    @Override
    public PageResult<User> getPage(UserQueryReqVO req) {
        return mapper.selectPage(req);
    }

    @Override
    public List<User> get(Integer... status) {
        return mapper.selectList(Objects.isNull(status) || status.length < 1 ? null : status[0]);
    }

    @Override
    public User get(String phone) {
        User user = mapper.selectByPhone(phone);
        if (Objects.isNull(user)) {
            throw new UsernameNotFoundException(phone);
        }
        user.setSecretKey(SecureUtil.aes(secret.getBytes()).decryptStr(user.getSecretKey()));
        return user;
    }

    @Override
    public User get(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public void add(User user) {
        user.setSecretKey(SecureUtil.aes(secret.getBytes()).encryptHex(GoogleAuthenticator.generateSecretKey()));
        user.setPassword(PasswordUtils.encode(user.getPassword()));
        mapper.insert(user);
    }

    @Override
    public void update(User user) {
        if (StrUtil.isNotBlank(user.getPassword())) {
            user.setPassword(PasswordUtils.encode(user.getPassword()));
        }
        if (mapper.updateById(user) == N_0) {
            throw ServiceExceptionUtil.get(ErrorCodeConstants.USER_NOT_EXISTS);
        }
    }

    @Override
    public void remove(Long id) {
        if (mapper.deleteById(id) == N_0) {
            throw ServiceExceptionUtil.get(ErrorCodeConstants.USER_NOT_EXISTS);
        }
    }

    @Override
    public void verify(String phone) {
        if (Objects.nonNull(mapper.selectByPhone(phone))) {
            throw ServiceExceptionUtil.get(ErrorCodeConstants.USER_IS_EXISTS);
        }
    }

    @Override
    public void update(PasswordVO password) {
        User user = mapper.selectById(password.getId());
        if (!PasswordUtils.verify(password.getOldPassword(), user.getPassword())) {
            throw ServiceExceptionUtil.get(USER_ORIGINAL_PASSWORD_UNCONFORMITY);
        }
        user.setPassword(PasswordUtils.encode(password.getNewPassword()));
        mapper.updateById(user);
    }

    @Override
    public void resetAuthenticator(Long id) {
        mapper.updateById((User) new User()
                .setSecretKey(SecureUtil.aes(secret.getBytes()).encryptHex(GoogleAuthenticator.generateSecretKey()))
                .setBindAuthenticator(INIT.getNumber())
                .setId(id));
    }

    @Override
    public LoginUser toLoginUser(BaseUser<?> baseUser) {
        return AuthConvert.INSTANCE.convert((BaseUser<Long>) baseUser);
    }
}
