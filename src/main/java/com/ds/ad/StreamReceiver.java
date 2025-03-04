package com.ds.ad;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Service
public class StreamReceiver {

    Logger log = LoggerFactory.getLogger(StreamReceiver.class);

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final String streamKey;
    private Disposable subscription;

    public StreamReceiver(ReactiveRedisTemplate<String, String> redisTemplate,
                          @Value("${spring.data.redis.stream.key}") String streamKey) {
        this.redisTemplate = redisTemplate;
        this.streamKey = streamKey;
    }

    @PostConstruct
    public void startConsuming() {
        // 配置消费者组（如果首次使用需创建）
        setupConsumerGroup();

        // 启动消费
        subscription = consumeMessages()
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                        record -> log.info("Received message: {}", record.getValue()),
                        error -> log.error("Error consuming message: ", error),
                        () -> log.info("Consumer completed")
                );
    }

    private void setupConsumerGroup() {
        redisTemplate.opsForStream()
                .createGroup(streamKey, "my-group-xx")
                .doOnSuccess(result -> log.info("Consumer group 'my-group' created"))
                .doOnError(error -> log.warn("Consumer group already exists"))
                .subscribe();
    }

    private Flux<MapRecord<String, Object, Object>> consumeMessages() {
        return redisTemplate.opsForStream()
                .read(Consumer.from("my-group-xx", "consumer-1"),
                        StreamReadOptions.empty().count(1).block(Duration.ofMillis(1000)),
                        StreamOffset.fromStart(streamKey))
                .doOnNext(record -> {
                    // 确认消息已处理
                    redisTemplate.opsForStream()
                            .acknowledge(streamKey, "my-group", record.getId())
                            .subscribe();
                })
                .repeat();
    }

    @PreDestroy
    public void shutdown() {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
        }
    }
}
