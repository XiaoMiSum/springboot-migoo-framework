package xyz.migoo.framework.common.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 异常处理工具类
 *
 * @author migoo
 * @since 1.3.16
 */
public final class ExceptionUtils {

    private ExceptionUtils() {
    }

    /**
     * 获取异常的完整堆栈信息
     */
    public static String stacktraceToString(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    /**
     * 获取异常的简短信息（类名 + 消息）
     */
    public static String getMessage(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        String message = throwable.getMessage();
        if (message == null || message.isEmpty()) {
            return throwable.getClass().getSimpleName();
        }
        return throwable.getClass().getSimpleName() + ": " + message;
    }

    /**
     * 获取根本原因异常的简短信息
     */
    public static String getRootCauseMessage(Throwable throwable) {
        Throwable rootCause = getRootCause(throwable);
        return getMessage(rootCause);
    }

    /**
     * 获取根本原因异常
     */
    public static Throwable getRootCause(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        Throwable cause = throwable.getCause();
        if (cause == null) {
            return throwable;
        }
        return getRootCause(cause);
    }

    /**
     * 获取根本原因异常（带循环引用保护）
     */
    public static Throwable getRootCause(Throwable throwable, int maxDepth) {
        if (throwable == null) {
            return null;
        }
        Throwable cause = throwable.getCause();
        if (cause == null || maxDepth <= 0) {
            return throwable;
        }
        return getRootCause(cause, maxDepth - 1);
    }

    /**
     * 判断异常链中是否包含指定类型的异常
     */
    public static boolean isCausedBy(Throwable throwable, Class<? extends Throwable>... causeTypes) {
        if (throwable == null) {
            return false;
        }
        for (Class<? extends Throwable> causeType : causeTypes) {
            if (causeType.isInstance(throwable)) {
                return true;
            }
            Throwable cause = throwable.getCause();
            while (cause != null) {
                if (causeType.isInstance(cause)) {
                    return true;
                }
                cause = cause.getCause();
            }
        }
        return false;
    }
}
