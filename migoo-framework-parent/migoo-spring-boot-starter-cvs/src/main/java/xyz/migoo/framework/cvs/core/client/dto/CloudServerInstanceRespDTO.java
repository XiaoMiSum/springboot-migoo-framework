package xyz.migoo.framework.cvs.core.client.dto;

import lombok.Builder;
import lombok.Data;
import xyz.migoo.framework.cvs.core.enums.CloudServerType;

import java.math.BigDecimal;

@Data
@Builder
public class CloudServerInstanceRespDTO {

    private String instanceId;

    private String hostname;

    private String createdTime;

    private InstanceStatus status;

    private String operateSystem;

    private String publicIpAddress;

    private String privateIpAddress;

    private String expiredTime;

    private BigDecimal price;

    private CloudServerType type;

}
