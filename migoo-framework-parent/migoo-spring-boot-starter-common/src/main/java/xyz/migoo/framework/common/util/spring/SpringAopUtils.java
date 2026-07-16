package xyz.migoo.framework.common.util.spring;

import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.support.AopUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * Spring AOP 工具类
 * <p>
 * 参考波克尔 http://www.bubuko.com/infodetail-3471885.html 实现
 */
public class SpringAopUtils {

    /**
     * 获取代理的目标对象
     *
     * @param proxy 代理对象
     * @return 目标对象
     */
    public static Object getTarget(Object proxy) throws Exception {
        // 不是代理对象
        if (!AopUtils.isAopProxy(proxy)) {
            return proxy;
        }
        // Jdk 代理
        if (AopUtils.isJdkDynamicProxy(proxy)) {
            return getJdkDynamicProxyTargetObject(proxy);
        }
        // Cglib 代理
        return getCglibProxyTargetObject(proxy);
    }

    private static Object getCglibProxyTargetObject(Object proxy) throws Exception {
        Object dynamicAdvisedInterceptor = getFieldValue(proxy, "CGLIB$CALLBACK_0");
        AdvisedSupport advisedSupport = (AdvisedSupport) getFieldValue(dynamicAdvisedInterceptor, "advised");
        return advisedSupport.getTargetSource().getTarget();
    }

    private static Object getJdkDynamicProxyTargetObject(Object proxy) throws Exception {
        AopProxy aopProxy = (AopProxy) getFieldValue(proxy, "h");
        AdvisedSupport advisedSupport = (AdvisedSupport) getFieldValue(aopProxy, "advised");
        return advisedSupport.getTargetSource().getTarget();
    }

    /**
     * 反射获取字段值
     */
    private static Object getFieldValue(Object obj, String fieldName) {
        Class<?> clazz = obj.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(obj);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Field not found: " + fieldName);
    }

}
