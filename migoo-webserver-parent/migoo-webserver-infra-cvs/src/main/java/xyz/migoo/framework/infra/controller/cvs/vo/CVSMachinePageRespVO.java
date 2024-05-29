package xyz.migoo.framework.infra.controller.cvs.vo;

import lombok.Data;
import xyz.migoo.framework.cvs.core.enums.CVSMachineType;

@Data
public class CVSMachinePageRespVO {

    private Long id;

    private CVSMachineType machineType;

    private String hostname;

    private String instanceId;

    private String operateSystem;

    private String status;

    private String expiredTime;
}
