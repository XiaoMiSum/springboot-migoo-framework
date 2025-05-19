package xyz.migoo.framework.infra.controller.developer.dictionary.vo;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DictionaryBaseVO {

    @NotEmpty(message = "字典名称不能为空")
    private String name;

    @NotEmpty(message = "字典编码不能为空")
    private String code;

    private Integer status;

    private String source;
}
