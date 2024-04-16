package xyz.migoo.franework.infra.controller.developer.errorlog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorLogRespVO {

    private Long id;

    private String applicationName;

    private String requestMethod;

    private String requestUrl;

    private String requestParams;

    private String userIp;

    private Date exceptionTime;

    private String exceptionName;

    private String exceptionClassName;

    private String exceptionFileName;

    private String exceptionMethodName;

    private Integer exceptionLineNumber;

    private String exceptionStackTrace;

    private String exceptionRootCauseMessage;

    private String exceptionMessage;

    private Integer status;
}