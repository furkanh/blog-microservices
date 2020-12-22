package com.blog.posts.controllers;

import com.blog.posts.models.Post;
import com.blog.posts.repositories.PostRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PostController {

    @Autowired
    private Queue queue;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private PostRepository postRepository;

    @GetMapping("/api/posts")
    public List<Post> getPosts() {
        return postRepository.findAll();
    }

    @PostMapping("/api/posts")
    public Post createPost(@RequestBody Post post) throws JsonProcessingException {
        postRepository.save(post);
        String payload = new ObjectMapper().writeValueAsString(post);
        rabbitTemplate.convertAndSend(queue.getName(), payload);
        return post;
    }
}
