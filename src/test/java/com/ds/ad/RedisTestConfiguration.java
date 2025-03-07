
package com.ds.ad;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.utility.DockerImageName;

@SpringBootApplication
public class RedisTestConfiguration {

    @Bean
    @ServiceConnection(name = "redis")
    RedisContainer redisContainer() {
        return new RedisContainer(DockerImageName.parse("redis/redis-stack:latest"));
    }

    @Bean
    public TokenBucketRateLimiter bucketRateLimiter(RedisContainer redisContainer) {
        var config = new RedisStandaloneConfiguration();
        config.setHostName(redisContainer.getHost());
        config.setPort(redisContainer.getRedisPort());
        var factory = new LettuceConnectionFactory(config);
        return new TokenBucketRateLimiter(new StringRedisTemplate(factory));
    }

}
