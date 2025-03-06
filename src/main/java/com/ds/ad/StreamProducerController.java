package com.ds.ad;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StreamProducerController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String STREAM_KEY = "my-stream";

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
