package com.example.socialnetwork.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdatePostRequest {
    @NotBlank(message = "Content cannot be blank")
    private String content;
}