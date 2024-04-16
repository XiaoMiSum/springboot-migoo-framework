package xyz.migoo.framework.infra.controller.sys.post.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class PostRespVO extends PostBaseVO {

    private Long id;

    private Integer status;

    private Date createTime;

}
