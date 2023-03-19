package xyz.migoo.framework.quartz.core.handler;

/**
 * 任务处理器
 *
 * @author xiaomi
 */
public interface JobHandler {

    /**
     * 执行任务
     *
     * @param param    参数
     * @param jobLogId 任务调度日志Id
     * @return 结果
     * @throws Exception 异常
     */
    String execute(String param, Long jobLogId) throws Exception;

}
