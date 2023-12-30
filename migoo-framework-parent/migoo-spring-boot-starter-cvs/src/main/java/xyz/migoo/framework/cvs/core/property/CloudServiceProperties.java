package xyz.migoo.framework.cvs.core.property;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@AllArgsConstructor
@NoArgsConstructor
public class CloudServiceProperties {

    @NotEmpty(message = "accessKeyId 不能为空")
    private String accessKeyId;

    @NotEmpty(message = "accessKeySecret 不能为空")
    private String accessKeySecret;

    @NotEmpty(message = "region 不能为空")
    private String region;


}
