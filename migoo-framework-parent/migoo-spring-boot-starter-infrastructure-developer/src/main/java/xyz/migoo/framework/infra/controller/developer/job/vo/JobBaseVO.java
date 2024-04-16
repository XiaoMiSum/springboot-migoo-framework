package xyz.migoo.franework.infra.controller.developer.job.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JobBaseVO {

    @NotNull(message = "任务名称不能为空")
    private String name;

    private String handlerParam;

    @NotNull(message = "CRON 表达式不能为空")
    private String cronExpression;

    @NotNull(message = "重试次数不能为空")
    private Integer retryCount;

    @NotNull(message = "重试间隔不能为空")
    private Integer retryInterval;

    private Integer monitorTimeout;

}