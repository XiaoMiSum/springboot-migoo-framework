package xyz.migoo.framework.mybatis.core;

import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.github.yulichang.wrapper.JoinAbstractLambdaWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.github.yulichang.wrapper.interfaces.MFunction;
import xyz.migoo.framework.common.util.collection.ArrayUtils;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * @author xiaomi
 * Created at 2023/10/16 21:16
 */
public class MPJLambdaWrapperX<T> extends MPJLambdaWrapper<T> {

    public MPJLambdaWrapperX<T> limit(int n) {
        super.last("LIMIT " + n);
        return this;
    }

    @SafeVarargs
    public final <S> MPJLambdaWrapperX<T> selectX(SFunction<S, ?>... column) {
        super.select(column);
        return this;
    }

    public <X> MPJLambdaWrapperX<T> leftJoinX(Class<T> clazz, SFunction<T, ?> left, SFunction<X, ?> right) {
        super.leftJoin(clazz, left, right);
        return this;
    }

    public <X> MPJLambdaWrapperX<T> innerJoinX(Class<T> clazz, SFunction<T, ?> left, String rightAlias, SFunction<X, ?> right) {
        super.innerJoin(clazz, left, rightAlias, right);
        return this;
    }

    public <X> MPJLambdaWrapperX<T> likeIfPresent(SFunction<X, ?> column, String val) {
        super.likeIfExists(column, val);
        return this;
    }

    public <X> MPJLambdaWrapperX<T> inIfPresent(SFunction<X, ?> column, Collection<?> values) {
        if (!CollectionUtils.isEmpty(values)) {
            super.in(column, values);
        }
        return this;
    }

    public <X> MPJLambdaWrapperX<T> inIfPresent(SFunction<X, ?> column, Object... values) {
        if (!ArrayUtil.isEmpty(values)) {
            super.in(column, values);
        }
        return this;
    }

    public <X> MPJLambdaWrapperX<T> notInIfPresent(SFunction<X, ?> column, Collection<?> values) {
        if (!CollectionUtils.isEmpty(values)) {
            super.notIn(column, values);
        }
        return this;
    }

    public <X> MPJLambdaWrapperX<T> notInIfPresent(SFunction<X, ?> column, Object... values) {
        if (!ArrayUtil.isEmpty(values)) {
            super.notIn(column, values);
        }
        return this;
    }

    public <X> MPJLambdaWrapperX<T> eqIfPresent(SFunction<X, ?> column, Object val) {
        super.eqIfExists(column, val);
        return this;
    }

    public <X> MPJLambdaWrapperX<T> neIfPresent(SFunction<X, ?> column, Object val) {
        super.neIfExists(column, val);
        return this;
    }

    public <X> MPJLambdaWrapperX<T> gtIfPresent(SFunction<X, ?> column, Object val) {
        super.geIfExists(column, val);
        return this;
    }

    public <X> MPJLambdaWrapperX<T> geIfPresent(SFunction<X, ?> column, Object val) {
        super.geIfExists(column, val);
        return this;
    }

    public <X> MPJLambdaWrapperX<T> ltIfPresent(SFunction<X, ?> column, Object val) {
        super.ltIfExists(column, val);
        return this;
    }

    public <X> MPJLambdaWrapperX<T> leIfPresent(SFunction<X, ?> column, Object val) {
        super.leIfExists(column, val);
        return this;
    }

    public <X> MPJLambdaWrapperX<T> betweenIfPresent(SFunction<X, ?> column, Object val1, Object val2) {
        if (val1 != null && val2 != null) {
            super.between(column, val1, val2);
        } else if (val1 != null) {
            super.ge(column, val1);
        } else if (val2 != null) {
            super.le(column, val2);
        }
        return this;
    }

    public <X> MPJLambdaWrapperX<T> betweenIfPresent(SFunction<X, ?> column, Object[] values) {
        Object val1 = ArrayUtils.get(values, 0);
        Object val2 = ArrayUtils.get(values, 1);
        return betweenIfPresent(column, val1, val2);
    }

    // ========== 重写父类方法，方便链式调用 ==========

    @Override
    public MPJLambdaWrapperX<T> leftJoin(String sql) {
        super.leftJoin(sql);
        return this;
    }

