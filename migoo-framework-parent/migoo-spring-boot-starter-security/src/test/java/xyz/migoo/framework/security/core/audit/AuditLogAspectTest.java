package xyz.migoo.framework.security.core.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import xyz.migoo.framework.common.observability.AuditLogEvent;
import xyz.migoo.framework.common.sensitive.Sensitive;
import xyz.migoo.framework.common.sensitive.SensitiveType;
import xyz.migoo.framework.security.core.annotation.AuditLog;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AuditLogAspect} 单元测试
 *
 * <p>覆盖成功/失败事件字段、动作回退与显式覆盖、recordParams 关闭、
 * @Sensitive 掩码协同、敏感关键词脱敏（含嵌套结构）、容器参数跳过、
 * 关键词判定大小写不敏感。</p>
 */
class AuditLogAspectTest {

    private ApplicationEventPublisher eventPublisher;

    private AuditLogAspect aspect;

    @BeforeEach
    void setUp() {
        // 隔离其他测试可能残留的登录上下文
        SecurityContextHolder.clearContext();
        eventPublisher = mock(ApplicationEventPublisher.class);
        aspect = new AuditLogAspect(eventPublisher);
    }

    @Test
    void successRecordsOperatorPathActionAndParams() throws Throwable {
        Object result = aspect.around(joinPoint("create", new OrderReq()));

        assertThat(result).isEqualTo("ok");
        AuditLogEvent event = publishedEvent();
        // 无登录上下文/无请求上下文的兜底值
        assertThat(event.operator()).isEqualTo("anonymous");
        assertThat(event.clientIp()).isEqualTo("unknown");
        assertThat(event.path()).isEqualTo("unknown");
        // 动作回退 类名#方法名（低基数，可作指标 tag）
        assertThat(event.action()).isEqualTo("Target#create");
        assertThat(event.success()).isTrue();
        assertThat(event.errorMessage()).isNull();
        assertThat(event.params()).contains("\"sku\":\"A\"");
        // @Sensitive 字段经 JsonUtils 序列化自动掩码
        assertThat(event.params()).contains("138****5678").doesNotContain("13812345678");
    }

    @Test
    void failureRecordsErrorAndRethrows() throws Throwable {
        ProceedingJoinPoint failing = joinPoint("boom", new OrderReq());
        when(failing.proceed()).thenThrow(new IllegalStateException("boom"));

        assertThatThrownBy(() -> aspect.around(failing))
                .isInstanceOf(IllegalStateException.class);

        AuditLogEvent event = publishedEvent();
        assertThat(event.success()).isFalse();
        assertThat(event.errorMessage()).contains("IllegalStateException").contains("boom");
        assertThat(event.params()).contains("\"sku\":\"A\"");
    }

    @Test
    void explicitActionOverridesMethodKey() throws Throwable {
        aspect.around(joinPoint("pay", "order-1"));

        assertThat(publishedEvent().action()).isEqualTo("支付");
    }

    @Test
    void recordParamsFalseOmitsParams() throws Throwable {
        aspect.around(joinPoint("disable", "user-1"));

        assertThat(publishedEvent().params()).isNull();
    }

    @Test
    void sensitiveKeywordsAreRedacted() throws Throwable {
        aspect.around(joinPoint("login", new CredentialReq()));

        AuditLogEvent event = publishedEvent();
        // 非敏感字段保留，敏感关键词字段整体置 ******（password/refreshToken 双双命中）
        assertThat(event.params()).contains("bob").contains("******");
        assertThat(event.params()).doesNotContain("p@ss").doesNotContain("jwt-xyz");
    }

    @Test
    void containerOnlyArgsProduceNullParams() throws Throwable {
        aspect.around(joinPoint("upload", new NotPayload()));

        AuditLogEvent event = publishedEvent();
        // 纯容器参数不可摘要 → params 置空，审计本体照常输出
        assertThat(event.params()).isNull();
        assertThat(event.success()).isTrue();
        assertThat(event.action()).isEqualTo("Target#upload");
    }

    @Test
    void redactHandlesNestedObjectsAndArrays() {
        String json = "[{\"user\":{\"name\":\"bob\",\"refreshToken\":\"r1\"},"
                + "\"items\":[{\"secretKey\":\"s1\",\"note\":\"ok\"}]}]";

        String redacted = AuditLogAspect.redact(json);

        // 嵌套对象与数组中的敏感字段同样脱敏，非敏感字段保留
        assertThat(redacted).contains("\"name\":\"bob\"").contains("\"note\":\"ok\"");
        assertThat(redacted).contains("\"refreshToken\":\"******\"")
                .contains("\"secretKey\":\"******\"");
        assertThat(redacted).doesNotContain("r1").doesNotContain("s1");
    }

    @Test
    void isSensitiveFieldMatchesKeywordsCaseInsensitively() {
        assertThat(AuditLogAspect.isSensitiveField("Password")).isTrue();
        assertThat(AuditLogAspect.isSensitiveField("accessToken")).isTrue();
        assertThat(AuditLogAspect.isSensitiveField("client_secret")).isTrue();
        assertThat(AuditLogAspect.isSensitiveField("authorizationHeader")).isTrue();
        assertThat(AuditLogAspect.isSensitiveField("sku")).isFalse();
        assertThat(AuditLogAspect.isSensitiveField("note")).isFalse();
    }

    // ==================== 测试夹具 ====================

    static class OrderReq {

        @Sensitive(type = SensitiveType.MOBILE)
        public String mobile = "13812345678";

        public String sku = "A";
    }

    static class CredentialReq {

        public String username = "bob";

        public String password = "p@ss";

        public String refreshToken = "jwt-xyz";
    }

    /**
     * 模拟 HttpServletRequest / MultipartFile 等不可摘要的容器参数
     */
    static class NotPayload {
    }

    @SuppressWarnings("unused")
    static class Target {

        @AuditLog
        public String create(@RequestBody OrderReq req) {
            return "ok";
        }

        @AuditLog(action = "支付")
        public String pay(String orderId) {
            return "ok";
        }

        @AuditLog(recordParams = false)
        public String disable(String userId) {
            return "ok";
        }

        @AuditLog
        public String login(@RequestBody CredentialReq req) {
            return "ok";
        }

        @AuditLog
        public String upload(NotPayload file) {
            return "ok";
        }

        @AuditLog
        public String boom(@RequestBody OrderReq req) {
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

    private AuditLogEvent publishedEvent() {
        ArgumentCaptor<AuditLogEvent> captor = ArgumentCaptor.forClass(AuditLogEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        return captor.getValue();
    }
}
