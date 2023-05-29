package xyz.migoo.framework.aliyun.core;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CloudServer {

    private String instanceId;

    private String createTime;

    private String status;

    private String operateSystem;

    private String ipAddress;

    private String expiredTime;

    private BigDecimal price;

    private ServerType type;

}
