package xyz.migoo.framework.infra.dal.mapper.sys;

import xyz.migoo.framework.infra.controller.sys.user.vo.UserQueryReqVO;
import xyz.migoo.framework.infra.dal.dataobject.BaseUser;
import xyz.migoo.framework.infra.dal.dataobject.IdEnhanceDO;
import xyz.migoo.framework.infra.dal.dataobject.sys.User;
import org.apache.ibatis.annotations.Mapper;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.mybatis.core.BaseMapperX;
import xyz.migoo.framework.mybatis.core.LambdaQueryWrapperX;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapperX<User> {


    default User selectByPhone(String phone) {
        return selectOne(new LambdaQueryWrapperX<User>()
                .eq(User::getPhone, phone));
    }

    default PageResult<User> selectPage(UserQueryReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<User>()
                .likeIfPresent(User::getPhone, reqVO.getPhone())
                .likeIfPresent(BaseUser::getName, reqVO.getName())
                .eqIfPresent(User::getDeptId, reqVO.getDeptId())
                .eqIfPresent(BaseUser::getStatus, reqVO.getStatus())
                .gt(IdEnhanceDO::getId, 1)
                .orderByDesc(IdEnhanceDO::getId));
    }

    default List<User> selectList(Integer status) {
        return selectList(new LambdaQueryWrapperX<User>()
                .eqIfPresent(BaseUser::getStatus, status)
                .orderByDesc(IdEnhanceDO::getId));
    }
}
