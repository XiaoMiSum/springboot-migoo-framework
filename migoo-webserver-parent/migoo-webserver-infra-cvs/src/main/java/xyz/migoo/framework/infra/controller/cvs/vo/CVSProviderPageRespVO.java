package xyz.migoo.framework.infra.controller.cvs.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CVSProviderPageRespVO extends CVSProviderBaseVO {

    private Long id;
}
