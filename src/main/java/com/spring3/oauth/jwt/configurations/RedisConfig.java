package com.spring3.oauth.jwt.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class RedisConfig {
    @Value("${REDIS_HOST}")
    private String redisHost;
    
    @Value("${REDIS_PORT}")
    private int redisPort;
    
    @Value("${REDIS_PASSWORD:#{null}}")
    private String redisPassword;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setHostName(redisHost);
        factory.setPort(redisPort);
        if (redisPassword != null && !redisPassword.trim().isEmpty()) {
            factory.setPassword(redisPassword);
        }
        factory.setUsePool(true);
        return factory;
    }

    // Cấu hình RedisTemplate
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());

        // Sử dụng Jackson để serialize/deserialize Object thành JSON
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }
}
