package com.blog.posts.listener;

import lombok.Data;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;

@Data
@Service
public class PostListener {
    private CountDownLatch latch = new CountDownLatch(1);
    private String payload = null;

    public void reset() {
        payload = null;
        latch = new CountDownLatch(1);
    }

    @KafkaListener(topics = "posts", groupId = "test-listener")
    public void receive(@Payload String message) throws Exception {
        if (latch.getCount() == 0) {
            throw new Exception();
        }
        setPayload(message);
        latch.countDown();
    }
}