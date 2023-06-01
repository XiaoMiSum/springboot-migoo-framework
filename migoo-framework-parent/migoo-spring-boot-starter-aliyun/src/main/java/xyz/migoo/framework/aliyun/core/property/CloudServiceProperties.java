package xyz.migoo.framework.aliyun.core.property;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import xyz.migoo.framework.aliyun.core.enums.CloudServerType;

@Data
@Validated
public class CloudServiceProperties {

    @NotEmpty(message = "accessKeyId 不能为空")
    private String accessKeyId;

    @NotEmpty(message = "accessKeySecret 不能为空")
    private String accessKeySecret;

    @NotEmpty(message = "endpoint 不能为空")
    private String endpoint;

    @NotNull(message = "serverType 不能为空")
    private CloudServerType serverType;


}
