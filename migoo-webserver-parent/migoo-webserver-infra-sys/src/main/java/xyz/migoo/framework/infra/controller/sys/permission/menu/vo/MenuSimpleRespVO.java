package xyz.migoo.framework.infra.controller.sys.permission.menu.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuSimpleRespVO {

    private Long id;

    private String name;

    private Long parentId;

    private Integer type;

}