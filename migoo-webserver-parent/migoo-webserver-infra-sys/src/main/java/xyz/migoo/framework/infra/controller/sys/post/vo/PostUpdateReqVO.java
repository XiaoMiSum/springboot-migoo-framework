package xyz.migoo.framework.infra.controller.sys.post.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PostUpdateReqVO extends PostBaseVO {

    @NotNull(message = "岗位编号不能为空")
    private Long id;

    private Integer status;

}
