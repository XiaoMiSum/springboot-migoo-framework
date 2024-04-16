package xyz.migoo.franework.infra.controller.developer.errorlog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorLogUpdateVO {

    private Long id;

    private Integer status;
}
