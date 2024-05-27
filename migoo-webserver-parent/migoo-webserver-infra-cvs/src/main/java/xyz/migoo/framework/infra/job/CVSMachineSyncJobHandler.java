package xyz.migoo.framework.infra.job;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.migoo.framework.infra.service.cvs.CVSMachineService;
import xyz.migoo.framework.quartz.core.handler.JobHandler;

@Component
public class CVSMachineSyncJobHandler implements JobHandler {
    @Resource
    private CVSMachineService service;

    @Override
    public String execute(String param, Long jobLogId) throws Exception {
        service.sync();
        return "success";
    }
}
