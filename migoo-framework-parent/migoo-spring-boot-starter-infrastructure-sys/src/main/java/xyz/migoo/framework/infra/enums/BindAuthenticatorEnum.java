package xyz.migoo.framework.infra.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BindAuthenticatorEnum {

    INIT(0),
    IS_BIND(1);

    private final Integer number;
}
