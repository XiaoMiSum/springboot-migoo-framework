package xyz.migoo.framework.infra.convert.sys;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.infra.controller.sys.permission.role.vo.RoleAddReqVO;
import xyz.migoo.framework.infra.controller.sys.permission.role.vo.RoleRespVO;
import xyz.migoo.framework.infra.controller.sys.permission.role.vo.RoleSimpleRespVO;
import xyz.migoo.framework.infra.controller.sys.permission.role.vo.RoleUpdateReqVO;
import xyz.migoo.framework.infra.dal.dataobject.sys.Role;

import java.util.List;

@Mapper
public interface RoleConvert {

    RoleConvert INSTANCE = Mappers.getMapper(RoleConvert.class);

    Role convert(RoleAddReqVO reqVO);

    Role convert(RoleUpdateReqVO reqVO);

    RoleRespVO convert(Role role);

    List<RoleSimpleRespVO> convert(List<Role> list);

    PageResult<RoleRespVO> convert(PageResult<Role> page);
}
