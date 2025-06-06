package com.example.coursebe.service;

import com.example.coursebe.model.Review;
import com.example.coursebe.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Override
    public List<Review> getReviewsByCourseId(UUID courseId) {
        return reviewRepository.findByCourseId(courseId);
    }

    @Override
    public List<Review> getReviewsByUserId(UUID userId) {
        return reviewRepository.findByUserId(userId);
    }

    @Override
    public Optional<Review> getReviewById(UUID id) {
        return reviewRepository.findById(id);
    }

    @Override
    @Transactional
    public Review createReview(UUID courseId, UUID userId, int rating, String comment) {
        Review review = Review.builder()
                .courseId(courseId)
                .userId(userId)
                .rating(rating)
                .comment(comment)
                .build();
        return reviewRepository.save(review);
    }

    @Override
    @Transactional
    public Optional<Review> updateReview(UUID id, Integer rating, String comment) {
        Optional<Review> optionalReview = reviewRepository.findById(id);
        if (optionalReview.isEmpty()) {
            return Optional.empty();
        }
        Review review = optionalReview.get();
        if (rating != null) review.setRating(rating);
        if (comment != null) review.setComment(comment);
        Review updated = reviewRepository.save(review);
        return Optional.of(updated);
    }

    @Override
    @Transactional
    public boolean deleteReview(UUID id) {
        if (reviewRepository.existsById(id)) {
            reviewRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
