package com.ds.ad;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class TokenBucketRateLimiter {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private DefaultRedisScript<Long> rateLimitScript;

    // 初始化 Lua 脚本
    @PostConstruct
    public void init() {
        rateLimitScript = new DefaultRedisScript<>();
        rateLimitScript.setLocation(new ClassPathResource("token_bucket.lua"));
        rateLimitScript.setResultType(Long.class);
    }

    /**
     * 检查是否允许请求
     * @param key 令牌桶的唯一键（如用户 ID 或 API 路径）
     * @param capacity 桶的容量
     * @param rate 令牌生成速率（每秒生成令牌数）
     * @param requested 请求的令牌数
     * @return true 表示通过，false 表示被限流
     */
    public boolean allowRequest(String key, long capacity, double rate, int requested) {
        // 执行 Lua 脚本

        // 执行 Lua 脚本时将所有数值参数转换为String
        var result = stringRedisTemplate.execute(
                rateLimitScript,
                Collections.singletonList("rate_limiter:" + key), // KEYS[1]
                String.valueOf(capacity),    // ARGV[1]: 容量（转String）
                String.valueOf(rate),        // ARGV[2]: 速率（转String）
                String.valueOf(System.currentTimeMillis()), // ARGV[3]: 时间戳（转String）
                String.valueOf(requested)    // ARGV[4]: 请求令牌数（转String）
        );
        return result == 1;
    }
}
