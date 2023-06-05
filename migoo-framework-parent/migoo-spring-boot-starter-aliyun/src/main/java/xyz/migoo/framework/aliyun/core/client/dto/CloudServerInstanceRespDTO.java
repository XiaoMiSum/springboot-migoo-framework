package xyz.migoo.framework.aliyun.core.client.dto;

import lombok.Builder;
import lombok.Data;
import xyz.migoo.framework.aliyun.core.enums.CloudServerType;

@Data
@Builder
public class CloudServerInstanceRespDTO {

    private String instanceId;

    private String createdTime;

    private String status;

    private String operateSystem;

    private String ipAddress;

    private String expiredTime;

    private CloudServerType type;

}