    @Override
    public <X> MPJLambdaWrapperX<T> leftJoin(Class<X> clazz, MFunction<JoinAbstractLambdaWrapper<T, ?>> function) {
        super.leftJoin(clazz, function);
        return this;
    }

    @Override
    public <X> MPJLambdaWrapperX<T> leftJoin(Class<X> clazz, String alias, MFunction<JoinAbstractLambdaWrapper<T, ?>> function) {
        super.leftJoin(clazz, alias, function);
        return this;
    }

    @Override
    public MPJLambdaWrapperX<T> rightJoin(String sql) {
        super.rightJoin(sql);
        return this;
    }

    @Override
    public <X> MPJLambdaWrapperX<T> rightJoin(Class<X> clazz, MFunction<JoinAbstractLambdaWrapper<T, ?>> function) {
        super.rightJoin(clazz, function);
        return this;
    }

    @Override
    public <X> MPJLambdaWrapperX<T> rightJoin(Class<X> clazz, String alias, MFunction<JoinAbstractLambdaWrapper<T, ?>> function) {
        super.rightJoin(clazz, alias, function);
        return this;
    }

    @Override
    public MPJLambdaWrapperX<T> fullJoin(String sql) {
        super.fullJoin(sql);
        return this;
    }

    @Override
    public <X> MPJLambdaWrapperX<T> fullJoin(Class<X> clazz, MFunction<JoinAbstractLambdaWrapper<T, ?>> function) {
        super.fullJoin(clazz, function);
        return this;
    }

    @Override
    public <X> MPJLambdaWrapperX<T> fullJoin(Class<X> clazz, String alias, MFunction<JoinAbstractLambdaWrapper<T, ?>> function) {
        super.fullJoin(clazz, alias, function);
        return this;
    }


    @Override
    public <X> MPJLambdaWrapperX<T> eq(boolean condition, SFunction<X, ?> column, Object val) {
        super.eq(condition, column, val);
        return this;
    }

    @Override
    public <X> MPJLambdaWrapperX<T> eq(SFunction<X, ?> column, Object val) {
        super.eq(column, val);
        return this;
    }

    @Override
    public <X> MPJLambdaWrapperX<T> orderByDesc(SFunction<X, ?> column) {
        //noinspection unchecked
        super.orderByDesc(true, column);
        return this;
    }

    @Override
    public MPJLambdaWrapperX<T> last(String lastSql) {
        super.last(lastSql);
        return this;
    }

    @Override
    public <X> MPJLambdaWrapperX<T> in(SFunction<X, ?> column, Collection<?> coll) {
        super.in(column, coll);
        return this;
    }

    @Override
    public MPJLambdaWrapperX<T> selectAll(Class<?> clazz) {
        super.selectAll(clazz);
        return this;
    }

    @Override
    public MPJLambdaWrapperX<T> selectAll(Class<?> clazz, String prefix) {
        super.selectAll(clazz, prefix);
        return this;
    }

    @Override
    public <S> MPJLambdaWrapperX<T> selectAs(SFunction<S, ?> column, String alias) {
        super.selectAs(column, alias);
        return this;
    }

    @Override
    public <E> MPJLambdaWrapperX<T> selectAs(String column, SFunction<E, ?> alias) {
        super.selectAs(column, alias);
        return this;
    }

    @Override
    public <S, X> MPJLambdaWrapperX<T> selectAs(SFunction<S, ?> column, SFunction<X, ?> alias) {
        super.selectAs(column, alias);
        return this;
    }

    @Override
    public <E, X> MPJLambdaWrapperX<T> selectAs(String index, SFunction<E, ?> column, SFunction<X, ?> alias) {
        super.selectAs(index, column, alias);
        return this;
    }

    @Override
    public <E> MPJLambdaWrapperX<T> selectAsClass(Class<E> source, Class<?> tag) {
        super.selectAsClass(source, tag);
        return this;
    }

    @Override
    public <E, F> MPJLambdaWrapperX<T> selectSub(Class<E> clazz, Consumer<MPJLambdaWrapper<E>> consumer, SFunction<F, ?> alias) {
        super.selectSub(clazz, consumer, alias);
        return this;
    }

    @Override
    public <E, F> MPJLambdaWrapperX<T> selectSub(Class<E> clazz, String st, Consumer<MPJLambdaWrapper<E>> consumer, SFunction<F, ?> alias) {
        super.selectSub(clazz, st, consumer, alias);
        return this;
    }

