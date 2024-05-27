package xyz.migoo.framework.infra.controller.cvs.vo;

import lombok.Data;

@Data
public class CVSProviderBaseVO {

    private String provide;

    private String account;

    private String token;

    private String secret;

    private String regionId;

    private Integer status;
}
