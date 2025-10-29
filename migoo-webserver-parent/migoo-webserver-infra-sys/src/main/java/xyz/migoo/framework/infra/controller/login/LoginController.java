package xyz.migoo.framework.infra.controller.login;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import xyz.migoo.framework.common.exception.util.ServiceExceptionUtil;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.common.util.collection.SetUtils;
import xyz.migoo.framework.infra.controller.login.vo.AuthLoginReqVO;
import xyz.migoo.framework.infra.controller.login.vo.AuthLoginRespVO;
import xyz.migoo.framework.infra.controller.login.vo.AuthMenuRespVO;
import xyz.migoo.framework.infra.controller.login.vo.PasswordVO;
import xyz.migoo.framework.infra.convert.AuthConvert;
import xyz.migoo.framework.infra.dal.dataobject.sys.ConfigurerDO;
import xyz.migoo.framework.infra.dal.dataobject.sys.Menu;
import xyz.migoo.framework.infra.dal.dataobject.sys.User;
import xyz.migoo.framework.infra.enums.MenuTypeEnum;
import xyz.migoo.framework.infra.service.login.TokenService;
import xyz.migoo.framework.infra.service.sys.configurer.ConfigurerService;
import xyz.migoo.framework.infra.service.sys.permission.PermissionService;
import xyz.migoo.framework.infra.service.sys.user.UserService;
import xyz.migoo.framework.security.config.SecurityProperties;
import xyz.migoo.framework.security.core.BaseUser;
import xyz.migoo.framework.security.core.LoginUser;
import xyz.migoo.framework.security.core.annotation.Authenticator;
import xyz.migoo.framework.security.core.annotation.CurrentUser;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static xyz.migoo.framework.common.enums.CommonStatus.enabled;
import static xyz.migoo.framework.infra.enums.SysErrorCodeConstants.USER_PASSWORD_OLD_NEW;

@RestController
public class LoginController {

    public String title = "";
    @Resource
    private TokenService tokenService;
    @Resource
    private UserService userService;
    @Resource
    private PermissionService permissionService;
    @Resource
    private SecurityProperties securityProperties;
    @Resource
    private ConfigurerService configurerService;

    @PostMapping("/login")
    public Result<AuthLoginRespVO> login(@Valid @RequestBody AuthLoginReqVO req) {
        return Result.getSuccessful(tokenService.signIn(req));
    }

    @GetMapping("/user-info")
    public Result<?> getUserInfo(@CurrentUser LoginUser loginUser) {
        BaseUser<Long> user = userService.get(loginUser.getId());
        Set<Long> roleIds = permissionService.getUserRoleIds(user.getId(), SetUtils.asSet(enabled.status()));
        List<Menu> menuList = permissionService.getRoleMenusFromCache(
                roleIds,
                SetUtils.asSet(MenuTypeEnum.DIR.getType(), MenuTypeEnum.MENU.getType(), MenuTypeEnum.BUTTON.getType()),
                SetUtils.asSet(enabled.status()));
        return Result.getSuccessful(AuthConvert.INSTANCE.convert(user, menuList));
    }

    @GetMapping("/configurer")
    public Result<?> getConfig() {
        Map<String, String> result = configurerService.getList().stream()
                .collect(Collectors.toMap(ConfigurerDO::getName, ConfigurerDO::getValue));
        this.title = result.get("title");
        result.put("kit", securityProperties.getPasswordSecret());
        return Result.getSuccessful(result);
    }

    @GetMapping("user-menus")
    public Result<List<AuthMenuRespVO>> getMenus(@CurrentUser LoginUser loginUser) {
        Set<Long> roleIds = permissionService.getUserRoleIds(loginUser.getId(), SetUtils.asSet(enabled.status()));
        // 获得用户拥有的菜单列表
        List<Menu> menuList = permissionService.getRoleMenusFromCache(
                roleIds,
                SetUtils.asSet(MenuTypeEnum.DIR.getType(), MenuTypeEnum.MENU.getType()),
                SetUtils.asSet(enabled.status()));
        // 转换成 Tree 结构返回
        return Result.getSuccessful(AuthConvert.INSTANCE.convert(menuList));
    }

    @GetMapping("/authenticator")
    public Result<?> getAuthenticator(@CurrentUser LoginUser user) {
        Map<String, String> result = new HashMap<>(2);
        String content = String.format("otpauth://totp/%s@%s?secret=%s&issuer=%s", user.getUsername(), user.getName(), user.getSecurityCode(), title);
        result.put("quickMark", QrCodeUtil.generateAsBase64(content, new QrConfig(), "png"));
        result.put("securityCode", user.getSecurityCode());
        return Result.getSuccessful(result);
    }

    @PostMapping("/authenticator")
    @Authenticator
    public Result<?> bindAuthenticator(@CurrentUser LoginUser user) {
        userService.update((User) new User().setBindAuthenticator(enabled.status()).setId(user.getId()));
        return Result.getSuccessful();
    }

    @PostMapping("/password")
    @Authenticator
    public Result<?> updatePassword(@CurrentUser LoginUser user, @Valid @RequestBody PasswordVO password) {
        if (Objects.equals(password.getNewPassword(), password.getOldPassword())) {
            throw ServiceExceptionUtil.get(USER_PASSWORD_OLD_NEW);
        }
        password.setNewPassword(SecureUtil.aes(securityProperties.getPasswordSecret().getBytes(StandardCharsets.UTF_8))
                .decryptStr(password.getNewPassword()));
        password.setOldPassword(SecureUtil.aes(securityProperties.getPasswordSecret().getBytes(StandardCharsets.UTF_8))
                .decryptStr(password.getOldPassword()));
        password.setId(user.getId());
        userService.update(password);
        return Result.getSuccessful();
    }
}
