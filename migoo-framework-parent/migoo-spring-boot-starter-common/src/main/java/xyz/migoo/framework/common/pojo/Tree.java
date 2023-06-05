package xyz.migoo.framework.common.pojo;

import lombok.Data;

import java.util.List;

/**
 * @author xiaomi
 * Created at 2023/6/4 21:02
 */
@Data
public class Tree<T> {

    private T id;

    private String name;

    private Integer status;

    private T parentId;

    List<Tree<T>> children;
}
