package xyz.migoo.framework.mybatis.core;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.google.common.collect.Lists;
import org.apache.ibatis.reflection.property.PropertyNamer;
import org.springframework.util.StringUtils;
import xyz.migoo.framework.common.util.collection.ArrayUtils;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author xiaomi
 * Created on 2021/11/23 20:30
 */
public class QueryWrapperX<T> extends QueryWrapper<T> {


    @SafeVarargs
    public final QueryWrapperX<T> select(SFunction<T, ?>... columns) {
        if (columns.length > 0) {
            List<String> columnList = Lists.newArrayList();
            for (SFunction<T, ?> column : columns) {
                columnList.add(columnToString(column));
            }
            select(columnList);
        }
        return this;
    }

    private String columnToString(SFunction<T, ?> column) {
        return StrUtil.toUnderlineCase(PropertyNamer.methodToProperty(LambdaUtils.extract(column).getImplMethodName()));
    }

    public QueryWrapperX<T> limit(int n) {
        super.last("LIMIT " + n);
        return this;
    }

    public QueryWrapperX<T> likeIfPresent(SFunction<T, ?> column, String val) {
        return likeIfPresent(columnToString(column), val);
    }

    public QueryWrapperX<T> likeIfPresent(String column, String val) {
        if (StringUtils.hasText(val)) {
            return (QueryWrapperX<T>) super.like(column, val);
        }
        return this;
    }

    public QueryWrapperX<T> inIfPresent(SFunction<T, ?> column, Object... val) {
        return inIfPresent(columnToString(column), val);
    }

