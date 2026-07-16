package xyz.migoo.framework.security.core.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * TOTP 绑定信息（密钥 + otpauth URL）
 *
 * @author xiaomi
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode()
public class TotpBinding {

    /**
     * TOTP 密钥 (Base32 编码)
     */
    private String totpSecret;

    /**
     * otpauth:// URL，用于生成 QR 码
     */
    private String otpAuthUri;
}
