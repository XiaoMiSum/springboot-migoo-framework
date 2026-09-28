package xyz.migoo.framework.web.core.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import xyz.migoo.framework.common.exception.ServiceException;
import xyz.migoo.framework.web.core.annotation.RateLimit;
import xyz.migoo.framework.web.core.store.InMemoryStateStore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link RateLimitAspect} 走真实 Spring AOP 代理的回归用例
 * <p>
 * Spring 7 起复合切点（{@code @annotation || @within}）不再回填注解参数绑定：
 * 若把注解声明成通知形参，运行期会拿到 {@code null} 并在切面内 NPE，所有被限流接口 500。
 * 本用例覆盖「方法注解」「类注解」两条匹配路径在代理下的真实执行。
 */
class RateLimitAspectAopTest {

    @Configuration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    static class Config {

        @Bean
        RateLimiter rateLimiter() {
            return new DefaultRateLimiter(new InMemoryStateStore());
        }

        @Bean
        RateLimitAspect rateLimitAspect(RateLimiter rateLimiter) {
            return new RateLimitAspect(rateLimiter);
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

        @RateLimit(limit = 1, window = 60)
        public String call() {
            return "ok";
        }
    }

    @RateLimit(limit = 1, window = 60)
    static class ClassSample {

        public String call() {
            return "ok";
        }
    }

    @Test
    void methodAnnotationEnforcedThroughProxy() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Config.class)) {
            MethodSample proxy = ctx.getBean(MethodSample.class);

            assertThat(proxy.call()).isEqualTo("ok");
            assertThatThrownBy(proxy::call)
                    .isInstanceOf(ServiceException.class)
                    .hasFieldOrPropertyWithValue("code", 429);
        }
    }

    @Test
    void classAnnotationEnforcedThroughProxy() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Config.class)) {
            ClassSample proxy = ctx.getBean(ClassSample.class);

            assertThat(proxy.call()).isEqualTo("ok");
            assertThatThrownBy(proxy::call)
                    .isInstanceOf(ServiceException.class)
                    .hasFieldOrPropertyWithValue("code", 429);
        }
    }
}
