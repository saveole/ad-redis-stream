package com.ds.ad;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StreamConsumer {

    @Autowired
    private StreamMessageListenerContainer<String, ObjectRecord<String, String>> container;

    private static final String STREAM_KEY = "my-stream";
    private static final String GROUP_NAME = "my-group";
    private static final String CONSUMER_NAME = "consumer-1";

    @PostConstruct
    public void startConsumer() {
        // 配置消费者
        Consumer consumer = Consumer.from(GROUP_NAME, CONSUMER_NAME);

        // 订阅 Stream 并处理消息
        container.receive(
                consumer,
                StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed()),
                message -> {
                    var content = message.getValue();
                    System.out.println("Consumed message: " + content);

                    // 确认消息已处理（手动 ACK）
                    //streamMessageListenerContainer.receiveAutoAck()
                }
        );

        // 启动容器
        container.start();
    }
}