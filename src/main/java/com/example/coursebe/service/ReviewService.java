package com.example.coursebe.service;

import com.example.coursebe.model.Review;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewService {
    List<Review> getReviewsByCourseId(UUID courseId);
    List<Review> getReviewsByUserId(UUID userId);
    Optional<Review> getReviewById(UUID id);
    Review createReview(UUID courseId, UUID userId, int rating, String comment);
    Optional<Review> updateReview(UUID id, Integer rating, String comment);
    boolean deleteReview(UUID id);
}
