package xyz.migoo.framework.mybatis.config;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.migoo.framework.mybatis.core.handler.DefaultFieldHandler;
import xyz.migoo.framework.mybatis.core.handler.UTCLocalDateTimeHandler;
import xyz.migoo.framework.mybatis.core.handler.UUIDTypeHandler;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author xiaomi
 * Created on 2021/11/23 20:19
 */
@Configuration
@AutoConfiguration(before = MybatisPlusAutoConfiguration.class)
@MapperScan(value = {"xyz.migoo.framework.**"}, annotationClass = Mapper.class, lazyInitialization = "${mybatis.lazy-initialization:false}")
public class MybatisAutoConfiguration {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        // 分页插件
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return mybatisPlusInterceptor;
    }

    @Bean
    public ConfigurationCustomizer mybatisConfigurationCustomizer() {
        return configuration -> {
            TypeHandlerRegistry registry = configuration.getTypeHandlerRegistry();
            // 为所有 LocalDateTime 注册同一个 Handler
            registry.register(LocalDateTime.class, UTCLocalDateTimeHandler.class);
            // 为 UUID 注册 TypeHandler
            registry.register(UUID.class, UUIDTypeHandler.class);
        };

    }


    @Bean
    public MetaObjectHandler defaultMetaObjectHandler() {
        // 自动填充参数类
        return new DefaultFieldHandler();
    }
}