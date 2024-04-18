package xyz.migoo.framework.infra.controller.login;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import xyz.migoo.framework.common.exception.util.ServiceExceptionUtil;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.common.util.collection.SetUtils;
import xyz.migoo.framework.infra.controller.login.vo.*;
import xyz.migoo.framework.infra.convert.AuthConvert;
import xyz.migoo.framework.infra.dal.dataobject.sys.Menu;
import xyz.migoo.framework.infra.dal.dataobject.sys.User;
import xyz.migoo.framework.infra.enums.MenuTypeEnum;
import xyz.migoo.framework.infra.service.login.CaptchaService;
import xyz.migoo.framework.infra.service.login.TokenService;
import xyz.migoo.framework.infra.service.sys.permission.PermissionService;
import xyz.migoo.framework.infra.service.sys.user.UserService;
import xyz.migoo.framework.security.config.SecurityProperties;
import xyz.migoo.framework.security.core.BaseUser;
import xyz.migoo.framework.security.core.LoginUser;
import xyz.migoo.framework.security.core.annotation.Authenticator;
import xyz.migoo.framework.security.core.annotation.CurrentUser;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static xyz.migoo.framework.common.enums.CommonStatusEnum.ENABLE;
import static xyz.migoo.framework.infra.enums.ErrorCodeConstants.AUTH_LOGIN_CAPTCHA_CODE_ERROR;
import static xyz.migoo.framework.infra.enums.ErrorCodeConstants.USER_PASSWORD_OLD_NEW;

@RestController
public class LoginController {

    public boolean enableCaptcha;
    public String title = "";
    @Resource
    private TokenService tokenService;
    @Resource
    private UserService userService;
    @Resource
    private PermissionService permissionService;
    @Resource
    private CaptchaService captchaService;
    @Resource
    private SecurityProperties securityProperties;

    @PostMapping("/login")
    public Result<AuthLoginRespVO> login(@RequestBody AuthLoginReqVO req) {
        if (enableCaptcha && !Objects.equals(req.getCode(), captchaService.getCaptchaCode(req.getUuid()))) {
            throw ServiceExceptionUtil.get(AUTH_LOGIN_CAPTCHA_CODE_ERROR);
        }
        captchaService.deleteCaptchaCode(req.getUuid());
        return Result.getSuccessful(tokenService.signIn(req));
    }

    @GetMapping("/captcha")
    public Result<CaptchaImageRespVO> getCaptchaImage() {
        return Result.getSuccessful(captchaService.getCaptchaImage());
    }

    @GetMapping("/user-info")
    public Result<?> getUserInfo(@CurrentUser LoginUser loginUser) {
        BaseUser<Long> user = userService.get(loginUser.getId());
        Set<Long> roleIds = permissionService.getUserRoleIds(user.getId(), SetUtils.asSet(ENABLE.getStatus()));
        List<Menu> menuList = permissionService.getRoleMenusFromCache(
                roleIds,
                SetUtils.asSet(MenuTypeEnum.DIR.getType(), MenuTypeEnum.MENU.getType(), MenuTypeEnum.BUTTON.getType()),
                SetUtils.asSet(ENABLE.getStatus()));
        return Result.getSuccessful(AuthConvert.INSTANCE.convert(user, menuList));
    }

    @GetMapping("user-menus")
    public Result<List<AuthMenuRespVO>> getMenus(@CurrentUser LoginUser loginUser) {
        Set<Long> roleIds = permissionService.getUserRoleIds(loginUser.getId(), SetUtils.asSet(ENABLE.getStatus()));
        // 获得用户拥有的菜单列表
        List<Menu> menuList = permissionService.getRoleMenusFromCache(
                roleIds,
                SetUtils.asSet(MenuTypeEnum.DIR.getType(), MenuTypeEnum.MENU.getType()),
                SetUtils.asSet(ENABLE.getStatus()));
        // 转换成 Tree 结构返回
        return Result.getSuccessful(AuthConvert.INSTANCE.convert(menuList));
    }

    @GetMapping("/authenticator")
    public Result<?> getAuthenticator(@RequestParam("_token") String token) {
        LoginUser user = tokenService.verifyTokenAndRefresh(token);
        Map<String, String> result = new HashMap<>(2);
        String content = String.format("otpauth://totp/%s@%s?secret=%s&issuer=%s", user.getUsername(), title, user.getSecurityCode(), user.getName());
        result.put("quickMark", QrCodeUtil.generateAsBase64(content, new QrConfig(), "png"));
        result.put("securityCode", user.getSecurityCode());
        return Result.getSuccessful(result);
    }

    @PostMapping("/authenticator")
    @Authenticator
    public Result<?> bindAuthenticator(@RequestParam("_token") String token) {
        LoginUser user = tokenService.verifyTokenAndRefresh(token);
        userService.update((User) new User().setBindAuthenticator(ENABLE.getStatus()).setId(user.getId()));
        return Result.getSuccessful();
    }

    @PostMapping("/password")
    @Authenticator
    public Result<?> updatePassword(@CurrentUser LoginUser user, @RequestBody PasswordVO password) {
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
