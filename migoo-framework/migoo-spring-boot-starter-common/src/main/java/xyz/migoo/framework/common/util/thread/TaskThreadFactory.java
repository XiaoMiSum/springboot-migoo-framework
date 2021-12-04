package xyz.migoo.framework.common.util.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static xyz.migoo.framework.common.enums.NumberConstants.N_5;

/**
 * @author xiaomi
 * Created in 2021/7/21 22:30
 */
public class TaskThreadFactory implements ThreadFactory {
    final ThreadGroup group;
    final AtomicInteger threadNumber = new AtomicInteger(1);
    final String namePrefix;

    public TaskThreadFactory(String namePrefix) {
        SecurityManager s = System.getSecurityManager();
        this.group = s != null ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.namePrefix = namePrefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(this.group, r, this.namePrefix + this.threadNumber.getAndIncrement());
        t.setDaemon(true);
        if (t.getPriority() != N_5) {
            t.setPriority(N_5);
        }
        return t;
    }
}