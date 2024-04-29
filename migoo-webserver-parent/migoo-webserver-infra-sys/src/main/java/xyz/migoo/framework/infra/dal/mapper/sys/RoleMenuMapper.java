package xyz.migoo.framework.infra.dal.mapper.sys;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import xyz.migoo.framework.infra.dal.dataobject.sys.RoleMenu;
import xyz.migoo.framework.mybatis.core.BaseMapperX;
import xyz.migoo.framework.mybatis.core.LambdaQueryWrapperX;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Mapper
public interface RoleMenuMapper extends BaseMapperX<RoleMenu> {

    default List<RoleMenu> selectListByRoleId(Long roleId) {
        return selectList(new LambdaQueryWrapperX<RoleMenu>().eq(RoleMenu::getRoleId, roleId));
    }

    default void insertList(Long roleId, Collection<Long> menuIds) {
        List<RoleMenu> list = menuIds.stream().map(menuId -> {
            RoleMenu entity = new RoleMenu();
            entity.setRoleId(roleId);
            entity.setMenuId(menuId);
            return entity;
        }).toList();
        list.forEach(this::insert);
    }

    default void deleteListByRoleIdAndMenuIds(Long roleId, Collection<Long> menuIds) {
        delete(new LambdaQueryWrapperX<RoleMenu>().eq(RoleMenu::getRoleId, roleId)
                .in(RoleMenu::getMenuId, menuIds));
    }

    default void deleteListByMenuId(Long menuId) {
        delete(new LambdaQueryWrapperX<RoleMenu>().eq(RoleMenu::getMenuId, menuId));
    }

    default void deleteListByRoleId(Long roleId) {
        delete(new LambdaQueryWrapperX<RoleMenu>().eq(RoleMenu::getRoleId, roleId));
    }

    @Select("SELECT id FROM sys_role_menu WHERE update_time > #{maxUpdateTime} LIMIT 1")
    Long selectExistsByUpdateTimeAfter(LocalDateTime maxUpdateTime);

}
