package com.blog.feed.controller;

import com.blog.feed.dto.FeedItemDTO;
import com.blog.feed.model.Post;
import com.blog.feed.repository.PostRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class FeedController {

    @Autowired
    private PostRepository postRepository;

    // TODO sort with numberOfComments
    // TODO add pagination
    @GetMapping("/api/feed")
    public List<FeedItemDTO> getComments() {
        List<Post> posts = postRepository.findAll();
        return posts.stream().map(item -> {
            FeedItemDTO feedItemDTO = new ModelMapper().map(item, FeedItemDTO.class);
            feedItemDTO.setNumberOfComments(item.getComments().size());
            return feedItemDTO;
        }).collect(Collectors.toList());
    }
}
