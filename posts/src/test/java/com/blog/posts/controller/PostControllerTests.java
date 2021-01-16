package com.blog.posts.controller;

import com.blog.posts.dto.*;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(
        partitions = 1,
        bootstrapServersProperty = "spring.kafka.bootstrap-servers",
        topics = {"posts"}
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

    @Test
    public void createPost_emitsAnEventToKafka() throws Exception {
        InputStream inputStream = new ClassPathResource("posts/post1.json").getInputStream();
        String postJson = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
        mockMvc.perform(
                post("/api/posts").contentType(MediaType.APPLICATION_JSON)
                        .content(postJson)
        );
        assertEquals(postRepository.count(), 1);
        postListener.getLatch().await(10, TimeUnit.SECONDS);
        String payload = postListener.getPayload();
        assertNotNull(payload);
        PostEventDTO postEventDTO = new ObjectMapper().readValue(payload, PostEventDTO.class);
        assertEquals(postEventDTO.getEvent(), "PostCreated");
        assertEquals(postEventDTO.getId(), 1L);
        assertEquals(postEventDTO.getTitle(), "post 1");
        assertEquals(postEventDTO.getBody(), "body of post 1");
    }

    @Test
    public void getPost_returnsThePostGivenId() throws Exception {
        InputStream inputStream = new ClassPathResource("posts/post1.json").getInputStream();
        String postJson = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
        Post post = new ObjectMapper().readValue(postJson, Post.class);
        postRepository.save(post);
        String response = mockMvc.perform(
                get("/api/posts/1").contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        PostDTO postDTO = new ObjectMapper().readValue(response, PostDTO.class);
        assertEquals(postDTO.getId(), 1L);
        assertEquals(postDTO.getTitle(), "post 1");
        assertEquals(postDTO.getBody(), "body of post 1");
    }

    @Test
    public void getPost_returnsIsNotFoundWhenNonExistingIdIsGiven() throws Exception {
        InputStream inputStream = new ClassPathResource("posts/post1.json").getInputStream();
        String postJson = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
        Post post = new ObjectMapper().readValue(postJson, Post.class);
        postRepository.save(post);
        mockMvc.perform(
                get("/api/posts/2").contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound());
    }

    @Test
    public void updatePost_returnsIsNotFoundWhenNonExistingIdIsGiven() throws Exception {
        InputStream inputStream = new ClassPathResource("posts/post1.json").getInputStream();
        String postJson = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
        Post post = new ObjectMapper().readValue(postJson, Post.class);
        postRepository.save(post);
        mockMvc.perform(
                put("/api/posts/2").contentType(MediaType.APPLICATION_JSON)
                        .content(postJson)
        ).andExpect(status().isNotFound());
    }

    @Test
    public void updatePost_updatesThePost() throws Exception {
        InputStream inputStream = new ClassPathResource("posts/post1.json").getInputStream();
        String postJson = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
        Post post = new ObjectMapper().readValue(postJson, Post.class);
        postRepository.save(post);
        inputStream = new ClassPathResource("posts/post2.json").getInputStream();
        postJson = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
        String response = mockMvc.perform(
                put("/api/posts/1").contentType(MediaType.APPLICATION_JSON)
                        .content(postJson)
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        PostUpdateDTO postUpdateDTO = new ObjectMapper().readValue(response, PostUpdateDTO.class);
        assertEquals(postUpdateDTO.getId(), 1L);
        assertEquals(postUpdateDTO.getTitle(), "post 2");
        assertEquals(postUpdateDTO.getBody(), "body of post 2");
    }

    @Test
    public void updatePost_returnsBadRequestOnMissingTitle() throws Exception {
        InputStream inputStream = new ClassPathResource("posts/post1.json").getInputStream();
        String postJson = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
        Post post = new ObjectMapper().readValue(postJson, Post.class);
        postRepository.save(post);
        inputStream = new ClassPathResource("posts/post-title-missing.json").getInputStream();
        postJson = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
        mockMvc.perform(
                put("/api/posts/1").contentType(MediaType.APPLICATION_JSON)
                        .content(postJson)
        ).andExpect(status().isBadRequest());
        Optional<Post> postOptional = postRepository.findById(1L);
        assertEquals(postOptional.isPresent(), true);
        post = postOptional.get();
        assertEquals(post.getId(), 1L);
        assertEquals(post.getTitle(), "post 1");
        assertEquals(post.getBody(), "body of post 1");
    }

    @Test
    public void updatePost_returnsBadRequestOnMissingBody() throws Exception {
        InputStream inputStream = new ClassPathResource("posts/post1.json").getInputStream();
        String postJson = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
        Post post = new ObjectMapper().readValue(postJson, Post.class);
        postRepository.save(post);
        inputStream = new ClassPathResource("posts/post-body-missing.json").getInputStream();
        postJson = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
        mockMvc.perform(
                put("/api/posts/1").contentType(MediaType.APPLICATION_JSON)
                        .content(postJson)
        ).andExpect(status().isBadRequest());
        Optional<Post> postOptional = postRepository.findById(1L);
        assertTrue(postOptional.isPresent());
        post = postOptional.get();
        assertEquals(post.getId(), 1L);
        assertEquals(post.getTitle(), "post 1");
        assertEquals(post.getBody(), "body of post 1");
    }

    @Test
    public void updatePost_emitsAnEventToKafka() throws Exception {
        InputStream inputStream = new ClassPathResource("posts/post1.json").getInputStream();
        String postJson = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
        Post post = new ObjectMapper().readValue(postJson, Post.class);
        postRepository.save(post);
        inputStream = new ClassPathResource("posts/post2.json").getInputStream();
        postJson = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
        mockMvc.perform(
                put("/api/posts/1").contentType(MediaType.APPLICATION_JSON)
                        .content(postJson)
        );
        postListener.getLatch().await(10, TimeUnit.SECONDS);
        String payload = postListener.getPayload();
        assertNotNull(payload);
        PostEventDTO postEventDTO = new ObjectMapper().readValue(payload, PostEventDTO.class);
        assertEquals(postEventDTO.getEvent(), "PostUpdated");
        assertEquals(postEventDTO.getId(), 1L);
        assertEquals(postEventDTO.getTitle(), "post 2");
        assertEquals(postEventDTO.getBody(), "body of post 2");
    }

    @Test
    public void deletePost_returnsThePostIdWithOkStatusOnSuccessfulDeletion() throws Exception {
        InputStream inputStream = new ClassPathResource("posts/post1.json").getInputStream();
        String postJson = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
        Post post = new ObjectMapper().readValue(postJson, Post.class);
        postRepository.save(post);
        String response = mockMvc.perform(
                delete("/api/posts/1").contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertEquals(postRepository.count(), 0);
        PostDeleteDTO postDeleteDTO = new ObjectMapper().readValue(response, PostDeleteDTO.class);
        assertEquals(postDeleteDTO.getId(), 1L);
        assertFalse(postRepository.findById(1L).isPresent());
    }

    @Test
    public void deletePost_returnsIsNotFoundWhenNonExistingIdIsGiven() throws Exception {
        InputStream inputStream = new ClassPathResource("posts/post1.json").getInputStream();
        String postJson = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
        Post post = new ObjectMapper().readValue(postJson, Post.class);
        postRepository.save(post);
        mockMvc.perform(
                delete("/api/posts/2").contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound());
    }

    @Test
    public void deletePost_emitsAnEventToKafka() throws Exception {
        InputStream inputStream = new ClassPathResource("posts/post1.json").getInputStream();
        String postJson = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
        Post post = new ObjectMapper().readValue(postJson, Post.class);
        postRepository.save(post);
        mockMvc.perform(
                delete("/api/posts/1").contentType(MediaType.APPLICATION_JSON)
        );
        assertEquals(postRepository.count(), 0);
        postListener.getLatch().await(10, TimeUnit.SECONDS);
        String payload = postListener.getPayload();
        assertNotNull(payload);
        PostEventDTO postEventDTO = new ObjectMapper().readValue(payload, PostEventDTO.class);
        assertEquals(postEventDTO.getEvent(), "PostDeleted");
        assertEquals(postEventDTO.getId(), 1L);
        assertNull(postEventDTO.getTitle());
        assertNull(postEventDTO.getBody());
    }

}
