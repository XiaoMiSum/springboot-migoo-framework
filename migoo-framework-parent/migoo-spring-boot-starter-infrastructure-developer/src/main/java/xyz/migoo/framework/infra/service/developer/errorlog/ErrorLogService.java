package xyz.migoo.framework.infra.service.developer.errorlog;

import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.infra.controller.developer.errorlog.vo.ApiErrorLogQueryReqVO;
import xyz.migoo.framework.infra.dal.dataobject.developer.errorlog.ApiErrorLogDO;

public interface ErrorLogService {

    PageResult<ApiErrorLogDO> getPage(ApiErrorLogQueryReqVO req);

    void update(ApiErrorLogDO log);

    void remove(Long id);

    ApiErrorLogDO get(Long id);
}
