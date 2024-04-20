package xyz.migoo.framework.mybatis.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.migoo.framework.mybatis.core.handler.DefaultFieldHandler;

/**
 * @author xiaomi
 * Created on 2021/11/23 20:19
 */
@Configuration
@MapperScan(value = {"${migoo.framework.package-name}", "${migoo.framework.biz-package-name}"}
        , annotationClass = Mapper.class,
        lazyInitialization = "${mybatis.lazy-initialization:false}")
public class MybatisAutoConfiguration {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        // 分页插件
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return mybatisPlusInterceptor;
    }

    @Bean
    public MetaObjectHandler defaultMetaObjectHandler() {
        // 自动填充参数类
        return new DefaultFieldHandler();
    }

}
