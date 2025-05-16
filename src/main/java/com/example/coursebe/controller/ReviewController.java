package com.example.coursebe.controller;

import com.example.coursebe.model.Review;
import com.example.coursebe.service.ReviewService;
import com.example.coursebe.dto.ReviewResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByCourseId(
            @PathVariable UUID courseId,
            @RequestParam(defaultValue = "0") int page
    ) {
        Pageable pageable = PageRequest.of(page, 10);
        List<Review> all = reviewService.getReviewsByCourseId(courseId);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), all.size());
        if (start > end) return ResponseEntity.ok(List.of());
        List<ReviewResponse> responses = all.subList(start, end).stream().map(this::toResponse).toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByUserId(@PathVariable UUID userId) {
        List<ReviewResponse> responses = reviewService.getReviewsByUserId(userId).stream().map(this::toResponse).toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponse> getReviewById(@PathVariable UUID id) {
        Optional<Review> review = reviewService.getReviewById(id);
        return review.map(r -> ResponseEntity.ok(toResponse(r))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(@RequestBody Review review) {
        Review created = reviewService.createReview(
                review.getCourseId(),
                review.getUserId(),
                review.getRating(),
                review.getComment()
        );
        return ResponseEntity.ok(toResponse(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable UUID id,
            @RequestBody Review review
    ) {
        Optional<Review> updated = reviewService.updateReview(id, review.getRating(), review.getComment());
        return updated.map(r -> ResponseEntity.ok(toResponse(r))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID id) {
        boolean deleted = reviewService.deleteReview(id);
        if (deleted) return ResponseEntity.ok().build();
        return ResponseEntity.notFound().build();
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
