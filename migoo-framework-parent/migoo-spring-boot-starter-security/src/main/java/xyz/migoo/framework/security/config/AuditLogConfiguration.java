package xyz.migoo.framework.security.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.migoo.framework.security.core.audit.AuditLogAspect;

/**
 * 安全审计配置
 * <p>
 * 注册 {@code @AuditLog} 注解切面：双通道输出审计（{@code migoo.audit} logger JSON 行 +
 * {@code AuditLogEvent} 事件供应用落库）。
 *
 * @author xiaomi
 */
@Configuration
public class AuditLogConfiguration {

    /**
     * 审计切面 Bean（migoo.security.audit.enabled 控制，默认开启）
     */
    @Bean
    @ConditionalOnMissingBean(AuditLogAspect.class)
    @ConditionalOnProperty(name = "migoo.security.audit.enabled", havingValue = "true", matchIfMissing = true)
    public AuditLogAspect auditLogAspect(ApplicationEventPublisher eventPublisher) {
        return new AuditLogAspect(eventPublisher);
    }
}
