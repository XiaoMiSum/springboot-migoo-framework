package xyz.migoo.framework.infra.controller.developer.dictionary.vo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DictionaryUpdateReqVO extends DictionaryBaseVO {

    @NotNull(message = "{infra.dict.id.empty}")
    private Long id;
}
