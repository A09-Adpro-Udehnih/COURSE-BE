package com.example.coursebe.controller;

import com.example.coursebe.dto.GlobalResponse;
import com.example.coursebe.dto.review.ReviewResponse;
import com.example.coursebe.model.Review;
import com.example.coursebe.service.ReviewService;
import com.example.coursebe.dto.review.ReviewCreateRequest;
import com.example.coursebe.dto.review.ReviewUpdateRequest;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<GlobalResponse<List<ReviewResponse>>> getReviewsByCourseId(
            @PathVariable UUID courseId,
            @RequestParam(defaultValue = "0") int page
    ) {
        Pageable pageable = PageRequest.of(page, 10);
        List<Review> all = reviewService.getReviewsByCourseId(courseId);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), all.size());
        if (start > end) {
            return ResponseEntity.ok(GlobalResponse.<List<ReviewResponse>>builder()
                    .code(org.springframework.http.HttpStatus.OK)
                    .success(true)
                    .message("No reviews found.")
                    .data(List.of())
                    .build());
        }
        List<ReviewResponse> responses = all.subList(start, end).stream().map(this::toResponse).toList();
        return ResponseEntity.ok(GlobalResponse.<List<ReviewResponse>>builder()
                .code(org.springframework.http.HttpStatus.OK)
                .success(true)
                .message("Reviews fetched successfully.")
                .data(responses)
                .build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<GlobalResponse<List<ReviewResponse>>> getReviewsByUserId(@PathVariable UUID userId) {
        List<ReviewResponse> responses = reviewService.getReviewsByUserId(userId).stream().map(this::toResponse).toList();
        return ResponseEntity.ok(GlobalResponse.<List<ReviewResponse>>builder()
                .code(org.springframework.http.HttpStatus.OK)
                .success(true)
                .message("Reviews fetched successfully.")
                .data(responses)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GlobalResponse<ReviewResponse>> getReviewById(@PathVariable UUID id) {
        Optional<Review> review = reviewService.getReviewById(id);
        return review.map(r -> ResponseEntity.ok(GlobalResponse.<ReviewResponse>builder()
                        .code(org.springframework.http.HttpStatus.OK)
                        .success(true)
                        .message("Review found.")
                        .data(toResponse(r))
                        .build()))
                .orElseGet(() -> ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                        .body(GlobalResponse.<ReviewResponse>builder()
                                .code(org.springframework.http.HttpStatus.NOT_FOUND)
                                .success(false)
                                .message("Review not found.")
                                .data(null)
                                .build()));
    }

    @PostMapping
    public ResponseEntity<GlobalResponse<ReviewResponse>> createReview(@Valid @RequestBody ReviewCreateRequest request) {
        Review created = reviewService.createReview(
                request.getCourseId(),
                request.getUserId(),
                request.getRating(),
                request.getComment()
        );
        return ResponseEntity.ok(GlobalResponse.<ReviewResponse>builder()
                .code(org.springframework.http.HttpStatus.OK)
                .success(true)
                .message("Review created successfully.")
                .data(toResponse(created))
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<GlobalResponse<ReviewResponse>> updateReview(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        Optional<Review> updated = reviewService.updateReview(id, request.getRating(), request.getComment());
        return updated.map(r -> ResponseEntity.ok(GlobalResponse.<ReviewResponse>builder()
                        .code(org.springframework.http.HttpStatus.OK)
                        .success(true)
                        .message("Review updated successfully.")
                        .data(toResponse(r))
                        .build()))
                .orElseGet(() -> ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                        .body(GlobalResponse.<ReviewResponse>builder()
                                .code(org.springframework.http.HttpStatus.NOT_FOUND)
                                .success(false)
                                .message("Review not found.")
                                .data(null)
                                .build()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GlobalResponse<Void>> deleteReview(@PathVariable UUID id) {
        boolean deleted = reviewService.deleteReview(id);
        if (deleted) {
            return ResponseEntity.ok(GlobalResponse.<Void>builder()
                    .code(org.springframework.http.HttpStatus.OK)
                    .success(true)
                    .message("Review deleted successfully.")
                    .data(null)
                    .build());
        }
        return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                .body(GlobalResponse.<Void>builder()
                        .code(org.springframework.http.HttpStatus.NOT_FOUND)
                        .success(false)
                        .message("Review not found.")
                        .data(null)
                        .build());
    }

    private ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .courseId(review.getCourseId())
                .userId(review.getUserId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
