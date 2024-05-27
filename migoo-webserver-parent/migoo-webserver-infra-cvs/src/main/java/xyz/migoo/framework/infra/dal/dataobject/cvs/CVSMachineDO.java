package xyz.migoo.framework.infra.dal.dataobject.cvs;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import xyz.migoo.framework.cvs.core.enums.CVSMachineType;
import xyz.migoo.framework.mybatis.core.dataobject.BaseDO;

import java.math.BigDecimal;

@TableName(value = "infra_cvs_machine", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CVSMachineDO extends BaseDO<Long> {

    private String account;

    private String hostname;

    private String instanceId;

    private String operateSystem;

    private String publicIpAddress;

    private String privateIpAddress;

    private String status;

    private String expiredTime;

    private BigDecimal price;

    private CVSMachineType machineType;
}
