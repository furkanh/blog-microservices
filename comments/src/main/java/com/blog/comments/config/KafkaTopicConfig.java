package com.blog.comments.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String topic = "comments";

    @Bean
    public NewTopic comments() {
        return TopicBuilder
                .name(topic)
                .partitions(6)
                .replicas(1)
                .build();
    }

}
