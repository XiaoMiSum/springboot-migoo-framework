package xyz.migoo.framework.web.core.ratelimit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import xyz.migoo.framework.common.exception.ServiceException;
import xyz.migoo.framework.common.observability.RateLimitExceededEvent;
import xyz.migoo.framework.web.core.annotation.RateLimit;
import xyz.migoo.framework.web.core.annotation.RateLimitType;
import xyz.migoo.framework.web.core.store.InMemoryStateStore;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link RateLimitAspect} 单元测试
 */
class RateLimitAspectTest {

    private RateLimitAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new RateLimitAspect(new DefaultRateLimiter(new InMemoryStateStore()));
    }

    @Test
    void keyDimensionSpelLimitsPerUser() throws Throwable {
        // limit=2: 同一用户名第 3 次被拒，其他用户名不受影响
        assertThat(aspect.around(joinPoint("login", "user1"))).isEqualTo("ok");
        assertThat(aspect.around(joinPoint("login", "user1"))).isEqualTo("ok");
        assertThatThrownBy(() -> aspect.around(joinPoint("login", "user1")))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("code", 429);
        assertThat(aspect.around(joinPoint("login", "user2"))).isEqualTo("ok");
    }

    @Test
    void ipDimensionLimitsWhenNoRequestContext() throws Throwable {
        // 无请求上下文时 IP 维度取 unknown，仍按同一 key 限流
        assertThat(aspect.around(joinPoint("ipLimited"))).isEqualTo("ok");
        assertThatThrownBy(() -> aspect.around(joinPoint("ipLimited")))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("code", 429);
    }

    @Test
    void customMessageIsUsedWhenProvided() throws Throwable {
        for (int i = 0; i < 5; i++) {
            aspect.around(joinPoint("custom", "n"));
        }
        assertThatThrownBy(() -> aspect.around(joinPoint("custom", "n")))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("code", 429)
                .hasFieldOrPropertyWithValue("message", "操作过于频繁");
    }

    @Test
    void missingKeyExpressionFailsFast() throws Throwable {
        assertThatThrownBy(() -> aspect.around(joinPoint("missingKey")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("key SpEL");
    }

    @Test
    void publishesRateLimitExceededEventWhenRejected() throws Throwable {
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        RateLimitAspect aspectWithPublisher =
                new RateLimitAspect(new DefaultRateLimiter(new InMemoryStateStore()), publisher);

        aspectWithPublisher.around(joinPoint("ipLimited"));
        assertThatThrownBy(() -> aspectWithPublisher.around(joinPoint("ipLimited")))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("code", 429);

        // 无请求上下文 → path=unknown；keyType 取自限流维度；limit=1
        verify(publisher).publishEvent(new RateLimitExceededEvent("unknown", "ip", 1));
    }

    @Test
    void publishFailureDoesNotChangeRejection() throws Throwable {
        // 发布器异常（监听器抛回）→ 照常抛 429（观测不得影响业务）
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        doThrow(new IllegalStateException("listener failed")).when(publisher).publishEvent(any());
        RateLimitAspect aspectWithPublisher =
                new RateLimitAspect(new DefaultRateLimiter(new InMemoryStateStore()), publisher);

        aspectWithPublisher.around(joinPoint("ipLimited"));
        assertThatThrownBy(() -> aspectWithPublisher.around(joinPoint("ipLimited")))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("code", 429);
    }

    // ==================== 测试夹具 ====================

    @SuppressWarnings("unused")
    static class Target {

        @RateLimit(type = RateLimitType.KEY, key = "#username", limit = 2, window = 60)
        public String login(String username) {
            return "ok";
        }

        @RateLimit(limit = 1, window = 60)
        public String ipLimited() {
            return "ok";
        }

        @RateLimit(type = RateLimitType.KEY, key = "#name", limit = 5, window = 60, message = "操作过于频繁")
        public String custom(String name) {
            return "ok";
        }

        @RateLimit(type = RateLimitType.KEY, key = "", limit = 5, window = 60)
        public String missingKey() {
            return "ok";
        }
    }

    /**
     * 按方法名与实参构造被切的 joinPoint（实参类型自动推导方法签名）
     */
    private ProceedingJoinPoint joinPoint(String methodName, Object... args) throws Throwable {
        Class<?>[] parameterTypes = Arrays.stream(args)
                .map(Object::getClass)
                .toArray(Class<?>[]::new);
        Method method = Target.class.getMethod(methodName, parameterTypes);

        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(method);
        when(signature.toString()).thenReturn(method.toString());

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn("ok");
        return joinPoint;
    }
}
