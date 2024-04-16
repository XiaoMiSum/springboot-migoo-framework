package xyz.migoo.framework.infra.controller.developer.job.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class JobRespVO extends JobBaseVO {

    private Long id;

    private Integer status;

    @NotNull(message = "处理器的名字不能为空")
    private String handlerName;

}