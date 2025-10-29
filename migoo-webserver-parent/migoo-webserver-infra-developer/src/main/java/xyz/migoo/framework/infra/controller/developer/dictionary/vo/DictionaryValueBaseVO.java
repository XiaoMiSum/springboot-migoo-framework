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

    @NotBlank(message = "{infra.dict.key.label.empty}")
    @Size(max = 10, message = "{infra.dict.key.label.size.max}")
    private String label;

    @NotBlank(message = "{infra.dict.key.value.empty}")
    @Size(max = 64, message = "{infra.dict.key.value.size.max}")
    private String value;

    @NotBlank(message = "{infra.dict.key.code.empty}")
    @Size(max = 64, message = "{infra.dict.code.label.size.max}")
    private String dictCode;

    private Integer status;

    private String colorType;

    @NotNull(message = "{common.sort.null}")
    private Integer sort;
}
