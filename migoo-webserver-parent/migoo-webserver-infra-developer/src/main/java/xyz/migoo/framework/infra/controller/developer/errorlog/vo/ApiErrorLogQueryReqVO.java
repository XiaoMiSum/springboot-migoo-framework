package xyz.migoo.framework.infra.controller.developer.errorlog.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xyz.migoo.framework.common.pojo.PageParam;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApiErrorLogQueryReqVO extends PageParam {

    private String applicationName;

    private Integer status;

}
