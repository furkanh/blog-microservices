package com.blog.posts.controller;

import com.blog.posts.exception.KafkaSendException;
import com.blog.posts.listener.PostListener;
import com.blog.posts.repository.PostRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(
        partitions = 1,
        brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" }
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
}
