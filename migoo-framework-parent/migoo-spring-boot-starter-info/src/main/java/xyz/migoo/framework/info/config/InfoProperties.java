package xyz.migoo.framework.info.config;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "migoo.framework")
@Validated
@Data
public class InfoProperties {

    /**
     * 框架基础包名
     * <p>设置固定值为 xyz.migoo.framework </p>
     */
    @NotEmpty(message = "packageName 值必须为 xyz.migoo.framework")
    private String packageName;

    /**
     * 版本号
     */
    @NotEmpty(message = "version 版本号不能为空")
    private String version = "1.1.1";

    /**
     * 业务包名
     */
    @NotEmpty(message = "bizPackageName 业务包名不能为空")
    private String bizPackageName;


}
