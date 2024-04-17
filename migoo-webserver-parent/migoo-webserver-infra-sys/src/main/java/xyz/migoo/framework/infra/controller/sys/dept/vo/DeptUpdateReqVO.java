package xyz.migoo.framework.infra.controller.sys.dept.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DeptUpdateReqVO extends DeptBaseVO {

    @NotNull(message = "部门编号不能为空")
    private Long id;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
