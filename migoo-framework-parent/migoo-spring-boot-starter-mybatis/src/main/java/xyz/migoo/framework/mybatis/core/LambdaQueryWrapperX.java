package xyz.migoo.framework.mybatis.core;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.springframework.util.StringUtils;
import xyz.migoo.framework.common.util.collection.ArrayUtils;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * 拓展 MyBatis Plus QueryWrapper 类，主要增加如下功能：
 * <p>
 * 1. 拼接条件的方法，增加 xxxIfPresent 方法，用于判断值不存在的时候，不要拼接到条件中。
 *
 * @param <T> 数据类型
 */
public class LambdaQueryWrapperX<T> extends LambdaQueryWrapper<T> {


    public LambdaQueryWrapperX<T> limit(int n) {
        super.last("LIMIT " + n);
        return this;
    }

    @SafeVarargs
    public final LambdaQueryWrapper<T> selectX(SFunction<T, ?>... columns) {
        super.select(columns);
        return this;
    }

    public LambdaQueryWrapperX<T> likeIfPresent(SFunction<T, ?> column, String val) {
        if (StringUtils.hasText(val)) {
            return (LambdaQueryWrapperX<T>) super.like(column, val);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> likeLeftIfPresent(SFunction<T, ?> column, String val) {
        if (StringUtils.hasText(val)) {
            return (LambdaQueryWrapperX<T>) super.likeLeft(column, val);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> likeRightIfPresent(SFunction<T, ?> column, String val) {
        if (StringUtils.hasText(val)) {
            return (LambdaQueryWrapperX<T>) super.likeRight(column, val);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> inIfPresent(SFunction<T, ?> column, Collection<?> values) {
        if (!CollectionUtils.isEmpty(values)) {
            return (LambdaQueryWrapperX<T>) super.in(column, values);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> inIfPresent(SFunction<T, ?> column, Object... values) {
        if (!ArrayUtil.isEmpty(values)) {
            return (LambdaQueryWrapperX<T>) super.in(column, values);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> notInIfPresent(SFunction<T, ?> column, Collection<?> coll) {
        if (!CollectionUtils.isEmpty(coll)) {
            return (LambdaQueryWrapperX<T>) super.notIn(column, coll);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> notInIfPresent(SFunction<T, ?> column, Object... values) {
        if (!ArrayUtil.isEmpty(values)) {
            return (LambdaQueryWrapperX<T>) super.notIn(column, values);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> eqIfPresent(SFunction<T, ?> column, Object val) {
        if (ObjectUtil.isNotEmpty(val)) {
            return (LambdaQueryWrapperX<T>) super.eq(column, val);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> neIfPresent(SFunction<T, ?> column, Object val) {
        if (ObjectUtil.isNotEmpty(val)) {
            return (LambdaQueryWrapperX<T>) super.ne(column, val);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> gtIfPresent(SFunction<T, ?> column, Object val) {
        if (val != null) {
            return (LambdaQueryWrapperX<T>) super.gt(column, val);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> geIfPresent(SFunction<T, ?> column, Object val) {
        if (val != null) {
            return (LambdaQueryWrapperX<T>) super.ge(column, val);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> ltIfPresent(SFunction<T, ?> column, Object val) {
        if (val != null) {
            return (LambdaQueryWrapperX<T>) super.lt(column, val);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> leIfPresent(SFunction<T, ?> column, Object val) {
        if (val != null) {
            return (LambdaQueryWrapperX<T>) super.le(column, val);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> betweenIfPresent(SFunction<T, ?> column, Object val1, Object val2) {
        if (val1 != null && val2 != null) {
            return (LambdaQueryWrapperX<T>) super.between(column, val1, val2);
        }
        if (val1 != null) {
            return ge(column, val1);
        }
        if (val2 != null) {
            return le(column, val2);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> betweenIfPresent(SFunction<T, ?> column, Object[] values) {
        Object val1 = ArrayUtils.get(values, 0);
        Object val2 = ArrayUtils.get(values, 1);
        return betweenIfPresent(column, val1, val2);
    }

    // ========== 重写父类方法，方便链式调用 ==========

    @Override
    public LambdaQueryWrapperX<T> eq(boolean condition, SFunction<T, ?> column, Object val) {
        super.eq(condition, column, val);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> eq(SFunction<T, ?> column, Object val) {
        super.eq(column, val);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> ne(boolean condition, SFunction<T, ?> column, Object val) {
        super.ne(condition, column, val);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> ne(SFunction<T, ?> column, Object val) {
        super.ne(column, val);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> gt(boolean condition, SFunction<T, ?> column, Object val) {
        super.gt(condition, column, val);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> gt(SFunction<T, ?> column, Object val) {
        super.gt(column, val);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> ge(boolean condition, SFunction<T, ?> column, Object val) {
        super.ge(condition, column, val);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> ge(SFunction<T, ?> column, Object val) {
        super.ge(column, val);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> le(boolean condition, SFunction<T, ?> column, Object val) {
        super.le(condition, column, val);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> le(SFunction<T, ?> column, Object val) {
        super.le(column, val);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> lt(boolean condition, SFunction<T, ?> column, Object val) {
        super.lt(condition, column, val);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> lt(SFunction<T, ?> column, Object val) {
        super.lt(column, val);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> orderByAsc(boolean condition, SFunction<T, ?> column) {
        super.orderByAsc(condition, column);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> orderByAsc(SFunction<T, ?> column) {
        super.orderByAsc(column);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> orderByDesc(boolean condition, SFunction<T, ?> column) {
        super.orderByDesc(condition, column);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> orderByDesc(SFunction<T, ?> column) {
        super.orderByDesc(column);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> last(boolean condition, String lastSql) {
        super.last(condition, lastSql);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> last(String lastSql) {
        super.last(lastSql);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> in(boolean condition, SFunction<T, ?> column, Collection<?> coll) {
        super.in(condition, column, coll);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> in(SFunction<T, ?> column, Collection<?> coll) {
        super.in(column, coll);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> in(boolean condition, SFunction<T, ?> column, Object... objects) {
        super.in(condition, column, objects);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> in(SFunction<T, ?> column, Object... objects) {
        super.in(column, objects);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> notIn(boolean condition, SFunction<T, ?> column, Collection<?> coll) {
        super.notIn(condition, column, coll);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> notIn(SFunction<T, ?> column, Collection<?> coll) {
        super.notIn(column, coll);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> notIn(boolean condition, SFunction<T, ?> column, Object... objects) {
        super.notIn(condition, column, objects);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> notIn(SFunction<T, ?> column, Object... objects) {
        super.notIn(column, objects);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> isNotNull(boolean condition, SFunction<T, ?> column) {
        super.isNotNull(condition, column);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> isNotNull(SFunction<T, ?> column) {
        super.isNotNull(column);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> isNull(boolean condition, SFunction<T, ?> column) {
        super.isNull(condition, column);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> isNull(SFunction<T, ?> column) {
        super.isNull(column);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> or() {
        super.or();
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> or(boolean condition) {
        super.or(condition);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> or(Consumer<LambdaQueryWrapper<T>> consumer) {
        super.or(consumer);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> or(boolean condition, Consumer<LambdaQueryWrapper<T>> consumer) {
        super.or(condition, consumer);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> and(boolean condition) {
        super.and(condition);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> and(Consumer<LambdaQueryWrapper<T>> consumer) {
        super.and(consumer);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> and(boolean condition, Consumer<LambdaQueryWrapper<T>> consumer) {
        super.and(consumer);
        return this;
    }

}
