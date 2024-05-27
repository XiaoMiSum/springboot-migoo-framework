package xyz.migoo.framework.infra.controller.cvs.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xyz.migoo.framework.common.pojo.PageParam;

@Data
@EqualsAndHashCode(callSuper = true)
public class CVSMachinePageQueryReqVO extends PageParam {

    private String hostname;

    private String account;

}
