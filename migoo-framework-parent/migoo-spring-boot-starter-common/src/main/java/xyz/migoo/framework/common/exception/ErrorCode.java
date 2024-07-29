package xyz.migoo.framework.common.exception;

import com.google.common.collect.Maps;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import xyz.migoo.framework.common.core.KeyValue;
import xyz.migoo.framework.common.exception.enums.GlobalErrorCodeConstants;
import xyz.migoo.framework.common.exception.enums.ServiceErrorCodeRange;
import xyz.migoo.framework.common.util.servlet.ServletUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;

/**
 * 错误码对象
 * <p>
 * 全局错误码，占用 [0, 999], 参见 {@link GlobalErrorCodeConstants}
 * 业务异常错误码，占用 [1 000 000 000, +∞)，参见 {@link ServiceErrorCodeRange}
 */
@Data
public class ErrorCode {

    public static final Map<Integer, ErrorCode> message = Maps.newConcurrentMap();
    /**
     * 错误码
     */
    private final Integer code;

    private final Map<String, String> messages = Maps.newConcurrentMap();

    public ErrorCode(Integer code, String message) {
        this.code = code;
        this.messages.put("zh-CN", message);
    }

    private ErrorCode(Integer code) {
        this.code = code;
    }

    @SafeVarargs
    public static ErrorCode of(Integer code, KeyValue<String, String>... messages) {
        ErrorCode e = new ErrorCode(code);
        Arrays.stream(messages).forEach(i18n -> e.messages.put(i18n.getKey(), i18n.getValue()));
        return e;
    }

    public static ErrorCode of(Integer code, String msg) {
        return new ErrorCode(code, msg);
    }

    public static String getLocalMessage(Integer code, String locale) {
        return message.get(code).getMessage(locale);
    }

    public static String getLocalMessage(Integer code, HttpServletRequest request) {
        return message.get(code).getMessage(request);
    }

    public static void put(ErrorCode... errorCode) {
        Arrays.stream(errorCode).forEach(item -> message.put(item.getCode(), item));
    }

    public static void put(Class<?> clazz) throws RuntimeException {
        try {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                int mod = field.getModifiers();
                if (Modifier.isStatic(mod) && Modifier.isFinal(mod)) {
                    Object o = field.get(clazz);
                    if (o instanceof ErrorCode errorCode) {
                        put(errorCode);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getMessage(HttpServletRequest request) {
        return getMessage(ServletUtils.getLocale(request));
    }

    public String getMessage(String locale) {
        return messages.get(locale);
    }

    public String getMsg() {
        return getMessage("zh-CN");
    }

}
