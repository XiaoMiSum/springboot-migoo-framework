package xyz.migoo.framework.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimpleData<T> {

    private T code;

    private String name;

    private boolean disable;
}