package com.blog.posts.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String topic = "posts";

    @Bean
    public NewTopic posts() {
        return TopicBuilder
                .name(topic)
                .partitions(6)
                .replicas(1)
                .build();
    }

}
