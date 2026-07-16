package xyz.migoo.framework.security.core.service.dto;

import lombok.Data;
import xyz.migoo.framework.security.core.AuthUserDetails;

import java.time.LocalDateTime;

/**
 * 登录/刷新 token 返回结果
 *
 * @author xiaomi
 */
@Data
public class LoginResult<T extends AuthUserDetails<T, ?>> {

    private String accessToken;

    private LocalDateTime accessExpiry;

    private String refreshToken;

    private LocalDateTime refreshExpiry;

    private T userInfo;
}
