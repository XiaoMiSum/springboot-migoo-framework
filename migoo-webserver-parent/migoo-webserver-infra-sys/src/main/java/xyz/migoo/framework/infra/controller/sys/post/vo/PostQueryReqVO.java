package xyz.migoo.framework.infra.controller.sys.post.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xyz.migoo.framework.common.pojo.PageParam;

@Data
@EqualsAndHashCode(callSuper = true)
public class PostQueryReqVO extends PageParam {

    private String code;

    private String name;

    private Integer status;

}
