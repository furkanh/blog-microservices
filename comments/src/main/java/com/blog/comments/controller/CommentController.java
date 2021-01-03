package com.blog.comments.controller;

import com.blog.comments.config.KafkaTopicConfig;
import com.blog.comments.dto.CommentCreateDTO;
import com.blog.comments.dto.CommentDTO;
import com.blog.comments.dto.CommentEventDTO;
import com.blog.comments.model.Comment;
import com.blog.comments.model.Post;
import com.blog.comments.repository.CommentRepository;
import com.blog.comments.repository.PostRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class CommentController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    // TODO add pagination
    @GetMapping("/api/comments")
    public List<CommentDTO> getComments(@RequestParam String postId) {
        Post post = postRepository.getOne(Long.parseLong(postId));
        return post.getComments().stream().map(item -> {
            return new ModelMapper().map(item, CommentDTO.class);
        }).collect(Collectors.toList());
    }

    @Transactional
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/comments")
    public CommentCreateDTO putComment(@RequestBody CommentCreateDTO commentCreateDTO, @RequestParam String postId)
            throws JsonProcessingException {
        Post post = postRepository.getOne(Long.parseLong(postId));
        Comment comment = new ModelMapper().map(commentCreateDTO, Comment.class);
        comment.setPost(post);
        commentRepository.save(comment);
        commentCreateDTO = new ModelMapper().map(comment, CommentCreateDTO.class);
        CommentEventDTO commentEventDTO = new ModelMapper().map(commentCreateDTO, CommentEventDTO.class);
        commentEventDTO.setEvent("CommentCreated");
        commentEventDTO.setPostId(post.getId());
        kafkaTemplate.send(
                KafkaTopicConfig.topic,
                comment.getId().toString(),
                new ObjectMapper().writeValueAsString(commentEventDTO)
        );
        return commentCreateDTO;
    }

}
