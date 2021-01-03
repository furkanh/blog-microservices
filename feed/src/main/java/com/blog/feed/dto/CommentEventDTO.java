package com.blog.feed.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentEventDTO {
    private String event;
    private Long id;
    private String body;
    private Long postId;
}
