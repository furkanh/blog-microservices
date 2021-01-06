package com.blog.posts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PostCreateDTO {
    private Long id;
    @NotBlank(message = "Title cannot be empty.")
    private String title;
    @NotBlank(message = "Body cannot be empty.")
    private String body;
}
