package xyz.migoo.framework.cvs.core.property;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@AllArgsConstructor
@NoArgsConstructor
public class CVSClientProperties {

    @NotEmpty(message = "accessKeyId 不能为空")
    private String accessKeyId;

    @NotEmpty(message = "accessKeySecret 不能为空")
    private String accessKeySecret;

    @NotEmpty(message = "region 不能为空")
    private String region;

    @NotNull(message = "服务商 id 不能为空")
    private Long id;

    @NotNull(message = "服务商 code 不能为空")
    private String code;

}
