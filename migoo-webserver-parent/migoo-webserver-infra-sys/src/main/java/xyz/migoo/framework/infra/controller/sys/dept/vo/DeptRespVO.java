package xyz.migoo.framework.infra.controller.sys.dept.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class DeptRespVO extends DeptBaseVO {

    private Long id;

    private Integer status;

    private Date createTime;
}
