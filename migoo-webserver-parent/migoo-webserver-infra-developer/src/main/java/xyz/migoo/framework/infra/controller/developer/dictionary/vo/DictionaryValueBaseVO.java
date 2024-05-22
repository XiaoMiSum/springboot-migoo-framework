package xyz.migoo.framework.infra.controller.developer.dictionary.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DictionaryValueBaseVO {

    @NotBlank(message = "字典标签不能为空")
    @Size(max = 100, message = "字典标签长度不能超过100个字符")
    private String label;

    @NotBlank(message = "字典键值不能为空")
    @Size(max = 100, message = "字典键值长度不能超过100个字符")
    private String value;

    @NotBlank(message = "字典Key不能为空")
    @Size(max = 100, message = "字典类型长度不能超过100个字符")
    private String dictKey;

    private Integer status;

    private String colorType;

    @NotNull(message = "显示顺序不能为空")
    private Integer sort;
}
