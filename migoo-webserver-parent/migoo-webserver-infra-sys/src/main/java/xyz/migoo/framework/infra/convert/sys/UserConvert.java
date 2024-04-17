package xyz.migoo.framework.infra.convert.sys;

import xyz.migoo.framework.infra.controller.sys.user.vo.*;
import xyz.migoo.framework.infra.dal.dataobject.sys.Dept;
import xyz.migoo.framework.infra.dal.dataobject.sys.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface UserConvert {

    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    User convert(UserAddReqVO addReq);

    User convert(UserUpdateReqVO updateReq);

    User convert(UserPasswordReqVO reqVO);

    List<UserSimpleRespVO> convert(List<User> list);

    UserPageItemRespVO convert(User user);

    UserPageItemRespVO.Dept convert(Dept bean);
}
