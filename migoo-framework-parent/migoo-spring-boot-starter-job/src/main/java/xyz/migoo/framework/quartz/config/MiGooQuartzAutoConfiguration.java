package xyz.migoo.framework.quartz.config;

import org.quartz.Scheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import xyz.migoo.framework.quartz.core.scheduler.SchedulerManager;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableScheduling // 开启 Spring 自带的定时任务
public class MiGooQuartzAutoConfiguration implements SchedulingConfigurer {

    @Bean
    public SchedulerManager schedulerManager(Scheduler scheduler) {
        return new SchedulerManager(scheduler);
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskExecutor());
    }

    @Bean
    public Executor taskExecutor() {
        return Executors.newScheduledThreadPool(5);
    }
}
