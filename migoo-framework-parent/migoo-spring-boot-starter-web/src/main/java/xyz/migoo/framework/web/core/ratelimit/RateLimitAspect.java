package xyz.migoo.framework.web.core.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
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

    @Around(value = "@annotation(rateLimit) || @within(rateLimit)", argNames = "joinPoint,rateLimit")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
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
