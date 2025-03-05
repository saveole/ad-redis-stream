package com.ds.ad;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;
import java.util.Map;

@Configuration
public class RedisStreamConfig {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    private static final String STREAM_KEY = "my-stream";
    private static final String GROUP_NAME = "my-group";

    @Bean
    public StreamMessageListenerContainer<String, ObjectRecord<String, String>> container() {
        // 配置 Stream 监听容器
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, ObjectRecord<String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        .pollTimeout(Duration.ofSeconds(5)) // 轮询超时
                        .targetType(String.class)// 消息反序列化为 Map
                        .build();

        // 创建监听容器
        StreamMessageListenerContainer<String, ObjectRecord<String, String>> container =
                StreamMessageListenerContainer.create(redisConnectionFactory, options);

        // 创建消费者组（如果不存在）
        try {
            redisConnectionFactory.getConnection().streamCommands().xGroupCreate(
                    STREAM_KEY.getBytes(),
                    GROUP_NAME,
                    ReadOffset.from("0-0"),
                    true // 如果组不存在则创建
            );
        } catch (Exception e) {
            System.out.println("Consumer group already exists or stream not initialized: " + e.getMessage());
        }

        return container;
    }
}
