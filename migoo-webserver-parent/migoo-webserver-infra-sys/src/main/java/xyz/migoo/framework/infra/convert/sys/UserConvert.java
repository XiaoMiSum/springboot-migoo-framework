package xyz.migoo.framework.infra.convert.sys;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import xyz.migoo.framework.common.pojo.SimpleData;
import xyz.migoo.framework.infra.controller.sys.user.vo.UserAddReqVO;
import xyz.migoo.framework.infra.controller.sys.user.vo.UserPageItemRespVO;
import xyz.migoo.framework.infra.controller.sys.user.vo.UserPasswordReqVO;
import xyz.migoo.framework.infra.controller.sys.user.vo.UserUpdateReqVO;
import xyz.migoo.framework.infra.dal.dataobject.sys.Dept;
import xyz.migoo.framework.infra.dal.dataobject.sys.User;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserConvert {

    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    User convert(UserAddReqVO addReq);

    User convert(UserUpdateReqVO updateReq);

    User convert(UserPasswordReqVO reqVO);

    default SimpleData convert1(User user) {
        return new SimpleData(user.getId(), user.getName(), user.getStatus(),
                Map.of("post", user.getPostId()));
    }

    List<SimpleData> convert(List<User> list);

    UserPageItemRespVO convert(User user);

    UserPageItemRespVO.Dept convert(Dept bean);
}
