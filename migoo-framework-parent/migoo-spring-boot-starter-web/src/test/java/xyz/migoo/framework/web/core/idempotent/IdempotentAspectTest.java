package xyz.migoo.framework.web.core.idempotent;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.bind.annotation.RequestBody;
import xyz.migoo.framework.common.exception.ServiceException;
import xyz.migoo.framework.web.core.annotation.Idempotent;
import xyz.migoo.framework.web.core.store.InMemoryStateStore;
import xyz.migoo.framework.web.core.store.StateStore;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link IdempotentAspect} 单元测试
 *
 * <p>覆盖首次放行 + 窗口内重复拒绝（900）、不同入参放行、失败释放占位允许重试、
 * SpEL 维度、自定义消息、不可摘要参数退化、键格式与占位/防重窗口 TTL。</p>
 */
class IdempotentAspectTest {

    private InMemoryStateStore stateStore;

    private IdempotentAspect aspect;

    @BeforeEach
    void setUp() {
        stateStore = new InMemoryStateStore();
        aspect = new IdempotentAspect(stateStore);
    }

    @Test
    void firstRequestSucceedsThenDuplicateRejected() throws Throwable {
        assertThat(aspect.around(joinPoint("create", new OrderReq("A")))).isEqualTo("ok");
        // 防重窗口内相同请求体重复 → 900
        assertThatThrownBy(() -> aspect.around(joinPoint("create", new OrderReq("A"))))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("code", 900);
    }

    @Test
    void differentPayloadIsNotBlocked() throws Throwable {
        aspect.around(joinPoint("create", new OrderReq("A")));

        // 不同请求体 → 参数摘要不同 → 放行
        assertThat(aspect.around(joinPoint("create", new OrderReq("B")))).isEqualTo("ok");
    }

    @Test
    void failureReleasesClaimAllowsRetry() throws Throwable {
        ProceedingJoinPoint failing = joinPoint("create", new OrderReq("A"));
        when(failing.proceed()).thenThrow(new IllegalStateException("boom"));

        assertThatThrownBy(() -> aspect.around(failing))
                .isInstanceOf(IllegalStateException.class);

        // 失败释放占位 → 重试放行
        assertThat(aspect.around(joinPoint("create", new OrderReq("A")))).isEqualTo("ok");
    }

    @Test
    void spelKeyIsolatesByExpressionValue() throws Throwable {
        assertThat(aspect.around(joinPoint("pay", "order-1"))).isEqualTo("ok");
        assertThatThrownBy(() -> aspect.around(joinPoint("pay", "order-1")))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("code", 900);
        // 不同 SpEL 维度互不影响
        assertThat(aspect.around(joinPoint("pay", "order-2"))).isEqualTo("ok");
    }

    @Test
    void customMessageIsUsedWhenProvided() throws Throwable {
        aspect.around(joinPoint("custom", new OrderReq("A")));

        assertThatThrownBy(() -> aspect.around(joinPoint("custom", new OrderReq("A"))))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("code", 900)
                .hasFieldOrPropertyWithValue("message", "请勿重复提交");
    }

    @Test
    void nonPayloadArgsFallBackToMethodLevelKey() throws Throwable {
        assertThat(aspect.around(joinPoint("upload", new NotPayload()))).isEqualTo("ok");
        // 无可摘要参数 → 退化为「登录用户 + 方法」级幂等
        assertThatThrownBy(() -> aspect.around(joinPoint("upload", new NotPayload())))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("code", 900);
    }

    @Test
    void keyUsesDocumentedFormatAndSuccessUpgradesWindow() throws Throwable {
        StateStore spied = spy(stateStore);
        IdempotentAspect spiedAspect = new IdempotentAspect(spied);

        spiedAspect.around(joinPoint("create", new OrderReq("A")));

        // migoo:idempotent:类名#方法名:维度值（无请求上下文 → anonymous + 参数摘要）
        verify(spied).setIfAbsent(
                org.mockito.ArgumentMatchers.argThat(k ->
                        k.startsWith("migoo:idempotent:Target#create:anonymous:")),
                any(Duration.class));
        // 成功后占位升级为完整防重窗口（expire=60s，value=1）
        verify(spied).put(anyString(), eq(1L), eq(Duration.ofSeconds(60)));
    }

    @Test
    void claimTtlIsCappedByExpire() throws Throwable {
        StateStore spied = spy(stateStore);
        IdempotentAspect spiedAspect = new IdempotentAspect(spied);

        // expire=60s → 占位封顶 30s；expire=5s → 占位取 5s
        spiedAspect.around(joinPoint("create", new OrderReq("A")));
        spiedAspect.around(joinPoint("quick", new OrderReq("B")));

        ArgumentCaptor<Duration> ttl = ArgumentCaptor.forClass(Duration.class);
        verify(spied, times(2)).setIfAbsent(anyString(), ttl.capture());
        assertThat(ttl.getAllValues()).containsExactly(Duration.ofSeconds(30), Duration.ofSeconds(5));
    }

    // ==================== 测试夹具 ====================

    static class OrderReq {

        private final String sku;

        OrderReq(String sku) {
            this.sku = sku;
        }

        public String getSku() {
            return sku;
        }
    }

    /**
     * 模拟 HttpServletRequest / MultipartFile 等不可摘要的容器参数
     */
    static class NotPayload {
    }

    @SuppressWarnings("unused")
    static class Target {

        @Idempotent(expire = 60)
        public String create(@RequestBody OrderReq req) {
            return "ok";
        }

        @Idempotent(key = "#orderId", expire = 300)
        public String pay(String orderId) {
            return "ok";
        }

        @Idempotent(expire = 60, message = "请勿重复提交")
        public String custom(@RequestBody OrderReq req) {
            return "ok";
        }

        @Idempotent(expire = 60)
        public String upload(NotPayload file) {
            return "ok";
        }

        @Idempotent(expire = 5)
        public String quick(@RequestBody OrderReq req) {
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
