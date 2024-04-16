package xyz.migoo.franework.infra.controller.developer.errorlog.vo;

import lombok.Data;

import java.util.Date;

@Data
public class ApiErrorLogPageRespVO {

    private Long id;

    private String applicationName;

    private Integer status;

    private String userIp;

    private Date exceptionTime;
}
