package xyz.migoo.framework.infra.controller.sys.post.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public abstract class PostBaseVO {

    @NotBlank(message = "{infra.post.name.empty}")
    @Size(max = 50, message = "{infra.post.name.size.max}")
    private String name;

    @NotBlank(message = "{infra.post.code.empty}")
    @Size(max = 64, message = "{infra.post.code.size.max}")
    private String code;

    @NotNull(message = "{common.sort.null}")
    private Integer sort;

    private String remark;
}
