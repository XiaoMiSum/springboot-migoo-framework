package xyz.migoo.framework.jackson.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import xyz.migoo.framework.common.util.json.JsonUtils;

import java.time.LocalDateTime;

/**
 * @author xiaomi
 * Created on 2021/11/21 13:54
 */
@Slf4j
@Configuration
public class JacksonAutoConfiguration {
    @Bean
    public BeanPostProcessor objectMapperBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(@Nullable Object bean, @Nullable String beanName) throws BeansException {
                if (!(bean instanceof ObjectMapper)) {
                    return bean;
                }
                /*
                 * 1. 新增Long类型序列化规则，数值超过2^53-1，在JS会出现精度丢失问题，因此Long自动序列化为字符串类型
                 * 2. 新增LocalDateTime序列化、反序列化规则
                 */
                JsonUtils.init(((ObjectMapper) bean).registerModules(
                        new SimpleModule()
                                // 不开启 由项目自定义
                                //.addSerializer(Long.class, ToStringSerializer.instance)
                                //.addSerializer(Long.TYPE, ToStringSerializer.instance)
                                .addSerializer(LocalDateTime.class, LocalDateTimeSerializer.INSTANCE)
                                .addDeserializer(LocalDateTime.class, LocalDateTimeDeserializer.INSTANCE))
                );
                log.info("初始化 jackson 自动配置");
                return bean;
            }
        };
    }
}
