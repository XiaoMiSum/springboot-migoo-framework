package xyz.migoo.framework.infra.dal.mapper.developer.errorlog;

import org.apache.ibatis.annotations.Mapper;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.mybatis.core.BaseMapperX;
import xyz.migoo.framework.mybatis.core.LambdaQueryWrapperX;
import xyz.migoo.framework.infra.controller.developer.errorlog.vo.ApiErrorLogQueryReqVO;
import xyz.migoo.framework.infra.dal.dataobject.developer.errorlog.ApiErrorLogDO;

@Mapper
public interface ErrorLogMapper extends BaseMapperX<ApiErrorLogDO> {

    default PageResult<ApiErrorLogDO> selectPage(ApiErrorLogQueryReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ApiErrorLogDO>()
                .eqIfPresent(ApiErrorLogDO::getApplicationName, reqVO.getApplicationName())
                .eqIfPresent(ApiErrorLogDO::getStatus, reqVO.getStatus())
                .orderByAsc(ApiErrorLogDO::getStatus));
    }
}
