package com.blog.feed.listener;

import com.blog.feed.dto.PostEventDTO;
import com.blog.feed.model.Post;
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
public class PostListener {

    @Autowired
    private PostRepository postRepository;

    @Transactional
    @KafkaListener(topics = "posts", groupId = "feed")
    public void postListener(@Payload String message) throws JsonProcessingException {
        PostEventDTO postEventDto = new ObjectMapper().readValue(message, PostEventDTO.class);
        Post post = new ModelMapper().map(postEventDto, Post.class);
        if (postEventDto.getEvent().equals("PostCreated")
                || postEventDto.getEvent().equals("PostUpdated")) {
            postRepository.save(post);
        }
        else if (postEventDto.getEvent().equals("PostDeleted")) {
            postRepository.deleteById(post.getId());
        }
    }
}
