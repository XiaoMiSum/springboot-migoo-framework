package xyz.migoo.framework.cvs.core.property;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import xyz.migoo.framework.cvs.core.enums.CloudServerType;

@Data
@Validated
public class CloudServiceProperties {

    @NotEmpty(message = "accessKeyId 不能为空")
    private String accessKeyId;

    @NotEmpty(message = "accessKeySecret 不能为空")
    private String accessKeySecret;

    @NotEmpty(message = "region 不能为空")
    private String region;

    @NotNull(message = "serverType 不能为空")
    private CloudServerType serverType;


}
