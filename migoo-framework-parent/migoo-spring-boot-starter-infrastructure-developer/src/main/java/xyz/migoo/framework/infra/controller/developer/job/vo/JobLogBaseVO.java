package xyz.migoo.framework.infra.controller.developer.job.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class JobLogBaseVO {

    @NotNull(message = "任务编号不能为空")
    private Long jobId;

    @NotNull(message = "处理器的名字不能为空")
    private String handlerName;

    private String handlerParam;

    @NotNull(message = "第几次执行不能为空")
    private Integer executeIndex;

    @NotNull(message = "开始执行时间不能为空")
    private Date beginTime;

    private Date endTime;

    private Integer duration;

    @NotNull(message = "任务状态不能为空")
    private Integer status;

    private String result;

}
