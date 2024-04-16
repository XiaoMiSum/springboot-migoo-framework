package xyz.migoo.framework.infra.convert.sys;

import xyz.migoo.framework.infra.controller.sys.permission.menu.vo.MenuAddReqVO;
import xyz.migoo.framework.infra.controller.sys.permission.menu.vo.MenuRespVO;
import xyz.migoo.framework.infra.controller.sys.permission.menu.vo.MenuSimpleRespVO;
import xyz.migoo.framework.infra.controller.sys.permission.menu.vo.MenuUpdateReqVO;
import xyz.migoo.framework.infra.dal.dataobject.sys.Menu;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface MenuConvert {
    MenuConvert INSTANCE = Mappers.getMapper(MenuConvert.class);

    List<MenuRespVO> convert(List<Menu> list);

    List<MenuSimpleRespVO> convert0(List<Menu> list);

    MenuRespVO convert(Menu menu);

    Menu convert(MenuAddReqVO addReq);

    Menu convert(MenuUpdateReqVO updateReq);
}
