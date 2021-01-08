package com.blog.posts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PostUpdateDTO {
    private Long id;
    @NotBlank(message = "Title cannot be empty.")
    private String title;
    @NotBlank(message = "Body cannot be empty.")
    private String body;
}
