package xyz.migoo.framework.quartz.config;

import org.quartz.Scheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import xyz.migoo.framework.quartz.core.scheduler.SchedulerManager;

@Configuration
@EnableScheduling // 开启 Spring 自带的定时任务
public class MiGooQuartzAutoConfiguration {

    @Bean
    public SchedulerManager schedulerManager(Scheduler scheduler) {
        return new SchedulerManager(scheduler);
    }

}
