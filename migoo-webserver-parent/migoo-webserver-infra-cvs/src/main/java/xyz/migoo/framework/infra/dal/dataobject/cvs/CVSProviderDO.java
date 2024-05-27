package xyz.migoo.framework.infra.dal.dataobject.cvs;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import xyz.migoo.framework.mybatis.core.dataobject.BaseDO;

@TableName("infra_cvs_provider")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CVSProviderDO extends BaseDO<Long> {

    private String code;

    private String account;

    private String accessKeyId;

    private String accessKeySecret;

    private String region;

    private Integer status;
}
