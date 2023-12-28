package xyz.migoo.framework.cvs.core.client.dto;

public enum InstanceStatus {

    Creating,  // 准备中
    Pending,  // 准备中
    Running,  // 运行中
    Starting, // 启动中
    Deleting, // 删除中
    Rebooting, // 重启中
    Stopping, // 停止中
    Stopped,  // 已停止
    Released  // 已释放
    ;

    public static InstanceStatus valOf(int status) {
        switch (status) {
            case 0 -> {
                return Creating;
            }
            case 1 -> {
                return Running;
            }
            case 4, 5 -> {
                return Released;
            }
            default -> {
                return Pending;
            }
        }
    }

    public static InstanceStatus valOf(String status) {
        switch (status) {
            case "LAUNCH_FAILED", "STOPPED", "STOPPING" -> {
                return Stopped;
            }
            case "RUNNING", "STARTING", "REBOOTING" -> {
                return Running;
            }
            case "SHUTDOWN", "TERMINATING" -> {
                return Released;
            }
            default -> {
                return Pending;
            }
        }
    }
}
