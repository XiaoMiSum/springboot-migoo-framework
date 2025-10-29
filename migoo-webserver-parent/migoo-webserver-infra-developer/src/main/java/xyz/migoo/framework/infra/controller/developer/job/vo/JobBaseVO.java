package xyz.migoo.framework.infra.controller.developer.job.vo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JobBaseVO {

    @NotNull(message = "{infra.job.name.empty}")
    @Size(max = 32, message = "{infra.job.name.size.max}")
    private String name;

    @NotEmpty(message = "{infra.job.handler.empty}")
    @Size(max = 64, message = "{infra.job.handler.size.max}")
    private String handlerName;

    private String handlerParam;

    @NotEmpty(message = "{infra.job.corn.expression.empty}")
    @Size(max = 32, message = "{infra.job.corn.expression.size.max}")
    private String cronExpression;

    @NotNull(message = "{infra.job.retry.count.empty}")
    private Integer retryCount;

    @NotNull(message = "{infra.job.retry.interval.empty}")
    private Integer retryInterval;

    private Integer monitorTimeout;

}