package xyz.migoo.framework.quartz.core.registry;

import lombok.Data;
import xyz.migoo.framework.common.util.string.StrUtils;
import xyz.migoo.framework.quartz.core.handler.JobHandler;

/**
 * Redis Key 定义类
 *
 * @author xiaomi
 * Created on 2021/11/21 16:12
 */
@Data
public class JobHandlerDefine {

    /**
     * 任务名称
     */
    private final String title;
    /**
     * handler 类型
     */
    private final String handler;
    /**
     * 备注
     */
    private final String memo;


    private JobHandlerDefine(String title, String handler, String memo) {
        this.title = title;
        this.handler = handler;
        this.memo = memo;
        // 添加注册表
        JobHandlerRegistry.add(this);
    }

    private JobHandlerDefine(String title, String handler) {
        this(title, handler, "");
    }

    private JobHandlerDefine(String title, Class<? extends JobHandler> handler, String memo) {
        this(title, StrUtils.firstLetter2Lower(handler.getSimpleName()), memo);
    }

    private JobHandlerDefine(String title, Class<? extends JobHandler> handler) {
        this(title, handler, "");
    }

}



