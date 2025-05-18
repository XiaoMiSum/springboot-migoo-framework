package xyz.migoo.framework.infra.controller.developer.job.vo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class JobUpdateReqVO extends JobBaseVO {

    @NotNull(message = "任务编号不能为空")
    private Long id;

    @NotEmpty(message = "处理器的名字不能为空")
    private String handlerName;

}