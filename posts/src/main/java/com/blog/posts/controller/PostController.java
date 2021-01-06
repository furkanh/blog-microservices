package com.blog.posts.controller;

import com.blog.posts.config.KafkaTopicConfig;
import com.blog.posts.dto.*;
import com.blog.posts.exception.KafkaSendException;
import com.blog.posts.model.Post;
import com.blog.posts.repository.PostRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.NotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@RestController
public class PostController {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private PostRepository postRepository;

    // TODO add pagination
    @GetMapping("/api/posts")
    public List<PostDTO> getPosts() {
        ModelMapper modelMapper = new ModelMapper();
        return postRepository.findAll()
                .stream().map(item -> modelMapper.map(item, PostDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/posts")
    public PostCreateDTO createPost(@RequestBody @Valid PostCreateDTO postCreateDTO)
            throws JsonProcessingException {
        ModelMapper modelMapper = new ModelMapper();
        Post post = modelMapper.map(postCreateDTO, Post.class);
        postRepository.save(post);
        postCreateDTO = modelMapper.map(post, PostCreateDTO.class);
        PostEventDTO postEventDTO = new ModelMapper().map(postCreateDTO, PostEventDTO.class);
        postEventDTO.setEvent("PostCreated");
        String postEvent = new ObjectMapper().writeValueAsString(postEventDTO);
        try {
            kafkaTemplate.send(
                    KafkaTopicConfig.topic,
                    post.getId().toString(),
                    postEvent
            ).get(3, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new KafkaSendException();
        }
        return postCreateDTO;
    }

    @Transactional
    @PutMapping("/api/posts/{id}")
    public PostUpdateDTO updatePost(@RequestBody PostUpdateDTO postUpdateDTO, @PathVariable String id)
            throws NotFoundException, JsonProcessingException {
        Optional<Post> postOptional = postRepository.findById(Long.parseLong(id));
        if (!postOptional.isPresent()) {
            throw new NotFoundException(String.format("Post with id=%s is not found", id));
        }
        Post post = new ModelMapper().map(postUpdateDTO, Post.class);
        post.setId(Long.parseLong(id));
        postRepository.save(post);
        postUpdateDTO = new ModelMapper().map(post, PostUpdateDTO.class);
        PostEventDTO postEventDTO = new ModelMapper().map(postUpdateDTO, PostEventDTO.class);
        postEventDTO.setEvent("PostUpdated");
        String postEvent = new ObjectMapper().writeValueAsString(postEventDTO);
        try {
            kafkaTemplate.send(
                    KafkaTopicConfig.topic,
                    post.getId().toString(),
                    postEvent
            ).get(3, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new KafkaSendException();
        }
        return postUpdateDTO;
    }

    @Transactional
    @DeleteMapping("/api/posts/{id}")
    public PostDeleteDTO deletePost(@PathVariable String id)
            throws JsonProcessingException {
        Post post = postRepository.getOne(Long.parseLong(id));
        postRepository.deleteById(Long.parseLong(id));
        PostDeleteDTO postDeleteDTO = new ModelMapper().map(post, PostDeleteDTO.class);
        PostEventDTO postEventDTO = new ModelMapper().map(postDeleteDTO, PostEventDTO.class);
        postEventDTO.setEvent("PostDeleted");
        String postEvent = new ObjectMapper().writeValueAsString(postEventDTO);
        try {
            kafkaTemplate.send(
                    KafkaTopicConfig.topic,
                    post.getId().toString(),
                    postEvent
            ).get(3, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new KafkaSendException();
        }
        return postDeleteDTO;
    }

}
