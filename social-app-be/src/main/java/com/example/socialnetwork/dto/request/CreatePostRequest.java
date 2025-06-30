package com.example.socialnetwork.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreatePostRequest {
    private String content;
    private MultipartFile image;
}