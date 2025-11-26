package xyz.migoo.framework.security.core;

import lombok.Data;

/**
 * @author xiaomi
 * Created at 2025/11/25 9:06
 */
@Data
public class MiGooUserDetails extends AuthUserDetails<MiGooUserDetails> {

    private Long id;

    @Override
    public Long getId() {
        return id;
    }
}
