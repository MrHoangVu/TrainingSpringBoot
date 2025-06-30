package com.example.socialnetwork.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentRequest {
    @NotBlank(message = "Comment content cannot be blank")
    private String content;
}