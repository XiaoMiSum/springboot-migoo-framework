package xyz.migoo.framework.mybatis.core.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import xyz.migoo.framework.mybatis.core.dataobject.BaseDO;
import xyz.migoo.framework.web.core.util.WebFrameworkUtils;

import java.util.Date;
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
        if (Objects.nonNull(metaObject) && metaObject.getOriginalObject() instanceof BaseDO) {
            BaseDO baseDO = (BaseDO) metaObject.getOriginalObject();

            // 创建时间为空，则以当前时间为插入时间
            baseDO.setCreateTime(Objects.isNull(baseDO.getCreateTime()) ? new Date() : baseDO.getCreateTime());
            // 更新时间为空，则以当前时间为更新时间
            baseDO.setUpdateTime(Objects.isNull(baseDO.getUpdateTime()) ? baseDO.getCreateTime() : baseDO.getUpdateTime());
            // 状态标识为空，则默认为已启用状态
            String compoundName = WebFrameworkUtils.getLoginUserName();
            // 当前登录用户不为空，创建人为空，则当前登录用户为创建人
            baseDO.setCreator(Objects.isNull(baseDO.getCreator()) ? Objects.isNull(compoundName) ? "系统" : compoundName : baseDO.getCreator());
            // 当前登录用户不为空，更新人为空，则当前登录用户为更新人
            baseDO.setUpdater(Objects.isNull(baseDO.getUpdater()) ? Objects.isNull(compoundName) ? "系统" : compoundName : baseDO.getUpdater());
            baseDO.setDeleted(0);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 默认以当前时间为更新时间
        setFieldValByName("updateTime", new Date(), metaObject);
        // 默认以当前登录用户为更新人
        BaseDO baseDO = (BaseDO) metaObject.getOriginalObject();
        String compoundName = Objects.isNull(baseDO.getUpdater()) ? WebFrameworkUtils.getLoginUserName()
                : baseDO.getUpdater();
        setFieldValByName("updater", compoundName, metaObject);
    }
}
