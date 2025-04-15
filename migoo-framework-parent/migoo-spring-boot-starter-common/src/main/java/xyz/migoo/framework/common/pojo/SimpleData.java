package xyz.migoo.framework.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.migoo.framework.common.enums.CommonStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimpleData {

    private Object value;

    private String label;

    private boolean disabled;

    private Object extend;

    public SimpleData(Object value, String label) {
        this(value, label, false, null);
    }

    public SimpleData(Object value, String label, Integer status) {
        this(value, label, status, null);
    }

    public SimpleData(Object value, String label, boolean disabled) {
        this(value, label, disabled, null);
    }

    public SimpleData(Object value, String label, Integer status, Object extend) {
        this(value, label, CommonStatus.isDisabled(status), extend);
    }
}