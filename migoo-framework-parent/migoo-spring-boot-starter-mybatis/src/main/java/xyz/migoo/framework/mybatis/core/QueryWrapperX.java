package xyz.migoo.framework.mybatis.core;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.springframework.util.StringUtils;
import xyz.migoo.framework.common.util.collection.ArrayUtils;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * @author xiaomi
 * Created on 2021/11/23 20:30
 */
public class QueryWrapperX<T> extends QueryWrapper<T> {

    public QueryWrapperX<T> limit(int n) {
        super.last("LIMIT " + n);
        return this;
    }

    public QueryWrapperX<T> likeIfPresent(String column, String val) {
        if (StringUtils.hasText(val)) {
            return (QueryWrapperX<T>) super.like(column, val);
        }
        return this;
    }

    public QueryWrapperX<T> inIfPresent(String column, Collection<?> values) {
        if (!CollectionUtils.isEmpty(values)) {
            return (QueryWrapperX<T>) super.in(column, values);
        }
        return this;
    }

    public QueryWrapperX<T> inIfPresent(String column, Object... values) {
        if (!ArrayUtil.isEmpty(values)) {
            return (QueryWrapperX<T>) super.in(column, values);
        }
        return this;
    }

    public QueryWrapperX<T> notInIfPresent(String column, Collection<?> coll) {
        if (!CollectionUtils.isEmpty(coll)) {
            return (QueryWrapperX<T>) super.notIn(column, coll);
        }
        return this;
    }

    public QueryWrapperX<T> notInIfPresent(String column, Object... values) {
        if (!ArrayUtil.isEmpty(values)) {
            return (QueryWrapperX<T>) super.notIn(column, values);
        }
        return this;
    }

    public QueryWrapperX<T> eqIfPresent(String column, Object val) {
        if (ObjectUtil.isNotEmpty(val)) {
            return (QueryWrapperX<T>) super.eq(column, val);
        }
        return this;
    }

    public QueryWrapperX<T> neIfPresent(String column, Object val) {
        if (ObjectUtil.isNotEmpty(val)) {
            return (QueryWrapperX<T>) super.ne(column, val);
        }
        return this;
    }

    public QueryWrapperX<T> gtIfPresent(String column, Object val) {
        if (val != null) {
            return (QueryWrapperX<T>) super.gt(column, val);
        }
        return this;
    }

    public QueryWrapperX<T> geIfPresent(String column, Object val) {
        if (val != null) {
            return (QueryWrapperX<T>) super.ge(column, val);
        }
        return this;
    }

    public QueryWrapperX<T> ltIfPresent(String column, Object val) {
        if (val != null) {
            return (QueryWrapperX<T>) super.lt(column, val);
        }
        return this;
    }

    public QueryWrapperX<T> leIfPresent(String column, Object val) {
        if (val != null) {
            return (QueryWrapperX<T>) super.le(column, val);
        }
        return this;
    }

    public QueryWrapperX<T> betweenIfPresent(String column, Object val1, Object val2) {
        if (val1 != null && val2 != null) {
            return (QueryWrapperX<T>) super.between(column, val1, val2);
        }
        if (val1 != null) {
            return ge(column, val1);
        }
        if (val2 != null) {
            return le(column, val2);
        }
        return this;
    }

    public QueryWrapperX<T> betweenIfPresent(String column, Object[] values) {
        Object val1 = xyz.migoo.framework.common.util.collection.ArrayUtils.get(values, 0);
        Object val2 = ArrayUtils.get(values, 1);
        return betweenIfPresent(column, val1, val2);
    }

    // ========== 重写父类方法，方便链式调用 ==========

    @Override
    public QueryWrapperX<T> select(String... column) {
        super.select(column);
        return this;
    }

    @Override
    public QueryWrapperX<T> eq(boolean condition, String column, Object val) {
        super.eq(condition, column, val);
        return this;
    }

    @Override
    public QueryWrapperX<T> eq(String column, Object val) {
        super.eq(column, val);
        return this;
    }

    @Override
    public QueryWrapperX<T> ne(boolean condition, String column, Object val) {
        super.ne(condition, column, val);
        return this;
    }

    @Override
    public QueryWrapperX<T> ne(String column, Object val) {
        super.ne(column, val);
        return this;
    }

    @Override
    public QueryWrapperX<T> gt(boolean condition, String column, Object val) {
        super.gt(condition, column, val);
        return this;
    }

    @Override
    public QueryWrapperX<T> gt(String column, Object val) {
        super.gt(column, val);
        return this;
    }

    @Override
    public QueryWrapperX<T> ge(boolean condition, String column, Object val) {
        super.ge(condition, column, val);
        return this;
    }

    @Override
    public QueryWrapperX<T> ge(String column, Object val) {
        super.ge(column, val);
        return this;
    }

    @Override
    public QueryWrapperX<T> le(boolean condition, String column, Object val) {
        super.le(condition, column, val);
        return this;
    }

    @Override
    public QueryWrapperX<T> le(String column, Object val) {
        super.le(column, val);
        return this;
    }

    @Override
    public QueryWrapperX<T> lt(boolean condition, String column, Object val) {
        super.lt(condition, column, val);
        return this;
    }

    @Override
    public QueryWrapperX<T> lt(String column, Object val) {
        super.lt(column, val);
        return this;
    }

    @Override
    public QueryWrapperX<T> orderByDesc(boolean condition, String column) {
        super.orderByDesc(condition, column);
        return this;
    }

    @Override
    public QueryWrapperX<T> orderByDesc(String column) {
        super.orderByDesc(column);
        return this;
    }

    @Override
    public QueryWrapperX<T> last(boolean condition, String lastSql) {
        super.last(condition, lastSql);
        return this;
    }

    @Override
    public QueryWrapperX<T> last(String lastSql) {
        super.last(lastSql);
        return this;
    }

    @Override
    public QueryWrapperX<T> in(boolean condition, String column, Collection<?> coll) {
        super.in(condition, column, coll);
        return this;
    }

    @Override
    public QueryWrapperX<T> in(String column, Collection<?> coll) {
        super.in(column, coll);
        return this;
    }

    @Override
    public QueryWrapperX<T> isNotNull(boolean condition, String column) {
        super.isNotNull(condition, column);
        return this;
    }

    @Override
    public QueryWrapperX<T> isNotNull(String column) {
        super.isNotNull(column);
        return this;
    }

    @Override
    public QueryWrapperX<T> isNull(boolean condition, String column) {
        super.isNull(condition, column);
        return this;
    }

    @Override
    public QueryWrapperX<T> isNull(String column) {
        super.isNull(column);
        return this;
    }


    @Override
    public QueryWrapperX<T> or() {
        super.or();
        return this;
    }

    @Override
    public QueryWrapperX<T> or(boolean condition) {
        super.or(condition);
        return this;
    }

    @Override
    public QueryWrapperX<T> or(Consumer<QueryWrapper<T>> consumer) {
        super.or(consumer);
        return this;
    }


    @Override
    public QueryWrapperX<T> and(boolean condition) {
        super.and(condition);
        return this;
    }

    @Override
    public QueryWrapperX<T> and(Consumer<QueryWrapper<T>> consumer) {
        super.or(consumer);
        return this;
    }
}