    @Override
    public <S> MPJLambdaWrapperX<T> selectCount(SFunction<S, ?> column) {
        super.selectCount(column);
        return this;
    }

    @Override
    public MPJLambdaWrapperX<T> selectCount(Object column, String alias) {
        super.selectCount(column, alias);
        return this;
    }

    @Override
    public <X> MPJLambdaWrapperX<T> selectCount(Object column, SFunction<X, ?> alias) {
        super.selectCount(column, alias);
        return this;
    }

    @Override
    public <S, X> MPJLambdaWrapperX<T> selectCount(SFunction<S, ?> column, String alias) {
        super.selectCount(column, alias);
        return this;
    }

    @Override
    public <S, X> MPJLambdaWrapperX<T> selectCount(SFunction<S, ?> column, SFunction<X, ?> alias) {
        super.selectCount(column, alias);
        return this;
    }

    @Override
    public <S> MPJLambdaWrapperX<T> selectSum(SFunction<S, ?> column) {
        super.selectSum(column);
        return this;
    }

    @Override
    public <S, X> MPJLambdaWrapperX<T> selectSum(SFunction<S, ?> column, String alias) {
        super.selectSum(column, alias);
        return this;
    }

    @Override
    public <S, X> MPJLambdaWrapperX<T> selectSum(SFunction<S, ?> column, SFunction<X, ?> alias) {
        super.selectSum(column, alias);
        return this;
    }

    @Override
    public <S> MPJLambdaWrapperX<T> selectMax(SFunction<S, ?> column) {
        super.selectMax(column);
        return this;
    }

    @Override
    public <S, X> MPJLambdaWrapperX<T> selectMax(SFunction<S, ?> column, String alias) {
        super.selectMax(column, alias);
        return this;
    }

    @Override
    public <S, X> MPJLambdaWrapperX<T> selectMax(SFunction<S, ?> column, SFunction<X, ?> alias) {
        super.selectMax(column, alias);
        return this;
    }

    @Override
    public <S> MPJLambdaWrapperX<T> selectMin(SFunction<S, ?> column) {
        super.selectMin(column);
        return this;
    }

    @Override
    public <S, X> MPJLambdaWrapperX<T> selectMin(SFunction<S, ?> column, String alias) {
        super.selectMin(column, alias);
        return this;
    }

    @Override
    public <S, X> MPJLambdaWrapperX<T> selectMin(SFunction<S, ?> column, SFunction<X, ?> alias) {
        super.selectMin(column, alias);
        return this;
    }

    @Override
    public <S> MPJLambdaWrapperX<T> selectAvg(SFunction<S, ?> column) {
        super.selectAvg(column);
        return this;
    }

    @Override
    public <S, X> MPJLambdaWrapperX<T> selectAvg(SFunction<S, ?> column, String alias) {
        super.selectAvg(column, alias);
        return this;
    }

    @Override
    public <S, X> MPJLambdaWrapperX<T> selectAvg(SFunction<S, ?> column, SFunction<X, ?> alias) {
        super.selectAvg(column, alias);
        return this;
    }

    @Override
    public <S> MPJLambdaWrapperX<T> selectLen(SFunction<S, ?> column) {
        super.selectLen(column);
        return this;
    }

    @Override
    public <S, X> MPJLambdaWrapperX<T> selectLen(SFunction<S, ?> column, String alias) {
        super.selectLen(column, alias);
        return this;
    }

    @Override
    public <S, X> MPJLambdaWrapperX<T> selectLen(SFunction<S, ?> column, SFunction<X, ?> alias) {
        super.selectLen(column, alias);
        return this;
    }

    @Override
    public MPJLambdaWrapperX<T> or() {
        super.or();
        return this;
    }

    @Override
    public MPJLambdaWrapperX<T> or(boolean condition) {
        super.or(condition);
        return this;
    }

    @Override
    public MPJLambdaWrapperX<T> or(Consumer<MPJLambdaWrapper<T>> consumer) {
        super.or(consumer);
        return this;
    }


    @Override
    public MPJLambdaWrapperX<T> and(boolean condition) {
        super.and(condition);
        return this;
    }

    @Override
    public MPJLambdaWrapperX<T> and(Consumer<MPJLambdaWrapper<T>> consumer) {
        super.or(consumer);
        return this;
    }
}
