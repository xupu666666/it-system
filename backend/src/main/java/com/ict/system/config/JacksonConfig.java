package com.ict.system.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        System.out.println("配置全局Jackson ObjectMapper");

        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        
        // 设置序列化包含的属性
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        
        // 禁用循环引用检测，使用注解处理
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // 启用以下特性增强循环引用处理能力
        objectMapper.configure(SerializationFeature.WRITE_SELF_REFERENCES_AS_NULL, true);
        
        // 输出漂亮打印的JSON，便于调试
        // 生产环境可以注释掉这行来优化性能
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        return objectMapper;
    }
}
