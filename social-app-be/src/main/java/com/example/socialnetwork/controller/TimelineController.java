package com.example.socialnetwork.controller;

import com.example.socialnetwork.dto.response.PostResponse;
import com.example.socialnetwork.service.TimelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/timeline")
@RequiredArgsConstructor
@Tag(name = "6. Timeline", description = "API for fetching user's timeline")
@SecurityRequirement(name = "bearerAuth")
public class TimelineController {

    private final TimelineService timelineService;

    @Operation(summary = "Get the main timeline feed for the current user")
    @GetMapping("")
    public ResponseEntity<Page<PostResponse>> getTimeline(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(timelineService.getTimeline(pageable));
    }
}