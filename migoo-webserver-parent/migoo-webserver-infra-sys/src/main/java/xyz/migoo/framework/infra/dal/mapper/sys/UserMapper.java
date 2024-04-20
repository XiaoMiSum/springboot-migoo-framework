package xyz.migoo.framework.infra.dal.mapper.sys;

import org.apache.ibatis.annotations.Mapper;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.infra.controller.sys.user.vo.UserQueryReqVO;
import xyz.migoo.framework.infra.dal.dataobject.sys.User;
import xyz.migoo.framework.mybatis.core.BaseMapperX;
import xyz.migoo.framework.mybatis.core.LambdaQueryWrapperX;
import xyz.migoo.framework.mybatis.core.dataobject.BaseDO;
import xyz.migoo.framework.security.core.BaseUser;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapperX<User> {


    default User selectByUsername(String username) {
        return selectOne(new LambdaQueryWrapperX<User>()
                .eq(User::getUsername, username));
    }

    default PageResult<User> selectPage(UserQueryReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<User>()
                .likeIfPresent(User::getPhone, reqVO.getPhone())
                .likeIfPresent(BaseUser::getName, reqVO.getName())
                .eqIfPresent(User::getDeptId, reqVO.getDeptId())
                .eqIfPresent(BaseUser::getStatus, reqVO.getStatus())
                .gt(BaseDO::getId, 1)
                .orderByDesc(BaseDO::getId));
    }

    default List<User> selectList(Integer status) {
        return selectList(new LambdaQueryWrapperX<User>()
                .eqIfPresent(BaseUser::getStatus, status)
                .orderByDesc(BaseDO::getId));
    }
}
