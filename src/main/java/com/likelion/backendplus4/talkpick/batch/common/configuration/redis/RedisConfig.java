package com.likelion.backendplus4.talkpick.batch.common.configuration.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

/**
 * Redis 설정을 담당하는 Configuration 클래스입니다.
 *
 * @since 2025-05-12
 * @modified 2025-05-12
 */
@Configuration
public class RedisConfig {
    
    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.password}")
    private String password;

    /**
     * RedisConnectionFactory를 구성합니다.
     * application.yml에 설정된 host와 port 정보를 사용하여 LettuceConnectionFactory를 생성합니다.
     *
     * @return LettuceConnectionFactory 객체
     * @author 박찬병
     * @since 2025-05-12
     * @modified 2025-05-12
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(host);
        redisConfig.setPort(port);
        redisConfig.setPassword(RedisPassword.of(password));
    
        return new LettuceConnectionFactory(redisConfig);
    }

}
