package xyz.migoo.framework.mybatis.core.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import xyz.migoo.framework.mybatis.core.dataobject.BaseDO;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 通用参数填充实现类
 * <p>
 * 如果没有显式的对通用参数进行赋值，这里会对通用参数进行填充、赋值
 *
 * @author xiaomi
 * Created on 2021/11/21 12:54
 */
public class DefaultFieldHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        if (Objects.nonNull(metaObject) && metaObject.getOriginalObject() instanceof BaseDO<?, ?> baseDO) {
            // 创建时间为空，则以当前时间为插入时间
            baseDO.setCreatedAt(Objects.isNull(baseDO.getCreatedAt()) ? LocalDateTime.now() : baseDO.getCreatedAt());
            baseDO.setIsDeleted(false);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 默认以当前时间为更新时间
        setFieldValByName("updatedAt", LocalDateTime.now(), metaObject);
    }
}
