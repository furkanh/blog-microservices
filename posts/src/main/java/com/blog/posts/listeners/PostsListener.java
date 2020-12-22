package com.blog.posts.listeners;

import com.blog.posts.models.Post;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "posts")
public class PostsListener {
    @RabbitHandler
    public void receive(String in) throws Exception {
        Post post = new ObjectMapper().readValue(in, Post.class);
        System.out.println(post);
    }
}
