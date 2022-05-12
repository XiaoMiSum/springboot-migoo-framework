package xyz.migoo.framework.quartz.core.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link JobHandlerDefine} 注册表
 *
 * @author xiaomi
 * Created on 2021/11/21 16:14
 */
public class JobHandlerRegistry {

    /**
     * JobHandlerDefine 数组
     */
    private static final List<JobHandlerDefine> DEFINES = new ArrayList<>();
    private static final Map<String, JobHandlerDefine> JOB_HANDLER_MAP = new HashMap<>(16);

    public static void add(JobHandlerDefine define) {
        DEFINES.add(define);
        JOB_HANDLER_MAP.put(define.getHandler(), define);
    }

    public static List<JobHandlerDefine> list() {
        return DEFINES;
    }

    public static Map<String, JobHandlerDefine> maps() {
        return JOB_HANDLER_MAP;
    }

    public static int size() {
        return DEFINES.size();
    }
}