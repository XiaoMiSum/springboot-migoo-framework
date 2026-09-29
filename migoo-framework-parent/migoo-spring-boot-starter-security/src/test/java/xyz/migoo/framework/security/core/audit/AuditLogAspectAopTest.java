package xyz.migoo.framework.security.core.audit;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import xyz.migoo.framework.common.observability.AuditLogEvent;
import xyz.migoo.framework.security.core.annotation.AuditLog;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link AuditLogAspect} 走真实 Spring AOP 代理的回归用例
 * <p>
 * Spring 7 起复合切点（{@code @annotation || @within}）不再回填注解参数绑定：
 * 若把注解声明成通知形参，运行期会拿到 {@code null} 并在切面内 NPE，被审计接口全部 500。
 * 本用例覆盖「方法注解」「类注解」两条匹配路径在代理下的真实执行。
 */
class AuditLogAspectAopTest {

    @Configuration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    static class Config {

        @Bean
        CapturingPublisher eventPublisher() {
            return new CapturingPublisher();
        }

        @Bean
        AuditLogAspect auditLogAspect(CapturingPublisher eventPublisher) {
            return new AuditLogAspect(eventPublisher);
        }

        @Bean
        MethodSample methodSample() {
            return new MethodSample();
        }

        @Bean
        ClassSample classSample() {
            return new ClassSample();
        }
    }

    static class MethodSample {

        @AuditLog(action = "METHOD_ACTION")
        public String call() {
            return "ok";
        }
    }

    @AuditLog(action = "CLASS_ACTION")
    static class ClassSample {

        public String call() {
            return "ok";
        }
    }

    /**
     * 事件采集发布器（只收 {@link AuditLogEvent}，观测链路其余事件忽略）
     */
    static class CapturingPublisher implements ApplicationEventPublisher {

        private final List<AuditLogEvent> events = new CopyOnWriteArrayList<>();

        @Override
        public void publishEvent(Object event) {
            if (event instanceof AuditLogEvent auditEvent) {
                events.add(auditEvent);
            }
        }
    }

    @Test
    void methodAnnotationAuditsThroughProxy() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Config.class)) {
            MethodSample proxy = ctx.getBean(MethodSample.class);
            CapturingPublisher publisher = ctx.getBean(CapturingPublisher.class);

            // 缺陷态：形参绑定为 null → 切面内 NPE → 业务调用 500
            assertThat(proxy.call()).isEqualTo("ok");

            assertThat(publisher.events).hasSize(1);
            AuditLogEvent event = publisher.events.get(0);
            assertThat(event.action()).isEqualTo("METHOD_ACTION");
            assertThat(event.success()).isTrue();
            assertThat(event.operator()).isEqualTo("anonymous");
        }
    }

    @Test
    void classAnnotationAuditsThroughProxy() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Config.class)) {
            ClassSample proxy = ctx.getBean(ClassSample.class);
            CapturingPublisher publisher = ctx.getBean(CapturingPublisher.class);

            assertThat(proxy.call()).isEqualTo("ok");

            assertThat(publisher.events).hasSize(1);
            AuditLogEvent event = publisher.events.get(0);
            assertThat(event.action()).isEqualTo("CLASS_ACTION");
            assertThat(event.success()).isTrue();
        }
    }
}
