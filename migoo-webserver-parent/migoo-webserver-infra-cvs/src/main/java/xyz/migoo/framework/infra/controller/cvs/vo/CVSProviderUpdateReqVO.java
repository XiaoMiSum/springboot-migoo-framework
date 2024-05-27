package xyz.migoo.framework.infra.controller.cvs.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CVSProviderUpdateReqVO extends CVSProviderBaseVO {

    private Long id;
}
