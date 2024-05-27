package xyz.migoo.framework.infra.controller.cvs.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xyz.migoo.framework.common.pojo.PageParam;

@Data
@EqualsAndHashCode(callSuper = true)
public class CVSProviderPageQueryReqVO extends PageParam {

    private String provide;

    private String account;

    private Integer status;
}
