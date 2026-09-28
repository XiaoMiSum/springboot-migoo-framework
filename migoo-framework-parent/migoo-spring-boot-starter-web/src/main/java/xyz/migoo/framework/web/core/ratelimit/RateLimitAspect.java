package xyz.migoo.framework.web.core.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import xyz.migoo.framework.common.exception.GlobalErrorCodeConstants;
import xyz.migoo.framework.common.exception.ServiceExceptionUtil;
import xyz.migoo.framework.common.observability.RateLimitExceededEvent;
import xyz.migoo.framework.web.core.annotation.RateLimit;
import xyz.migoo.framework.web.core.annotation.RateLimitType;
import xyz.migoo.framework.web.core.util.ServletUtils;
import xyz.migoo.framework.web.core.util.WebFrameworkUtils;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 限流切面
 * <p>
 * 拦截标注了 {@link RateLimit} 的方法/类，按固定窗口计数限流，
 * 超出限制抛出 429（{@link GlobalErrorCodeConstants#TOO_MANY_REQUESTS}）。
 * <p>
 * 统计键: {@code 类名#方法名:维度值}，维度支持 IP、登录用户、自定义 SpEL key。
 *
 * @author xiaomi
 */
@Aspect
@Slf4j
public class RateLimitAspect {

    /**
     * SpEL 表达式解析器
     */
    private static final ExpressionParser PARSER = new SpelExpressionParser();

    /**
     * 方法参数名解析器（支持 #p0/#a0 下标与参数名占位符）
     */
    private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    private final RateLimiter rateLimiter;

    /**
     * 事件发布器（可观测性信号，可空：直连构造时允许不发布）
     */
    private final ApplicationEventPublisher eventPublisher;

    /**
     * SpEL 表达式缓存（key: 方法 + 表达式）
     */
    private final Map<String, Expression> expressionCache = new ConcurrentHashMap<>();

    public RateLimitAspect(RateLimiter rateLimiter) {
        this(rateLimiter, null);
    }

    public RateLimitAspect(RateLimiter rateLimiter, ApplicationEventPublisher eventPublisher) {
        this.rateLimiter = rateLimiter;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 限流通知
     * <p>
     * Spring 7 起，复合切点（{@code ||}）不再回填注解参数绑定，形参绑定会拿到 {@code null}，
     * 故此处不绑定注解、改由 {@link #resolveRateLimit(ProceedingJoinPoint)} 在方法体内解析；
     * 切点表达式与「方法注解 / 类注解」语义保持不变。
     *
     * @param joinPoint 被切方法
     * @return 被切方法（或其所在类）上的 {@link RateLimit} 注解
     */
    @Around(value = "@annotation(xyz.migoo.framework.web.core.annotation.RateLimit)"
            + " || @within(xyz.migoo.framework.web.core.annotation.RateLimit)",
            argNames = "joinPoint")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        RateLimit rateLimit = resolveRateLimit(joinPoint);
        String fullKey = buildMethodKey(joinPoint) + ":" + buildDimensionKey(rateLimit, joinPoint);
        boolean acquired = rateLimiter.tryAcquire(fullKey, rateLimit.limit(),
                Duration.ofSeconds(Math.max(rateLimit.window(), 1)));
        if (!acquired) {
            log.warn("[RateLimitAspect][限流拦截] key({}) limit({}) window({}s)", fullKey,
                    rateLimit.limit(), rateLimit.window());
            publishRateLimitExceeded(rateLimit);
            throw rateLimit.message().isBlank()
                    ? ServiceExceptionUtil.get(GlobalErrorCodeConstants.TOO_MANY_REQUESTS)
                    : ServiceExceptionUtil.get(GlobalErrorCodeConstants.TOO_MANY_REQUESTS.code(), rateLimit.message());
        }
        return joinPoint.proceed();
    }

    /**
     * 解析被切方法的 {@link RateLimit}：方法注解优先，未标注时取所在类（{@code @within} 语义）。
     * <p>
     * 经 {@link AopUtils#getMostSpecificMethod} 先定位实现类方法，避免 JDK 动态代理下
     * 只看到接口方法而漏掉实现上的注解。
     */
    private RateLimit resolveRateLimit(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object target = joinPoint.getTarget();
        Class<?> targetClass = target != null ? target.getClass() : method.getDeclaringClass();
        Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);

        RateLimit rateLimit = AnnotationUtils.findAnnotation(specificMethod, RateLimit.class);
        if (rateLimit == null && target != null) {
            rateLimit = AnnotationUtils.findAnnotation(targetClass, RateLimit.class);
        }
        if (rateLimit == null) {
            // 切点已匹配却解析不到注解，属内部不一致：快速失败便于定位，不静默放行
            throw new IllegalStateException("@RateLimit 切点匹配但未解析到注解: " + joinPoint.getSignature());
        }
        return rateLimit;
    }

    /**
     * 发布限流拒绝事件（观测不得影响业务：发布异常只记日志）
     */
    private void publishRateLimitExceeded(RateLimit rateLimit) {
        if (eventPublisher == null) {
            return;
        }
        try {
            eventPublisher.publishEvent(new RateLimitExceededEvent(ServletUtils.getRoutePattern(),
                    rateLimit.type().name().toLowerCase(), rateLimit.limit()));
        } catch (Exception ex) {
            log.warn("[publishRateLimitExceeded][发布限流拒绝事件失败]", ex);
        }
    }

    /**
     * 构建方法级 key 前缀: 类名#方法名
     */
    private String buildMethodKey(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        return method.getDeclaringClass().getSimpleName() + "#" + method.getName();
    }

    /**
     * 构建维度 key
     */
    private String buildDimensionKey(RateLimit rateLimit, ProceedingJoinPoint joinPoint) {
        return switch (rateLimit.type()) {
            case IP -> Objects.requireNonNullElse(ServletUtils.getClientIP(), "unknown");
            case USER -> {
                Object userId = WebFrameworkUtils.getLoginUserId();
                yield userId != null ? String.valueOf(userId) : "anonymous";
            }
            case KEY -> evaluateKey(rateLimit, joinPoint);
        };
    }

    /**
     * 求值自定义 SpEL key
     */
    private String evaluateKey(RateLimit rateLimit, ProceedingJoinPoint joinPoint) {
        if (rateLimit.key() == null || rateLimit.key().isBlank()) {
            throw new IllegalStateException(
                    "@RateLimit(type = KEY) 必须指定 key SpEL 表达式: " + joinPoint.getSignature());
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Expression expression = expressionCache.computeIfAbsent(
                method + "|" + rateLimit.key(), k -> PARSER.parseExpression(rateLimit.key()));

        StandardEvaluationContext context = new StandardEvaluationContext();
        Object[] args = joinPoint.getArgs();
        String[] names = PARAMETER_NAME_DISCOVERER.getParameterNames(method);
        for (int i = 0; i < args.length; i++) {
            context.setVariable("p" + i, args[i]);
            context.setVariable("a" + i, args[i]);
            if (names != null && i < names.length) {
                context.setVariable(names[i], args[i]);
            }
        }
        Object value = expression.getValue(context);
        return value != null ? String.valueOf(value) : "null";
    }
}
