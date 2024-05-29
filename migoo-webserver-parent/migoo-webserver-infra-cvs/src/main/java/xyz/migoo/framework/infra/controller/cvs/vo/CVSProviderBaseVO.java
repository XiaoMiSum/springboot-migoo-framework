package xyz.migoo.framework.infra.controller.cvs.vo;

import lombok.Data;

@Data
public class CVSProviderBaseVO {

    private String code;

    private String account;

    private String accessKeyId;

    private String accessKeySecret;

    private String region;

    private Integer status;
}
