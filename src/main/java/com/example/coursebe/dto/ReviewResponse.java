package com.example.coursebe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponse {
    private UUID id;
    private UUID courseId;
    private UUID userId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 