package xyz.migoo.framework.infra;

import org.springframework.stereotype.Component;
import xyz.migoo.framework.apilog.core.ApiErrorLog;
import xyz.migoo.framework.apilog.core.ApiErrorLogFrameworkService;

@Component
public class ApiErrorLogFrameworkServiceImpl implements ApiErrorLogFrameworkService {
    @Override
    public void createApiErrorLog(ApiErrorLog apiErrorLog) {

    }
}
