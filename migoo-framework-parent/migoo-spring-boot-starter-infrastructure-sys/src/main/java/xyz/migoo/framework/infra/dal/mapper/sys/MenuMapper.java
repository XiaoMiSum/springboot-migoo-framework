package xyz.migoo.framework.infra.dal.mapper.sys;

import xyz.migoo.framework.infra.controller.sys.permission.menu.vo.MenuQueryReqVO;
import xyz.migoo.framework.infra.dal.dataobject.IdEnhanceDO;
import xyz.migoo.framework.infra.dal.dataobject.sys.Menu;
import org.apache.ibatis.annotations.Mapper;
import xyz.migoo.framework.mybatis.core.BaseMapperX;
import xyz.migoo.framework.mybatis.core.LambdaQueryWrapperX;
import xyz.migoo.framework.mybatis.core.dataobject.BaseDO;

import java.util.Date;
import java.util.List;

@Mapper
public interface MenuMapper extends BaseMapperX<Menu> {

    default Menu selectByParentIdAndName(Long parentId, String name) {
        return selectOne(new LambdaQueryWrapperX<Menu>()
                .eq(Menu::getParentId, parentId)
                .eq(Menu::getName, name));
    }

    default Long selectCountByParentId(Long parentId) {
        return selectCount(new LambdaQueryWrapperX<Menu>().eq(Menu::getParentId, parentId));
    }

    default List<Menu> selectList(String name, Integer status) {
        return selectList(new LambdaQueryWrapperX<Menu>().likeIfPresent(Menu::getName, name)
                .eqIfPresent(Menu::getStatus, status));
    }

    default boolean selectExistsByUpdateTimeAfter(Date maxUpdateTime) {
        return selectOne(new LambdaQueryWrapperX<Menu>().select(IdEnhanceDO::getId)
                .gt(BaseDO::getUpdateTime, maxUpdateTime).last("LIMIT 1")) != null;
    }

    default List<Menu> selectList(MenuQueryReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<Menu>().likeIfPresent(Menu::getName, reqVO.getName())
                .eqIfPresent(Menu::getStatus, reqVO.getStatus()));
    }

}
