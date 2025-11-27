package xyz.migoo.framework.security.core.service.dto;

import lombok.Data;
import xyz.migoo.framework.security.core.AuthUserDetails;

import java.util.Date;

/**
 * Created at 2025/11/27 16:06
 */
@Data
public class Authenticated<T extends AuthUserDetails<T>> {

    private String accessToken;

    private Date accessTokenExpiresAt;

    private String refreshToken;

    private Date refreshTokenExpiresAt;

    private T user;


}
