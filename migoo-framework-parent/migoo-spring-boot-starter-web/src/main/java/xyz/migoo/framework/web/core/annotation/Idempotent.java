package xyz.migoo.framework.web.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 幂等注解（防重复提交）
 * <p>
 * 标注在方法或类上，由 {@code IdempotentAspect} 通过 {@link xyz.migoo.framework.web.core.store.StateStore}
 * 原子占位实现防重：首个请求占位执行，防重窗口内的相同请求直接拒绝
 * （409 语义的业务码 900 {@code GlobalErrorCodeConstants.REPEATED_REQUESTS}）。
 * <p>
 * 失败语义与 MQ 幂等拦截器一致：<b>成功保留占位</b>（窗口内拒绝重复）、
 * <b>失败释放占位</b>（允许重试）；处理中按短占位窗口过期兜底（进程崩溃后可自动恢复）。
 * <p>
 * 幂等键格式 {@code migoo:idempotent:类名#方法名:维度值}，维度：
 * <ul>
 *     <li>{@link #key()} 留空（默认）：{@code 登录用户 + 可摘要参数的 SHA-256 摘要}，
 *         不同用户、不同入参互不影响；</li>
 *     <li>{@link #key()} 指定 SpEL：按表达式取值（支持 #参数名 / #p0 / #a0），
 *         跨实例稳定，需自行纳入用户维度时请在表达式中体现。</li>
 * </ul>
 * 参数摘要只覆盖 {@code @RequestBody} 参数与基本类型/字符串/枚举/时间等简单类型，
 * 自动跳过 HttpServletRequest、MultipartFile 等容器对象；全部参数不可摘要时
 * 退化为「登录用户 + 方法」级幂等（可配 {@link #key()} 精确化）。
 * <p>
 * 示例:
 * <pre>
 * // 默认维度：同一用户 60 秒内相同请求体只受理一次
 * {@code @Idempotent}
 * public Result createOrder(@RequestBody OrderCreateReqBody req) { ... }
 *
 * // 按业务单号幂等（SpEL）
 * {@code @Idempotent(key = "#orderId", expire = 300)}
 * public Result pay(String orderId) { ... }
 * </pre>
 * 总开关: {@code migoo.web.idempotent.enabled}（默认开启）。
 *
 * @author xiaomi
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /**
     * 自定义幂等键（SpEL 表达式）
     * <p>
     * 留空使用默认维度（登录用户 + 参数摘要）。支持方法参数名（编译已启用 -parameters）
     * 或 #p0/#a0 下标占位符。
     */
    String key() default "";

    /**
     * 防重窗口（秒）：首次成功后窗口内相同请求被拒绝，至少 1 秒
     */
    long expire() default 60L;

    /**
     * 重复请求的提示信息（留空使用全局 i18n 消息 common.repeat.request）
     */
    String message() default "";
}
