package xyz.migoo.framework.security.core.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author xiaomi
 * Created on 2021/11/21 15:41
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode()
public class AuthenticatorDTO {

    private String securityCode;

    private String quickMark;
}
