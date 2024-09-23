package org.example.bean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author zhy
 * @date 2024/9/23 14:50
 */
@Configuration
public class RedisTemplateBean {
    @Autowired
    private RedisProperties redisProperties;

    @Bean
    public RedisTemplateFactoryBean redisTemplateBeanFactory() {
        return new RedisTemplateFactoryBean(redisProperties);
    }

    @Bean("redisConnectionFactory")
    public RedisConnectionFactory redisConnectionFactory() {
        return redisTemplateBeanFactory().create(false);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(@Qualifier("redisConnectionFactory") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplateBeanFactory().setSerializer(redisTemplate);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
