package com.example.coursebe.dto.review;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class ReviewCreateRequest {
    @NotNull(message = "Course ID is required")
    private UUID courseId;

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    @NotBlank(message = "Comment is required")
    private String comment;
} 