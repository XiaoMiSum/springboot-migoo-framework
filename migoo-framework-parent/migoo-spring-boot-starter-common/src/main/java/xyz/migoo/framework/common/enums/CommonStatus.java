package xyz.migoo.framework.common.enums;

public record CommonStatus(int status) {

    public static CommonStatus enabled = new CommonStatus(1);

    public static CommonStatus disabled = new CommonStatus(0);

    public static boolean isEnabled(CommonStatus status) {
        return isEnabled(status.status());
    }


    public static boolean isDisabled(CommonStatus status) {
        return isDisabled(status.status());
    }

    public static boolean isEnabled(int status) {
        return enabled.status() == status;
    }

    public static boolean isDisabled(int status) {
        return !isEnabled(status);
    }
}
