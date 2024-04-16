package xyz.migoo.framework.infra.dal.mapper.sys;

import xyz.migoo.framework.infra.controller.sys.permission.role.vo.RoleQueryReqVO;
import xyz.migoo.framework.infra.dal.dataobject.IdEnhanceDO;
import xyz.migoo.framework.infra.dal.dataobject.sys.Role;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.lang.Nullable;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.mybatis.core.BaseMapperX;
import xyz.migoo.framework.mybatis.core.LambdaQueryWrapperX;
import xyz.migoo.framework.mybatis.core.dataobject.BaseDO;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapperX<Role> {

    default Role selectByName(String name) {
        return selectOne(new LambdaQueryWrapperX<Role>().eq(Role::getName, name));
    }

    default Role selectByCode(String code) {
        return selectOne(new LambdaQueryWrapperX<Role>().eq(Role::getCode, code));
    }

    default List<Role> selectListByStatus(@Nullable Collection<Integer> statuses) {
        return selectList(new LambdaQueryWrapperX<Role>().in(Role::getStatus, statuses));
    }

    default boolean selectExistsByUpdateTimeAfter(Date maxUpdateTime) {
        return selectOne(new LambdaQueryWrapperX<Role>().select(IdEnhanceDO::getId)
                .gt(BaseDO::getUpdateTime, maxUpdateTime).last("LIMIT 1")) != null;
    }

    default PageResult<Role> selectPage(RoleQueryReqVO req) {
        return selectPage(req, new LambdaQueryWrapperX<Role>()
                .likeIfPresent(Role::getName, req.getName())
                .likeIfPresent(Role::getCode, req.getCode())
                .eqIfPresent(Role::getStatus, req.getStatus())
                .gt(IdEnhanceDO::getId, 1)
                .orderByAsc(IdEnhanceDO::getId));
    }

}
