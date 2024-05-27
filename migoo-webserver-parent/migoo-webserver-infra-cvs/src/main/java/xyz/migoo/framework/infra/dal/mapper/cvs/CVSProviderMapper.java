package xyz.migoo.framework.infra.dal.mapper.cvs;


import org.apache.ibatis.annotations.Mapper;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.infra.controller.cvs.vo.CVSProviderPageQueryReqVO;
import xyz.migoo.framework.infra.controller.cvs.vo.CVSProviderQueryReqVO;
import xyz.migoo.framework.infra.dal.dataobject.cvs.CVSProviderDO;
import xyz.migoo.framework.mybatis.core.BaseMapperX;
import xyz.migoo.framework.mybatis.core.LambdaQueryWrapperX;

import java.util.List;

@Mapper
public interface CVSProviderMapper extends BaseMapperX<CVSProviderDO> {

    default PageResult<CVSProviderDO> selectPage(CVSProviderPageQueryReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CVSProviderDO>()
                .eqIfPresent(CVSProviderDO::getCode, reqVO.getProvide())
                .likeIfPresent(CVSProviderDO::getAccount, reqVO.getAccount())
                .eqIfPresent(CVSProviderDO::getStatus, reqVO.getStatus()));
    }

    default List<CVSProviderDO> selectList(CVSProviderQueryReqVO req) {
        return selectList(new LambdaQueryWrapperX<CVSProviderDO>()
                .likeIfPresent(CVSProviderDO::getAccount, req.getAccount())
                .eq(CVSProviderDO::getStatus, req.getStatus()));
    }

    default CVSProviderDO selectOne(String account) {
        return selectOne(new LambdaQueryWrapperX<CVSProviderDO>()
                .ge(CVSProviderDO::getAccount, account));
    }
}
