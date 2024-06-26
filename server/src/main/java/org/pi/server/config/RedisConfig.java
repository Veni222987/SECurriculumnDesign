package org.pi.server.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * @author huhuayu
 * @date 2024/6/14 1:15
 * @description Redis 配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedisConfig {
    private String host;
    private int port;
    private int timeout;
    private String password;
    private int database;


    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        // 配置 Redis 连接信息
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(host, port);
        {
            redisStandaloneConfiguration.setPassword(password);
            redisStandaloneConfiguration.setDatabase(database);
        }
        // 配置 Lettuce 客户端选项，包括保活设置
        SocketOptions socketOptions = SocketOptions.builder()
                .keepAlive(true)
                .build();

        ClientOptions clientOptions = ClientOptions.builder()
                .socketOptions(socketOptions)
                .build();

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(3000))
                .shutdownTimeout(Duration.ofMillis(100))
                .clientOptions(clientOptions)
                .build();

        return new LettuceConnectionFactory(redisStandaloneConfiguration, clientConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }
}
