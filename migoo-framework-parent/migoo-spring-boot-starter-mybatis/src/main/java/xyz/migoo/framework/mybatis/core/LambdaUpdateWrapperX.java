package xyz.migoo.framework.mybatis.core;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.springframework.util.StringUtils;
import xyz.migoo.framework.common.util.collection.ArrayUtils;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * 拓展 MyBatis Plus LambdaUpdateWrapper 类，主要增加如下功能：
 * <p>
 * 1. 拼接条件的方法，增加 xxxIfPresent 方法，用于判断值不存在的时候，不要拼接到条件中。
 *
 * @param <T> 数据类型
 */
public class LambdaUpdateWrapperX<T> extends LambdaUpdateWrapper<T> {

    public LambdaUpdateWrapperX<T> likeIfPresent(SFunction<T, ?> column, String val) {
        if (StringUtils.hasText(val)) {
            return (LambdaUpdateWrapperX<T>) super.like(column, val);
        }
        return this;
    }

    public LambdaUpdateWrapperX<T> inIfPresent(SFunction<T, ?> column, Collection<?> values) {
        if (!CollectionUtils.isEmpty(values)) {
            return (LambdaUpdateWrapperX<T>) super.in(column, values);
        }
        return this;
    }

    public LambdaUpdateWrapperX<T> inIfPresent(SFunction<T, ?> column, Object... values) {
        if (!ArrayUtil.isEmpty(values)) {
            return (LambdaUpdateWrapperX<T>) super.in(column, values);
        }
        return this;
    }

    public LambdaUpdateWrapperX<T> notInIfPresent(SFunction<T, ?> column, Collection<?> coll) {
        if (!CollectionUtils.isEmpty(coll)) {
            return (LambdaUpdateWrapperX<T>) super.notIn(column, coll);
        }
        return this;
    }

    public LambdaUpdateWrapperX<T> notInIfPresent(SFunction<T, ?> column, Object... values) {
        if (!ArrayUtil.isEmpty(values)) {
            return (LambdaUpdateWrapperX<T>) super.notIn(column, values);
        }
        return this;
    }

    public LambdaUpdateWrapperX<T> eqIfPresent(SFunction<T, ?> column, Object val) {
        if (ObjectUtil.isNotEmpty(val)) {
            return (LambdaUpdateWrapperX<T>) super.eq(column, val);
        }
        return this;
    }

    public LambdaUpdateWrapperX<T> neIfPresent(SFunction<T, ?> column, Object val) {
        if (ObjectUtil.isNotEmpty(val)) {
            return (LambdaUpdateWrapperX<T>) super.ne(column, val);
        }
        return this;
    }

    public LambdaUpdateWrapperX<T> gtIfPresent(SFunction<T, ?> column, Object val) {
        if (val != null) {
            return (LambdaUpdateWrapperX<T>) super.gt(column, val);
        }
        return this;
    }

    public LambdaUpdateWrapperX<T> geIfPresent(SFunction<T, ?> column, Object val) {
        if (val != null) {
            return (LambdaUpdateWrapperX<T>) super.ge(column, val);
        }
        return this;
    }

    public LambdaUpdateWrapperX<T> ltIfPresent(SFunction<T, ?> column, Object val) {
        if (val != null) {
            return (LambdaUpdateWrapperX<T>) super.lt(column, val);
        }
        return this;
    }

    public LambdaUpdateWrapperX<T> leIfPresent(SFunction<T, ?> column, Object val) {
        if (val != null) {
            return (LambdaUpdateWrapperX<T>) super.le(column, val);
        }
        return this;
    }

    public LambdaUpdateWrapperX<T> betweenIfPresent(SFunction<T, ?> column, Object val1, Object val2) {
        if (val1 != null && val2 != null) {
            return (LambdaUpdateWrapperX<T>) super.between(column, val1, val2);
        }
        if (val1 != null) {
            return ge(column, val1);
        }
        if (val2 != null) {
            return le(column, val2);
        }
        return this;
    }

    public LambdaUpdateWrapperX<T> betweenIfPresent(SFunction<T, ?> column, Object[] values) {
        Object val1 = ArrayUtils.get(values, 0);
        Object val2 = ArrayUtils.get(values, 1);
        return betweenIfPresent(column, val1, val2);
    }

    // ========== 重写父类方法，方便链式调用 ==========

    @Override
    public LambdaUpdateWrapperX<T> set(boolean condition, SFunction<T, ?> column, Object val, String mapping) {
        return (LambdaUpdateWrapperX<T>) super.set(condition, column, val, mapping);
    }

    @Override
    public LambdaUpdateWrapperX<T> setSql(boolean condition, String setSql, Object... params) {
        return (LambdaUpdateWrapperX<T>) super.setSql(condition, setSql, params);
    }

