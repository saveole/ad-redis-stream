package com.ds.ad;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;


@Component
public class StreamConsumer {

    @Autowired
    private StreamMessageListenerContainer<String, ObjectRecord<String, Message>> container;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String STREAM_KEY = "my-stream";
    private static final String GROUP_NAME = "my-group";
    private static final String CONSUMER_NAME = "consumer-1";

    @PostConstruct
    public void startConsumer() {
        // 配置消费者，集群消费场景的话需要配置不同 CONSUMER_NAME
        Consumer consumer = Consumer.from(GROUP_NAME, CONSUMER_NAME);
        var streamOps = stringRedisTemplate.opsForStream();

        // 订阅 Stream 并处理消息
        container.receive(
                consumer,
                StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed()),
                message -> {
                    var recordId = message.getId();
                    System.out.println(message.getClass());
                    var msg = message.toString();
                    System.out.println("Consumed msg: " + msg);
                    var content = message.getValue();
                    System.out.println("Consumed message: " + content.getId());
                    System.out.println("Consumed message: " + content.getContent());
                    // 手动 ACK
                    var acked = streamOps.acknowledge(STREAM_KEY, GROUP_NAME, recordId);
                    System.out.println("Acked: " + acked);
                }
        );

        // 启动容器
        container.start();
    }
}