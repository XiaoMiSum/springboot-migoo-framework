package xyz.migoo.framework.jackson.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * @author xiaomi
 * Created on 2021/11/21 13:54
 */
@Slf4j
@Configuration
public class JacksonAutoConfiguration {

    /**
     * 配置 Jackson 支持 LocalDateTime
     */
   /* @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 注册 JavaTimeModule
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // 配置序列化
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(dateTimeFormatter));

        // 配置反序列化
        javaTimeModule.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(dateTimeFormatter));

        objectMapper.registerModule(javaTimeModule);

        // 设置时区
        objectMapper.setTimeZone(TimeZone.getTimeZone("UTC"));

        // 禁用时间戳
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return objectMapper;
    }*/
}