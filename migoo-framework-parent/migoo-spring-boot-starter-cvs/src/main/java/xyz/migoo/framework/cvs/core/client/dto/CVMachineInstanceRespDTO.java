package xyz.migoo.framework.cvs.core.client.dto;

import lombok.Builder;
import lombok.Data;
import xyz.migoo.framework.cvs.core.enums.CVSMachineType;

import java.math.BigDecimal;

@Data
@Builder
public class CVMachineInstanceRespDTO {

    private String instanceId;

    private String hostname;

    private String createdTime;

    private InstanceStatus status;

    private String operateSystem;

    private String publicIpAddress;

    private String privateIpAddress;

    private String expiredTime;

    private BigDecimal price;

    private CVSMachineType machineType;

}