    @Override
    public LambdaUpdateWrapperX<T> eq(boolean condition, SFunction<T, ?> column, Object val) {
        super.eq(condition, column, val);
        return this;
    }

    @Override
    public LambdaUpdateWrapperX<T> eq(SFunction<T, ?> column, Object val) {
        super.eq(column, val);
        return this;
    }


    @Override
    public LambdaUpdateWrapperX<T> ne(boolean condition, SFunction<T, ?> column, Object val) {
        super.ne(condition, column, val);
        return this;
    }

    @Override
    public LambdaUpdateWrapperX<T> ne(SFunction<T, ?> column, Object val) {
        super.ne(column, val);
        return this;
    }

    @Override
    public LambdaUpdateWrapperX<T> gt(boolean condition, SFunction<T, ?> column, Object val) {
        super.gt(condition, column, val);
        return this;
    }

    @Override
    public LambdaUpdateWrapperX<T> gt(SFunction<T, ?> column, Object val) {
        super.gt(column, val);
        return this;
    }

    @Override
    public LambdaUpdateWrapperX<T> ge(boolean condition, SFunction<T, ?> column, Object val) {
        super.ge(condition, column, val);
        return this;
    }

    @Override
    public LambdaUpdateWrapperX<T> ge(SFunction<T, ?> column, Object val) {
        super.ge(column, val);
        return this;
    }

    @Override
    public LambdaUpdateWrapperX<T> le(boolean condition, SFunction<T, ?> column, Object val) {
        super.le(condition, column, val);
        return this;
    }

    @Override
    public LambdaUpdateWrapperX<T> le(SFunction<T, ?> column, Object val) {
        super.le(column, val);
        return this;
    }

    @Override
    public LambdaUpdateWrapperX<T> lt(boolean condition, SFunction<T, ?> column, Object val) {
        super.lt(condition, column, val);
        return this;
    }

    @Override
    public LambdaUpdateWrapperX<T> lt(SFunction<T, ?> column, Object val) {
        super.lt(column, val);
        return this;
    }

    @Override
    public LambdaUpdateWrapperX<T> in(boolean condition, SFunction<T, ?> column, Collection<?> coll) {
        super.in(condition, column, coll);
        return this;
    }

    @Override
    public LambdaUpdateWrapperX<T> in(SFunction<T, ?> column, Collection<?> coll) {
        super.in(column, coll);
        return this;
    }

    @Override
    public LambdaUpdateWrapperX<T> in(boolean condition, SFunction<T, ?> column, Object... objects) {
        super.in(condition, column, objects);
        return this;
    }

    @Override
    public LambdaUpdateWrapperX<T> in(SFunction<T, ?> column, Object... objects) {
        super.in(column, objects);
        return this;
    }

    @Override
    public LambdaUpdateWrapperX<T> notIn(boolean condition, SFunction<T, ?> column, Collection<?> coll) {
        super.notIn(condition, column, coll);
        return this;
    }

    @Override
    public LambdaUpdateWrapperX<T> notIn(SFunction<T, ?> column, Collection<?> coll) {
        super.notIn(column, coll);
        return this;
    }

    @Override
    public LambdaUpdateWrapperX<T> notIn(boolean condition, SFunction<T, ?> column, Object... objects) {
        super.notIn(condition, column, objects);
        return this;
    }

    @Override
    public LambdaUpdateWrapperX<T> notIn(SFunction<T, ?> column, Object... objects) {
        super.notIn(column, objects);
        return this;
    }

    @Override
    public LambdaUpdateWrapperX<T> isNotNull(boolean condition, SFunction<T, ?> column) {
        super.isNotNull(condition, column);
        return this;
    }

    @Override
    public LambdaUpdateWrapperX<T> isNotNull(SFunction<T, ?> column) {
        super.isNotNull(column);
        return this;
    }

    @Override
    public LambdaUpdateWrapperX<T> isNull(boolean condition, SFunction<T, ?> column) {
        super.isNull(condition, column);
        return this;
    }

    @Override
    public LambdaUpdateWrapperX<T> isNull(SFunction<T, ?> column) {
        super.isNull(column);
        return this;
    }


    @Override
    public LambdaUpdateWrapperX<T> or() {
        super.or();
        return this;
    }

    @Override
    public LambdaUpdateWrapperX<T> or(boolean condition) {
        super.or(condition);
        return this;
    }

    @Override
    public LambdaUpdateWrapperX<T> or(Consumer<LambdaUpdateWrapper<T>> consumer) {
        super.or(consumer);
        return this;
    }


    @Override
    public LambdaUpdateWrapperX<T> and(boolean condition) {
        super.and(condition);
        return this;
    }

    @Override
    public LambdaUpdateWrapperX<T> and(Consumer<LambdaUpdateWrapper<T>> consumer) {
        super.or(consumer);
        return this;
    }

}
