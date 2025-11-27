package xyz.migoo.framework.common.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.util.Assert;
import xyz.migoo.framework.common.exception.ErrorCode;
import xyz.migoo.framework.common.exception.ServiceException;
import xyz.migoo.framework.common.exception.enums.GlobalErrorCodeConstants;

import java.io.Serializable;
import java.util.Objects;

/**
 * 通用返回
 *
 * @param <T> 数据泛型
 */
@Data
public class Result<T> implements Serializable {

    /**
     * 错误码
     *
     * @see ErrorCode#code()
     */
    private Integer code;
    /**
     * 返回数据
     */
    private T data;
    /**
     * 错误提示，用户可阅读
     *
     * @see ErrorCode#msg() ()
     */
    private String msg;

    /**
     * 将传入的 result 对象，转换成另外一个泛型结果的对象
     * <p>
     * 因为 A 方法返回的 CommonResult 对象，不满足调用其的 B 方法的返回，所以需要进行转换。
     *
     * @param result 传入的 result 对象
     * @param <T>    返回的泛型
     * @return 新的 CommonResult 对象
     */
    public static <T> Result<T> getError(Result<?> result) {
        return getError(result.getCode(), result.getMsg());
    }

    public static <T> Result<T> getError(Integer code, String message) {
        Assert.isTrue(!GlobalErrorCodeConstants.SUCCESS.code().equals(code), "code 必须是错误的！");
        Result<T> result = new Result<>();
        result.code = code;
        result.msg = message;
        return result;
    }

    public static <T> Result<T> getError(ErrorCode errorCode) {
        return getError(errorCode.code(), errorCode.msg());
    }

    public static <T> Result<T> getSuccessful(T data) {
        Result<T> result = new Result<>();
        result.code = GlobalErrorCodeConstants.SUCCESS.code();
        result.msg = GlobalErrorCodeConstants.SUCCESS.msg();
        result.data = data;
        return result;
    }

    public static <T> Result<T> getSuccessful() {
        return getSuccessful(null);
    }

    public static boolean isSuccessful(Integer code) {
        return Objects.equals(code, GlobalErrorCodeConstants.SUCCESS.code());
    }

    public static <T> Result<T> getError(ServiceException serviceException) {
        return getError(serviceException.getCode(), serviceException.getMessage());
    }

    @JsonIgnore // 避免 jackson 序列化
    public boolean isSuccessful() {
        return isSuccessful(code);
    }

    // ========= 和 Exception 异常体系集成 =========

    @JsonIgnore // 避免 jackson 序列化
    public boolean isError() {
        return !isSuccessful();
    }

    /**
     * 判断是否有异常。如果有，则抛出 {@link ServiceException} 异常
     */
    public void checkError() throws ServiceException {
        if (!isSuccessful()) {
            // 业务异常
            throw new ServiceException(code, msg);
        }
    }

}
