package com.example.coursebe.controller;

import com.example.coursebe.model.Review;
import com.example.coursebe.service.ReviewService;
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
    public ResponseEntity<List<Review>> getReviewsByCourseId(
            @PathVariable UUID courseId,
            @RequestParam(defaultValue = "0") int page
    ) {
        Pageable pageable = PageRequest.of(page, 10);
        List<Review> all = reviewService.getReviewsByCourseId(courseId);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), all.size());
        if (start > end) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(all.subList(start, end));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Review>> getReviewsByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(reviewService.getReviewsByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable UUID id) {
        Optional<Review> review = reviewService.getReviewById(id);
        return review.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Review> createReview(@RequestBody Review review) {
        Review created = reviewService.createReview(
                review.getCourseId(),
                review.getUserId(),
                review.getRating(),
                review.getComment()
        );
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(
            @PathVariable UUID id,
            @RequestBody Review review
    ) {
        Optional<Review> updated = reviewService.updateReview(id, review.getRating(), review.getComment());
        return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID id) {
        boolean deleted = reviewService.deleteReview(id);
        if (deleted) return ResponseEntity.noContent().build();
        return ResponseEntity.notFound().build();
    }
}
