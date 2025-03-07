package com.ds.ad;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = RedisTestConfiguration.class)
public class TokenBucketRateLimiterTest {

    @Autowired TokenBucketRateLimiter bucketRateLimiter;

    @Test
    public void testAllowRequest_WhenTokenAvailable() {
        // 准备测试数据
        String key = "testAllowRequest_WhenTokenAvailable";
        long capacity = 10;
        double rate = 1.0;
        int requested = 1;

        // 执行测试
        boolean result = bucketRateLimiter.allowRequest(key, capacity, rate, requested);

        // 验证结果
        Assertions.assertTrue(result, "当有可用令牌时应该返回true");
    }

    @Test
    public void testAllowRequest_WhenNoTokenAvailable() {
        // 准备测试数据
        String key = "testAllowRequest_WhenNoTokenAvailable";
        long capacity = 10;
        double rate = 1.0;
        int requested = 1;

        // 执行测试
        boolean first = bucketRateLimiter.allowRequest(key, capacity, rate, requested);
        boolean second = bucketRateLimiter.allowRequest(key, capacity, rate, requested);

        // 验证结果
        Assertions.assertTrue(first, "当有可用令牌时应该返回true");
        Assertions.assertFalse(second, "当没有可用令牌时应该返回false");
    }

    @Test
    public void testAllowRequest_WithZeroCapacity() {
        // 准备测试数据
        String key = "testAllowRequest_WithZeroCapacity";
        long capacity = 0;
        double rate = 1.0;
        int requested = 1;

        // 执行测试
        boolean result = bucketRateLimiter.allowRequest(key, capacity, rate, requested);

        // 验证结果
        Assertions.assertFalse(result, "当容量为0时应该返回false");
    }

    @Test
    public void testAllowRequest_WithHighRequestedTokens() {
        // 准备测试数据
        String key = "testAllowRequest_WithHighRequestedTokens";
        long capacity = 5;
        double rate = 1.0;
        int requested = 10;

        // 执行测试
        boolean result = bucketRateLimiter.allowRequest(key, capacity, rate, requested);

        // 验证结果
        Assertions.assertFalse(result, "当请求的令牌数大于容量时应该返回false");
    }
}
