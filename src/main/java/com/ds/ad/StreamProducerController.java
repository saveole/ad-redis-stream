package com.ds.ad;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class StreamProducerController {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final String streamKey;

    public StreamProducerController(ReactiveRedisTemplate<String, String> redisTemplate,
                                    @Value("${spring.data.redis.stream.key}") String streamKey) {
        this.redisTemplate = redisTemplate;
        this.streamKey = streamKey;
    }

    @Data
    @AllArgsConstructor
    static class Message {
        private String id;
        private String content;

        public Map<String, String> toMap() {
            return Map.of("id", id, "content", content);
        }
    }

    @PostMapping("/api/send-message")
    public Mono<ResponseEntity<String>> sendMessage(@RequestBody Message message) {
        // 创建 Redis Stream 记录
        MapRecord<String, String, String> record = MapRecord.create(streamKey, message.toMap());

        // 发送消息到 Redis Stream
        return redisTemplate.opsForStream()
                .add(record)
                .map(result -> ResponseEntity.ok("Message sent with ID: " + result))
                .defaultIfEmpty(ResponseEntity.badRequest().body("Failed to send message"));
    }
}
