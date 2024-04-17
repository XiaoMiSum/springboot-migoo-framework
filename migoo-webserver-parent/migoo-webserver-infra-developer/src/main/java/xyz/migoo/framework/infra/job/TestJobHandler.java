package xyz.migoo.framework.infra.job;

import org.springframework.stereotype.Component;
import xyz.migoo.framework.quartz.core.handler.JobHandler;

@Component
public class TestJobHandler implements JobHandler {
    @Override
    public String execute(String param, Long jobLogId) throws Exception {
        return "success";
    }
}
