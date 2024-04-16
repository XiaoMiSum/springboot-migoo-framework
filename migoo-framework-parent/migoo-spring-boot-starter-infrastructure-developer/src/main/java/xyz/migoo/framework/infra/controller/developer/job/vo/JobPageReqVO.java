package xyz.migoo.franework.infra.controller.developer.job.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xyz.migoo.framework.common.pojo.PageParam;

@Data
@EqualsAndHashCode(callSuper = true)
public class JobPageReqVO extends PageParam {

    private String name;

    private Integer status;

    private String handlerName;
}
