package xyz.migoo.framework.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimpleData<T> {

    private T value;

    private String label;

    private boolean disable;

    private List<SimpleData<T>> children;

    public SimpleData(T value, String label) {
        this.value = value;
        this.label = label;
    }

    public SimpleData(T value, String label, boolean disable) {
        this.value = value;
        this.label = label;
        this.disable = disable;
    }
}