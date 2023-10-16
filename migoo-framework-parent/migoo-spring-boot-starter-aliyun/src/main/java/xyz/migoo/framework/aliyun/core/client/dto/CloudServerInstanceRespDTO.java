package xyz.migoo.framework.aliyun.core.client.dto;

import lombok.Builder;
import lombok.Data;
import xyz.migoo.framework.aliyun.core.enums.CloudServerType;

@Data
@Builder
public class CloudServerInstanceRespDTO {

    private String instanceId;

    private String hostname;

    private String createdTime;

    private String status;

    private String operateSystem;

    private String publicIpAddress;

    private String privateIpAddress;

    private String expiredTime;

    private CloudServerType type;

}
