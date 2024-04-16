package xyz.migoo.franework.infra.service.developer.errorlog;

import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.franework.infra.controller.developer.errorlog.vo.ApiErrorLogQueryReqVO;
import xyz.migoo.franework.infra.dal.dataobject.developer.errorlog.ApiErrorLogDO;

public interface ErrorLogService {

    PageResult<ApiErrorLogDO> getPage(ApiErrorLogQueryReqVO req);

    void update(ApiErrorLogDO log);

    void remove(Long id);

    ApiErrorLogDO get(Long id);
}
