package com.blog.posts.controller;

import com.blog.posts.dto.PostCreateDTO;
import com.blog.posts.listener.PostListener;
import com.blog.posts.model.Post;
import com.blog.posts.repository.PostRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(
        partitions = 1,
        brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" }
)
@AutoConfigureMockMvc
@AutoConfigureDataJpa
class PostControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostListener postListener;

    @Test
    public void createPost_returnsTheCreatedContentWithCreatedStatus() throws Exception {
        InputStream inputStream = new ClassPathResource("posts/post1.json").getInputStream();
        String postJson = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
        String response = mockMvc.perform(
                post("/api/posts").contentType(MediaType.APPLICATION_JSON)
                        .content(postJson)
        ).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        PostCreateDTO postCreateDTO = new ObjectMapper().readValue(response, PostCreateDTO.class);
        assertEquals(postCreateDTO.getId(), 1L);
        assertEquals(postCreateDTO.getTitle(), "post 1");
        assertEquals(postCreateDTO.getBody(), "body of post 1");
    }

    @Test
    public void createPost_createsSingleRecordOnDatabase() throws Exception {
        InputStream inputStream = new ClassPathResource("posts/post1.json").getInputStream();
        String postJson = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
        mockMvc.perform(
                post("/api/posts").contentType(MediaType.APPLICATION_JSON)
                        .content(postJson)
        );
        assertEquals(postRepository.count(), 1);
        Optional<Post> postOptional = postRepository.findById(1L);
        assertEquals(postOptional.isPresent(), true);
        Post post = postOptional.get();
        assertEquals(post.getTitle(), "post 1");
        assertEquals(post.getBody(), "body of post 1");
    }

    @Test
    public void createPost_returnsBadRequestOnMissingTitle() throws Exception {
        InputStream inputStream = new ClassPathResource("posts/post-title-missing.json").getInputStream();
        String postJson = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
        mockMvc.perform(
                post("/api/posts").contentType(MediaType.APPLICATION_JSON)
                        .content(postJson)
        ).andExpect(status().isBadRequest());
        assertEquals(postRepository.count(), 0);
    }

    @Test
    public void createPost_returnsBadRequestOnMissingBody() throws Exception {
        InputStream inputStream = new ClassPathResource("posts/post-body-missing.json").getInputStream();
        String postJson = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
        mockMvc.perform(
                post("/api/posts").contentType(MediaType.APPLICATION_JSON)
                        .content(postJson)
        ).andExpect(status().isBadRequest());
        assertEquals(postRepository.count(), 0);
    }

}
