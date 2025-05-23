package com.example.coursebe.controller;

import com.example.coursebe.model.Review;
import com.example.coursebe.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.coursebe.dto.ReviewResponse;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewController reviewController;
    
    private UUID courseId;
    private UUID userId;
    private UUID reviewId;
    private Review review;
    private ReviewResponse reviewResponse;

    @BeforeEach
    void setUp() {
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
        reviewResponse = ReviewResponse.builder()
                .id(reviewId)
                .courseId(courseId)
                .userId(userId)
                .rating(5)
                .comment("Great!")
                .createdAt(null)
                .updatedAt(null)
                .build();
    }

    @Test
    @DisplayName("GET /api/reviews/course/{courseId} paginated")
    void getReviewsByCourseId() throws Exception {
       when(reviewService.getReviewsByCourseId(courseId)).thenReturn(List.of(review));

       ResponseEntity<List<ReviewResponse>> response = reviewController.getReviewsByCourseId(courseId, 0);

       assertNotNull(response);
       assertEquals(HttpStatus.OK, response.getStatusCode());
       List<ReviewResponse> body = response.getBody();
       assertNotNull(body);
       assertFalse(body.isEmpty());
       assertEquals(reviewResponse, body.get(0));
    }

    @Test
    @DisplayName("GET /api/reviews/user/{userId}")
    void getReviewsByUserId() throws Exception {
        when(reviewService.getReviewsByUserId(userId)).thenReturn(List.of(review));
        ResponseEntity<List<ReviewResponse>> response = reviewController.getReviewsByUserId(userId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<ReviewResponse> body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isEmpty());
        assertEquals(reviewResponse, body.get(0));
    }

    @Test
    @DisplayName("GET /api/reviews/{id}")
    void getReviewById() throws Exception {
        when(reviewService.getReviewById(reviewId)).thenReturn(Optional.of(review));
        ResponseEntity<ReviewResponse> response = reviewController.getReviewById(reviewId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(reviewResponse, response.getBody());
    }

    @Test
    @DisplayName("GET /api/reviews/{id} not found")
    void getReviewByIdNotFound() throws Exception {
        when(reviewService.getReviewById(reviewId)).thenReturn(Optional.empty());
        ResponseEntity<ReviewResponse> response = reviewController.getReviewById(reviewId);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("POST /api/reviews")
    void createReview() throws Exception {
        when(reviewService.createReview(any(), any(), anyInt(), anyString())).thenReturn(review);
        ResponseEntity<ReviewResponse> response = reviewController.createReview(review);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(reviewResponse, response.getBody());
    }

    @Test
    @DisplayName("PUT /api/reviews/{id}")
    void updateReview() throws Exception {
        when(reviewService.updateReview(eq(reviewId), anyInt(), anyString())).thenReturn(Optional.of(review));
        ResponseEntity<ReviewResponse> response = reviewController.updateReview(reviewId, review);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(reviewResponse, response.getBody());
    }

    @Test
    @DisplayName("PUT /api/reviews/{id} not found")
    void updateReviewNotFound() throws Exception {
        when(reviewService.updateReview(eq(reviewId), anyInt(), anyString())).thenReturn(Optional.empty());
        ResponseEntity<ReviewResponse> response = reviewController.updateReview(reviewId, review);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("DELETE /api/reviews/{id}")
    void deleteReview() throws Exception {
        when(reviewService.deleteReview(reviewId)).thenReturn(true);
        ResponseEntity<Void> response = reviewController.deleteReview(reviewId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("DELETE /api/reviews/{id} not found")
    void deleteReviewNotFound() throws Exception {
        when(reviewService.deleteReview(reviewId)).thenReturn(false);
        ResponseEntity<Void> response = reviewController.deleteReview(reviewId);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
