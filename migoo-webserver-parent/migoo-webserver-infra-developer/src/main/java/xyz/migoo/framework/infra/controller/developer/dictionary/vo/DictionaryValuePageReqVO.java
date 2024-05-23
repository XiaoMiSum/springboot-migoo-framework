package xyz.migoo.framework.infra.controller.developer.dictionary.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xyz.migoo.framework.common.pojo.PageParam;

@Data
@EqualsAndHashCode(callSuper = true)
public class DictionaryValuePageReqVO extends PageParam {

    private String label;

    private String dictCode;

}
