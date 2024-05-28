package xyz.migoo.framework.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static xyz.migoo.framework.common.enums.CommonStatusEnum.isDisabled;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimpleData<T> {

    private T value;

    private String label;

    private boolean disable;

    public SimpleData(T value, String label) {
        this(value, label, false);
    }

    public SimpleData(T value, String label, Integer status) {
        this(value, label, isDisabled(status));
    }
}