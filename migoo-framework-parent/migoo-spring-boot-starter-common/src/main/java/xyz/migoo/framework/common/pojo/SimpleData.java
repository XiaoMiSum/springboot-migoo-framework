package xyz.migoo.framework.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.migoo.framework.common.enums.CommonStatusEnum;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimpleData {

    private Object value;

    private String label;

    private boolean disabled;

    public SimpleData(Object value, String label) {
        this(value, label, false);
    }

    public SimpleData(Object value, String label, Integer status) {
        this(value, label, CommonStatusEnum.isDisabled(status));
    }
}