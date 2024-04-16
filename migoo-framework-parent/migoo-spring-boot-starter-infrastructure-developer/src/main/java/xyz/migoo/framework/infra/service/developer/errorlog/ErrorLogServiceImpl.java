package xyz.migoo.franework.infra.service.developer.errorlog;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.migoo.framework.apilog.core.ApiErrorLog;
import xyz.migoo.framework.apilog.core.ApiErrorLogFrameworkService;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.franework.infra.controller.developer.errorlog.vo.ApiErrorLogQueryReqVO;
import xyz.migoo.franework.infra.convert.developer.errorlog.ErrorLogConvert;
import xyz.migoo.franework.infra.dal.dataobject.developer.errorlog.ApiErrorLogDO;
import xyz.migoo.franework.infra.dal.mapper.developer.errorlog.ErrorLogMapper;

@Service
public class ErrorLogServiceImpl implements ErrorLogService, ApiErrorLogFrameworkService {

    @Resource
    private ErrorLogMapper mapper;

    @Override
    public void createApiErrorLog(ApiErrorLog apiErrorLog) {
        mapper.insert(ErrorLogConvert.INSTANCE.convert(apiErrorLog));
    }

    @Override
    public PageResult<ApiErrorLogDO> getPage(ApiErrorLogQueryReqVO req) {
        return mapper.selectPage(req);
    }

    @Override
    public void update(ApiErrorLogDO log) {
        mapper.updateById(log);
    }

    @Override
    public void remove(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public ApiErrorLogDO get(Long id) {
        return mapper.selectById(id);
    }
}
