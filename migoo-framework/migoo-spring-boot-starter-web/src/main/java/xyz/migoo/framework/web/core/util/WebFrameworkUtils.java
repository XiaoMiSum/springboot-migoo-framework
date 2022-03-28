package xyz.migoo.framework.web.core.util;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import xyz.migoo.framework.common.pojo.Result;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * @author xiaomi
 * Created on 2021/11/21 13:09
 */
public class WebFrameworkUtils {

    private static final String REQUEST_ATTRIBUTE_LOGIN_USER_ID = "login_user_id";
    private static final String REQUEST_ATTRIBUTE_LOGIN_USER_NAME = "login_user_compound_name";

    private static final String REQUEST_ATTRIBUTE_RESULT = "result";

    public static void setLoginUserId(ServletRequest request, Long userId) {
        request.setAttribute(REQUEST_ATTRIBUTE_LOGIN_USER_ID, userId);
    }

    public static void setLoginUserName(ServletRequest request, String compoundName) {
        request.setAttribute(REQUEST_ATTRIBUTE_LOGIN_USER_NAME, compoundName);
    }

    /**
     * 获得当前用户的编号，从请求中
     *
     * @param request 请求
     * @return 用户编号
     */
    public static Long getLoginUserId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return (Long) request.getAttribute(REQUEST_ATTRIBUTE_LOGIN_USER_ID);
    }

    /**
     * 获得当前用户的类型，从请求中
     *
     * @param request 请求
     * @return 用户编号
     */
    public static String getLoginUserName(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return (String) request.getAttribute(REQUEST_ATTRIBUTE_LOGIN_USER_NAME);
    }

    public static Long getLoginUserId() {
        HttpServletRequest request = getRequest();
        return getLoginUserId(request);
    }

    public static String getLoginUserName() {
        HttpServletRequest request = getRequest();
        return getLoginUserName(request);
    }

    public static void setResult(ServletRequest request, Result<?> result) {
        request.setAttribute(REQUEST_ATTRIBUTE_RESULT, result);
    }

    public static Result<?> getCommonResult(ServletRequest request) {
        return (Result<?>) request.getAttribute(REQUEST_ATTRIBUTE_RESULT);
    }

    private static HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes)) {
            return null;
        }
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
        return servletRequestAttributes.getRequest();
    }
}
