package com.blog.posts;

import com.blog.posts.model.Post;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.function.Consumer;
import java.util.function.Supplier;

@SpringBootApplication
public class PostsApplication {

	public static void main(String[] args) {
		SpringApplication.run(PostsApplication.class, args);
	}

}
