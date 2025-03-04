package com.ds.ad;

import lombok.AllArgsConstructor;
import lombok.Data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class StreamProducerController {

    @Data
    @AllArgsConstructor
    static class Message {
        private String id;
        private String content;

        public Map<String, String> toMap() {
            return Map.of("id", id, "content", content);
        }
    }

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String STREAM_KEY = "my-stream";

    // 发送消息到 Redis Stream
    @PostMapping("/api/send-message")
    public String sendMessage(@RequestBody Message message) {
        // 添加消息到 Stream
        redisTemplate.opsForStream().add(STREAM_KEY, message.toMap());
        System.out.println("Produced message: " + message);
        return message.toString();
    }
}
