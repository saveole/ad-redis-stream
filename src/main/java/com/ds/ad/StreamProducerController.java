package com.ds.ad;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class StreamProducerController {

    public static class Message {
        private String id;
        private String content;

        public Message() {}

        public Map<String, String> toMap() {
            return Map.of("id", id, "content", content);
        }

        public void setContent(String content) {
            this.content = content;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getContent() {
            return content;
        }

        public String getId() {
            return id;
        }
    }

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String STREAM_KEY = "my-stream";

    // 发送消息到 Redis Stream
    @PostMapping("/api/send-message")
    public String sendMessage(@RequestBody Message message) {
        // 添加消息到 Stream
        var recordId = stringRedisTemplate.opsForStream().add(STREAM_KEY, message.toMap());
        System.out.println("Produced message ID: " + recordId);
        System.out.println("Produced message: " + message);
        return message.toString();
    }
}
