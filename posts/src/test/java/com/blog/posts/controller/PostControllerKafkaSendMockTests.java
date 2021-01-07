package com.blog.posts.controller;

import com.blog.posts.exception.KafkaSendException;
import com.blog.posts.listener.PostListener;
import com.blog.posts.model.Post;
import com.blog.posts.repository.PostRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
        brokerProperties = { "listeners=PLAINTEXT://localhost:9093", "port=9093" }
)
@AutoConfigureMockMvc
@AutoConfigureDataJpa
class PostControllerKafkaSendMockTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostListener postListener;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    public void createPost_doesNotSaveToDatabaseWhenItFailsToEmitKafkaEvent() throws Exception {
        Mockito.when(kafkaTemplate.send(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenThrow(new KafkaSendException());
        InputStream inputStream = new ClassPathResource("posts/post1.json").getInputStream();
        String postJson = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
        mockMvc.perform(
                post("/api/posts").contentType(MediaType.APPLICATION_JSON)
                        .content(postJson)
        ).andExpect(status().isInternalServerError());
        postListener.getLatch().await(3, TimeUnit.SECONDS);
        assertEquals(postListener.getPayload(), null);
        assertEquals(postRepository.count(), 0);
    }

    @Test
    public void updatePost_doesNotSaveToDatabaseWhenItFailsToEmitKafkaEvent() throws Exception {
        Mockito.when(kafkaTemplate.send(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenThrow(new KafkaSendException());
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
        ).andExpect(status().isInternalServerError());
        postListener.getLatch().await(3, TimeUnit.SECONDS);
        assertEquals(postListener.getPayload(), null);
        post = postRepository.findById(1L).get();
        assertEquals(post.getId(), 1L);
        assertEquals(post.getTitle(), "post 1");
        assertEquals(post.getBody(), "body of post 1");
    }

    @Test
    public void deletePost_doesNotSaveToDatabaseWhenItFailsToEmitKafkaEvent() throws Exception {
        Mockito.when(kafkaTemplate.send(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenThrow(new KafkaSendException());
        InputStream inputStream = new ClassPathResource("posts/post1.json").getInputStream();
        String postJson = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
        Post post = new ObjectMapper().readValue(postJson, Post.class);
        postRepository.save(post);
        mockMvc.perform(
                delete("/api/posts/1").contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isInternalServerError());
        postListener.getLatch().await(3, TimeUnit.SECONDS);
        assertEquals(postListener.getPayload(), null);
        post = postRepository.findById(1L).get();
        assertEquals(post.getId(), 1L);
        assertEquals(post.getTitle(), "post 1");
        assertEquals(post.getBody(), "body of post 1");
    }
}
