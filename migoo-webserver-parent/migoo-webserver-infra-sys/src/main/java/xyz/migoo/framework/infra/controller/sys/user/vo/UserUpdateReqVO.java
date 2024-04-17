package xyz.migoo.framework.infra.controller.sys.user.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserUpdateReqVO extends UserBaseVO {

    @NotNull(message = "用户编号不能为空")
    private Long id;

    private Integer status;
}
