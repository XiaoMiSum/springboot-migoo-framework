package xyz.migoo.framework.infra.dal.mapper.sys;

import xyz.migoo.framework.infra.dal.dataobject.sys.UserRole;
import org.apache.ibatis.annotations.Mapper;
import xyz.migoo.framework.mybatis.core.BaseMapperX;
import xyz.migoo.framework.mybatis.core.LambdaQueryWrapperX;

import java.util.Collection;
import java.util.List;

@Mapper
public interface UserRoleMapper extends BaseMapperX<UserRole> {

    default List<UserRole> selectListByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<UserRole>().eq(UserRole::getUserId, userId));
    }

    default void insertList(Long userId, Collection<Long> roleIds) {
        List<UserRole> list = roleIds.stream().map(roleId -> {
            UserRole entity = new UserRole();
            entity.setUserId(userId);
            entity.setRoleId(roleId);
            return entity;
        }).toList();
        list.forEach(this::insert);
    }

    default void deleteListByUserIdAndRoleIdIds(Long userId, Collection<Long> roleIds) {
        delete(new LambdaQueryWrapperX<UserRole>().eq(UserRole::getUserId, userId)
                .in(UserRole::getRoleId, roleIds));
    }

    default void deleteListByUserId(Long userId) {
        delete(new LambdaQueryWrapperX<UserRole>().eq(UserRole::getUserId, userId));
    }

    default void deleteListByRoleId(Long roleId) {
        delete(new LambdaQueryWrapperX<UserRole>().eq(UserRole::getRoleId, roleId));
    }

}
