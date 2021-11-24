package xyz.migoo.framework.common.pojo;

import java.io.Serializable;

/**
 * 排序字段 DTO
 */
public class SortField implements Serializable {

    /**
     * 顺序 - 升序
     */
    public static final String ORDER_ASC = "asc";
    /**
     * 顺序 - 降序
     */
    public static final String ORDER_DESC = "desc";

    /**
     * 字段
     */
    private String field;
    /**
     * 顺序
     */
    private String order;

    // 空构造方法，解决反序列化
    public SortField() {
    }

    public SortField(String field, String order) {
        this.field = field;
        this.order = order;
    }

    public String getField() {
        return field;
    }

    public SortField setField(String field) {
        this.field = field;
        return this;
    }

    public String getOrder() {
        return order;
    }

    public SortField setOrder(String order) {
        this.order = order;
        return this;
    }
}