    public QueryWrapperX<T> inIfPresent(SFunction<T, ?> column, Collection<?> val) {
        return inIfPresent(columnToString(column), val);
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

    public QueryWrapperX<T> notInIfPresent(SFunction<T, ?> column, Collection<?> val) {
        return notInIfPresent(columnToString(column), val);
    }

    public QueryWrapperX<T> notInIfPresent(SFunction<T, ?> column, Object... val) {
        return notInIfPresent(columnToString(column), val);
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

    public QueryWrapperX<T> eqIfPresent(SFunction<T, ?> column, Object val) {
        return eqIfPresent(columnToString(column), val);
    }

    public QueryWrapperX<T> eqIfPresent(String column, Object val) {
        if (ObjectUtil.isNotEmpty(val)) {
            return (QueryWrapperX<T>) super.eq(column, val);
        }
        return this;
    }

    public QueryWrapperX<T> neIfPresent(SFunction<T, ?> column, Object val) {
        return neIfPresent(columnToString(column), val);
    }

    public QueryWrapperX<T> neIfPresent(String column, Object val) {
        if (ObjectUtil.isNotEmpty(val)) {
            return (QueryWrapperX<T>) super.ne(column, val);
        }
        return this;
    }

    public QueryWrapperX<T> gtIfPresent(SFunction<T, ?> column, Object val) {
        return gtIfPresent(columnToString(column), val);
    }

    public QueryWrapperX<T> gtIfPresent(String column, Object val) {
        if (val != null) {
            return (QueryWrapperX<T>) super.gt(column, val);
        }
        return this;
    }

    public QueryWrapperX<T> geIfPresent(SFunction<T, ?> column, Object val) {
        return geIfPresent(columnToString(column), val);
    }

    public QueryWrapperX<T> geIfPresent(String column, Object val) {
        if (val != null) {
            return (QueryWrapperX<T>) super.ge(column, val);
        }
        return this;
    }

    public QueryWrapperX<T> ltIfPresent(SFunction<T, ?> column, Object val) {
        return ltIfPresent(columnToString(column), val);
    }

    public QueryWrapperX<T> ltIfPresent(String column, Object val) {
        if (val != null) {
            return (QueryWrapperX<T>) super.lt(column, val);
        }
        return this;
    }

    public QueryWrapperX<T> leIfPresent(SFunction<T, ?> column, Object val) {
        return leIfPresent(columnToString(column), val);
    }

    public QueryWrapperX<T> leIfPresent(String column, Object val) {
        if (val != null) {
            return (QueryWrapperX<T>) super.le(column, val);
        }
        return this;
    }

    public QueryWrapperX<T> betweenIfPresent(SFunction<T, ?> column, Object val1, Object val2) {
        return betweenIfPresent(columnToString(column), val1, val2);
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

    public QueryWrapperX<T> betweenIfPresent(SFunction<T, ?> column, Object[] values) {
        return betweenIfPresent(columnToString(column), values);
    }

    public QueryWrapperX<T> betweenIfPresent(String column, Object[] values) {
        Object val1 = xyz.migoo.framework.common.util.collection.ArrayUtils.get(values, 0);
        Object val2 = ArrayUtils.get(values, 1);
        return betweenIfPresent(column, val1, val2);
    }

    public QueryWrapperX<T> eq(boolean condition, SFunction<T, ?> column, Object val) {
        return eq(condition, columnToString(column), val);
    }

    public QueryWrapperX<T> eq(SFunction<T, ?> column, Object val) {
        return eq(columnToString(column), val);
    }

    public QueryWrapperX<T> ne(boolean condition, SFunction<T, ?> column, Object val) {
        return ne(condition, columnToString(column), val);
    }

    public QueryWrapperX<T> ne(SFunction<T, ?> column, Object val) {
        return ne(columnToString(column), val);
    }

    public QueryWrapperX<T> gt(boolean condition, SFunction<T, ?> column, Object val) {
        return gt(condition, columnToString(column), val);
    }

    public QueryWrapperX<T> gt(SFunction<T, ?> column, Object val) {
        return gt(columnToString(column), val);
    }

    public QueryWrapperX<T> ge(boolean condition, SFunction<T, ?> column, Object val) {
        return ge(condition, columnToString(column), val);
    }

    public QueryWrapperX<T> ge(SFunction<T, ?> column, Object val) {
        return ge(columnToString(column), val);
    }

    public QueryWrapperX<T> lt(boolean condition, SFunction<T, ?> column, Object val) {
        return lt(condition, columnToString(column), val);
    }

    public QueryWrapperX<T> lt(SFunction<T, ?> column, Object val) {
        return lt(columnToString(column), val);
    }

    public QueryWrapperX<T> le(boolean condition, SFunction<T, ?> column, Object val) {
        return le(condition, columnToString(column), val);
    }

    public QueryWrapperX<T> le(SFunction<T, ?> column, Object val) {
        return le(columnToString(column), val);
    }

    public QueryWrapperX<T> isNull(boolean condition, SFunction<T, ?> column) {
        return isNull(condition, columnToString(column));
    }

    public QueryWrapperX<T> isNull(SFunction<T, ?> column) {
        return isNull(columnToString(column));
    }

    public QueryWrapperX<T> isNotNull(boolean condition, SFunction<T, ?> column) {
        return isNotNull(condition, columnToString(column));
    }

    public QueryWrapperX<T> isNotNull(SFunction<T, ?> column) {
        return isNotNull(columnToString(column));
    }

    public QueryWrapperX<T> in(boolean condition, SFunction<T, ?> column, Object... values) {
        return in(condition, columnToString(column), values);
    }

    public QueryWrapperX<T> in(SFunction<T, ?> column, Object... values) {
        return in(columnToString(column), values);
    }

    public QueryWrapperX<T> in(boolean condition, SFunction<T, ?> column, Collection<?> values) {
        return in(condition, columnToString(column), values);
    }

    public QueryWrapperX<T> in(SFunction<T, ?> column, Collection<?> values) {
        return in(columnToString(column), values);
    }

    public QueryWrapperX<T> notIn(boolean condition, SFunction<T, ?> column, Object... values) {
        return notIn(condition, columnToString(column), values);
    }

    public QueryWrapperX<T> notIn(SFunction<T, ?> column) {
        return notIn(columnToString(column));
    }

    public QueryWrapperX<T> notIn(boolean condition, SFunction<T, ?> column, Collection<?> values) {
        return notIn(condition, columnToString(column), values);
    }

    public QueryWrapperX<T> notIn(SFunction<T, ?> column, Collection<?> values) {
        return notIn(columnToString(column), values);
    }

    public QueryWrapperX<T> orderByAsc(boolean condition, SFunction<T, ?> column) {
        return orderByAsc(condition, columnToString(column));
    }

    public QueryWrapperX<T> orderByAsc(SFunction<T, ?> column) {
        return orderByAsc(columnToString(column));
    }

    public QueryWrapperX<T> orderByDesc(boolean condition, SFunction<T, ?> column) {
        return orderByDesc(condition, columnToString(column));
    }

    public QueryWrapperX<T> orderByDesc(SFunction<T, ?> column) {
        return orderByDesc(columnToString(column));
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
    public QueryWrapperX<T> orderByAsc(boolean condition, String column) {
        super.orderByAsc(condition, column);
        return this;
    }

    @Override
    public QueryWrapperX<T> orderByAsc(String column) {
        super.orderByAsc(column);
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
    public QueryWrapperX<T> in(boolean condition, String column, Object... coll) {
        super.in(condition, column, coll);
        return this;
    }

    @Override
    public QueryWrapperX<T> in(String column, Object... coll) {
        super.in(column, coll);
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
    public QueryWrapperX<T> notIn(boolean condition, String column, Object... coll) {
        super.notIn(condition, column, coll);
        return this;
    }

    @Override
    public QueryWrapperX<T> notIn(String column, Object... coll) {
        super.notIn(column, coll);
        return this;
    }

    @Override
    public QueryWrapperX<T> notIn(boolean condition, String column, Collection<?> coll) {
        super.notIn(condition, column, coll);
        return this;
    }

    @Override
    public QueryWrapperX<T> notIn(String column, Collection<?> coll) {
        super.notIn(column, coll);
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
