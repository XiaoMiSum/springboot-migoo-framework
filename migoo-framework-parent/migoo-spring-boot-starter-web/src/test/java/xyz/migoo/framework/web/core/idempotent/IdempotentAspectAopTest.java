package xyz.migoo.framework.web.core.idempotent;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import xyz.migoo.framework.common.exception.ServiceException;
import xyz.migoo.framework.web.core.annotation.Idempotent;
import xyz.migoo.framework.web.core.store.InMemoryStateStore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link IdempotentAspect} 走真实 Spring AOP 代理的回归用例
 * <p>
 * Spring 7 起复合切点（{@code @annotation || @within}）不再回填注解参数绑定：
 * 若把注解声明成通知形参，运行期会拿到 {@code null} 并在切面内 NPE，被防重保护的接口全部 500。
 * 本用例覆盖「方法注解」「类注解」两条匹配路径在代理下的真实执行。
 */
class IdempotentAspectAopTest {

    @Configuration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    static class Config {

        @Bean
        IdempotentAspect idempotentAspect() {
            return new IdempotentAspect(new InMemoryStateStore());
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

        @Idempotent(expire = 60)
        public String call() {
            return "ok";
        }
    }

    @Idempotent(expire = 60)
    static class ClassSample {

        public String call() {
            return "ok";
        }
    }

    @Test
    void methodAnnotationDeduplicatesThroughProxy() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Config.class)) {
            MethodSample proxy = ctx.getBean(MethodSample.class);

            // 缺陷态：形参绑定为 null → 切面内 NPE → 业务调用 500
            assertThat(proxy.call()).isEqualTo("ok");
            // 防重窗口内相同请求重复 → 900
            assertThatThrownBy(proxy::call)
                    .isInstanceOf(ServiceException.class)
                    .hasFieldOrPropertyWithValue("code", 900);
        }
    }

    @Test
    void classAnnotationDeduplicatesThroughProxy() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Config.class)) {
            ClassSample proxy = ctx.getBean(ClassSample.class);

            assertThat(proxy.call()).isEqualTo("ok");
            assertThatThrownBy(proxy::call)
                    .isInstanceOf(ServiceException.class)
                    .hasFieldOrPropertyWithValue("code", 900);
        }
    }
}
