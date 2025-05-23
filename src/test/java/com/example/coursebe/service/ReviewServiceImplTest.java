package com.example.coursebe.service;

import com.example.coursebe.model.Review;
import com.example.coursebe.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReviewServiceImplTest {
    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private UUID courseId;
    private UUID userId;
    private UUID reviewId;
    private Review review;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        courseId = UUID.randomUUID();
        userId = UUID.randomUUID();
        reviewId = UUID.randomUUID();
        review = Review.builder()
                .id(reviewId)
                .courseId(courseId)
                .userId(userId)
                .rating(5)
                .comment("Great!")
                .build();
    }

    @Test
    @DisplayName("Should get reviews by courseId")
    void getReviewsByCourseId() {
        when(reviewRepository.findByCourseId(courseId)).thenReturn(Arrays.asList(review));
        List<Review> result = reviewService.getReviewsByCourseId(courseId);
        assertEquals(1, result.size());
        assertEquals(courseId, result.get(0).getCourseId());
    }

    @Test
    @DisplayName("Should get reviews by userId")
    void getReviewsByUserId() {
        when(reviewRepository.findByUserId(userId)).thenReturn(Arrays.asList(review));
        List<Review> result = reviewService.getReviewsByUserId(userId);
        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).getUserId());
    }

    @Test
    @DisplayName("Should get review by id")
    void getReviewById() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        Optional<Review> result = reviewService.getReviewById(reviewId);
        assertTrue(result.isPresent());
        assertEquals(reviewId, result.get().getId());
    }

    @Test
    @DisplayName("Should create review")
    void createReview() {
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        Review result = reviewService.createReview(courseId, userId, 5, "Great!");
        assertNotNull(result);
        assertEquals(5, result.getRating());
        assertEquals("Great!", result.getComment());
    }

    @Test
    @DisplayName("Should throw on invalid rating")
    void createReviewInvalidRating() {
        assertThrows(IllegalArgumentException.class, () -> reviewService.createReview(courseId, userId, 0, "Bad"));
        assertThrows(IllegalArgumentException.class, () -> reviewService.createReview(courseId, userId, 6, "Bad"));
    }

    @Test
    @DisplayName("Should update review")
    void updateReview() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> i.getArguments()[0]);
        Optional<Review> result = reviewService.updateReview(reviewId, 4, "Updated");
        assertTrue(result.isPresent());
        assertEquals(4, result.get().getRating());
        assertEquals("Updated", result.get().getComment());
    }

    @Test
    @DisplayName("Should return empty when updating non-existent review")
    void updateReviewNotFound() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());
        Optional<Review> result = reviewService.updateReview(reviewId, 4, "Updated");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should delete review")
    void deleteReview() {
        when(reviewRepository.existsById(reviewId)).thenReturn(true);
        doNothing().when(reviewRepository).deleteById(reviewId);
        boolean result = reviewService.deleteReview(reviewId);
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when deleting non-existent review")
    void deleteReviewNotFound() {
        when(reviewRepository.existsById(reviewId)).thenReturn(false);
        boolean result = reviewService.deleteReview(reviewId);
        assertFalse(result);
    }
}
