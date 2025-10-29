package xyz.migoo.framework.infra.controller.developer.errorlog.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorLogUpdateVO {

    @NotBlank(message = "{infra.error.log.id.not.empty}")
    private Long id;
    
    @NotBlank(message = "{infra.error.log.status.not.empty}")
    private Integer status;
}
