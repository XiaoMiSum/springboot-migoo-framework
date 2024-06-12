package xyz.migoo.framework.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.migoo.framework.common.enums.CommonStatus;

import java.util.List;

import static xyz.migoo.framework.common.enums.CommonStatus.enabled;


/**
 * @author xiaomi
 * Created at 2023/6/4 21:02
 */
@Getter
@AllArgsConstructor
public class Tree {

    private final Object id;
    private final String name;
    private final boolean disable;
    private final Object parentId;
    private final List<Tree> children;

    private Tree(Object id, String name) {
        this(id, name, enabled.status());
    }

    private Tree(Object id, String name, Integer status) {
        this(id, name, status, null);
    }

    private Tree(Object id, String name, Integer status, Object parentId) {
        this(id, name, CommonStatus.isDisabled(status), parentId, List.of());
    }

    public static Tree rootNode(Object id, String name) {
        return new Tree(id, name);
    }

    public static Tree rootNode(Object id, String name, Integer status) {
        return new Tree(id, name, status);
    }

    public Tree addChildren(Object id, String name) {
        return addChildren(id, name, enabled.status());
    }

    public Tree addChildren(Object id, String name, Integer status) {
        return addChildren(new Tree(id, name, status, this.id));
    }

    public Tree addChildren(Tree children) {
        this.children.add(children);
        return this;
    }


}
