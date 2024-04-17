package xyz.migoo.framework.infra.controller.developer.job.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import xyz.migoo.framework.common.pojo.PageParam;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class JobLogPageReqVO extends PageParam {

    private Long jobId;

    private String handlerName;

    private String beginTime;

    private String endTime;

    private Integer status;

}
