package xyz.migoo.framework.infra.controller;

import lombok.Getter;

import java.util.Objects;

import static xyz.migoo.framework.common.enums.CommonStatusEnum.DISABLE;

@Getter

public class CodeNameSimpleRespVO {
    private final String code;
    private final String name;
    private final Boolean disabled;

    public CodeNameSimpleRespVO(String code, String name, Integer status) {
        this.code = code;
        this.name = name;
        this.disabled = Objects.isNull(status) || Objects.equals(DISABLE.getStatus(), status);
    }


}
