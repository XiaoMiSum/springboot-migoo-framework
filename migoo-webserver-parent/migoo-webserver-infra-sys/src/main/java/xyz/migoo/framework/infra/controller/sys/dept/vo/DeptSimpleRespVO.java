package xyz.migoo.framework.infra.controller.sys.dept.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeptSimpleRespVO {

    private Long id;

    private String name;

    private Long parentId;

}