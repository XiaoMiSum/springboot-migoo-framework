package xyz.migoo.framework.infra.controller.sys.permission.role.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xyz.migoo.framework.common.pojo.PageParam;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoleQueryReqVO extends PageParam {

    private String name;

    private String code;

    private Integer status;

}