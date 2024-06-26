package xyz.migoo.framework.infra.dal.dataobject.developer.job;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import xyz.migoo.framework.infra.enums.JobLogStatusEnum;
import xyz.migoo.framework.mybatis.core.dataobject.BaseDO;
import xyz.migoo.framework.quartz.core.handler.JobHandler;

import java.time.LocalDateTime;

@TableName("infra_job_log")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class JobLogDO extends BaseDO<Long> {

    /**
     * 日志编号
     */
    private Long id;
    /**
     * 任务编号
     * <p>
     * 关联 {@link JobDO#getId()}
     */
    private Long jobId;
    /**
     * 处理器的名字
     * <p>
     * 冗余字段 {@link JobDO#getHandlerName()}
     */
    private String handlerName;
    /**
     * 处理器的参数
     * <p>
     * 冗余字段 {@link JobDO#getHandlerParam()}
     */
    private String handlerParam;
    /**
     * 第几次执行
     * <p>
     * 用于区分是不是重试执行。如果是重试执行，则 index 大于 1
     */
    private Integer executeIndex;

    /**
     * 开始执行时间
     */
    private LocalDateTime beginTime;
    /**
     * 结束执行时间
     */
    private LocalDateTime endTime;
    /**
     * 执行时长，单位：毫秒
     */
    private Integer duration;
    /**
     * 状态
     * <p>
     * 枚举 {@link JobLogStatusEnum}
     */
    private Integer status;
    /**
     * 结果数据
     * <p>
     * 成功时，使用 {@link JobHandler#execute(String, Long)} 的结果
     * 失败时，使用 {@link JobHandler#execute(String, Long)} 的异常堆栈
     */
    private String result;

}
