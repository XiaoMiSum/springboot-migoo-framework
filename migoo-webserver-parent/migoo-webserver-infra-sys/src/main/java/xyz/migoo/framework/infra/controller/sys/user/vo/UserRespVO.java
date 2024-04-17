package xyz.migoo.framework.infra.controller.sys.user.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserRespVO extends UserBaseVO {

    private Long id;

    private Integer status;

    private Date createTime;
}
