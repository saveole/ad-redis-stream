package com.ds.ad;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
public class StreamProducerController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String STREAM_KEY = "my-stream";

    @Autowired
    private TokenBucketRateLimiter rateLimiter;

    @GetMapping("/test")
    public String testRateLimit(@RequestParam String userId) {
        // 配置：桶容量为 10，每秒生成 2 个令牌，每次请求需要 1 个令牌
        boolean allowed = rateLimiter.allowRequest(userId, 1, 1.0, 1);

        if (allowed) {
            return "Request allowed for user: " + userId;
        } else {
            return "Request denied for user: " + userId + " (rate limited)";
        }
    }

    // 发送消息到 Redis Stream
    @PostMapping("/api/send-message")
    public String sendMessage(@RequestBody Message message) {
        // 添加消息到 Stream
        StreamOperations<String, Object, Object> opsForStream = stringRedisTemplate.opsForStream();
        // var recordId = opsForStream.add(STREAM_KEY, message.toMap());
        var recordId = opsForStream.add(StreamRecords.newRecord().ofObject(message).withStreamKey(STREAM_KEY));
        System.out.println("Produced message ID: " + recordId);
        System.out.println("Produced message: " + message);
        assert recordId != null;
        return recordId.getValue();
    }
}
