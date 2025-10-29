package xyz.migoo.framework.infra.controller.developer.dictionary.vo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DictionaryValueUpdateReqVO extends DictionaryValueBaseVO {

    @NotNull(message = "{infra.dict.key.id.empty}")
    private Long id;
}
