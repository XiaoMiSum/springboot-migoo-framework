package xyz.migoo.framework.infra.convert.developer.errorlog;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import xyz.migoo.framework.apilog.core.ApiErrorLog;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.infra.controller.developer.errorlog.vo.ApiErrorLogPageRespVO;
import xyz.migoo.framework.infra.controller.developer.errorlog.vo.ApiErrorLogRespVO;
import xyz.migoo.framework.infra.controller.developer.errorlog.vo.ApiErrorLogUpdateVO;
import xyz.migoo.framework.infra.dal.dataobject.developer.errorlog.ApiErrorLogDO;

@Mapper
public interface ErrorLogConvert {

    ErrorLogConvert INSTANCE = Mappers.getMapper(ErrorLogConvert.class);

    ApiErrorLogDO convert(ApiErrorLog bean);

    PageResult<ApiErrorLogPageRespVO> convert(PageResult<ApiErrorLogDO> bean);

    ApiErrorLogRespVO convert(ApiErrorLogDO bean);

    ApiErrorLogDO convert(ApiErrorLogUpdateVO bean);
}
