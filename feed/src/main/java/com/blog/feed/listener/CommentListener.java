package com.blog.feed.listener;

import com.blog.feed.dto.CommentEventDTO;
import com.blog.feed.dto.PostEventDTO;
import com.blog.feed.model.Comment;
import com.blog.feed.model.Post;
import com.blog.feed.repository.CommentRepository;
import com.blog.feed.repository.PostRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentListener {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Transactional
    @KafkaListener(topics = "comments", groupId = "feed")
    public void commentListener(@Payload String message) throws JsonProcessingException {
        CommentEventDTO commentEventDTO = new ObjectMapper().readValue(message, CommentEventDTO.class);
        Comment comment = new ModelMapper().map(commentEventDTO, Comment.class);
        Post post = postRepository.findById(commentEventDTO.getPostId()).orElseThrow();
        comment.setPost(post);
        if (commentEventDTO.getEvent().equals("CommentCreated")
                || commentEventDTO.getEvent().equals("CommentUpdated")) {
            commentRepository.save(comment);
        }
        else if (commentEventDTO.getEvent().equals("CommentDeleted")) {
            commentRepository.deleteById(comment.getId());
        }
    }

}
