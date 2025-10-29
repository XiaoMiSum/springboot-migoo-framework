package xyz.migoo.framework.infra.controller.developer.dictionary.vo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DictionaryBaseVO {

    @NotEmpty(message = "{infra.dict.name.empty}")
    @Size(max = 32, message = "{infra.dict.name.size.max}")
    private String name;

    @NotEmpty(message = "{infra.dict.code.empty}")
    @Size(max = 64, message = "{infra.dict.code.size.max}")
    private String code;

    private Integer status;

    private String source;
}
