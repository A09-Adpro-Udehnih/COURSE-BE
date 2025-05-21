package com.example.coursebe.controller;

import com.example.coursebe.dto.GlobalResponse;
import com.example.coursebe.dto.review.ReviewResponse;
import com.example.coursebe.model.Review;
import com.example.coursebe.service.ReviewService;
import com.example.coursebe.dto.review.ReviewCreateRequest;
import com.example.coursebe.dto.review.ReviewUpdateRequest;
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

       ResponseEntity<GlobalResponse<List<ReviewResponse>>> response = reviewController.getReviewsByCourseId(courseId, 0);

       assertNotNull(response);
       assertEquals(HttpStatus.OK, response.getStatusCode());
       GlobalResponse<List<ReviewResponse>> body = response.getBody();
       assertNotNull(body);
       assertTrue(body.isSuccess());
       assertNotNull(body.getData());
       assertFalse(body.getData().isEmpty());
       assertEquals(reviewResponse, body.getData().get(0));
    }

    @Test
    @DisplayName("GET /api/reviews/user/{userId}")
    void getReviewsByUserId() throws Exception {
        when(reviewService.getReviewsByUserId(userId)).thenReturn(List.of(review));
        ResponseEntity<GlobalResponse<List<ReviewResponse>>> response = reviewController.getReviewsByUserId(userId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        GlobalResponse<List<ReviewResponse>> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        assertNotNull(body.getData());
        assertFalse(body.getData().isEmpty());
        assertEquals(reviewResponse, body.getData().get(0));
    }

    @Test
    @DisplayName("GET /api/reviews/{id}")
    void getReviewById() throws Exception {
        when(reviewService.getReviewById(reviewId)).thenReturn(Optional.of(review));
        ResponseEntity<GlobalResponse<ReviewResponse>> response = reviewController.getReviewById(reviewId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        GlobalResponse<ReviewResponse> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        assertEquals(reviewResponse, body.getData());
    }

    @Test
    @DisplayName("GET /api/reviews/{id} not found")
    void getReviewByIdNotFound() throws Exception {
        when(reviewService.getReviewById(reviewId)).thenReturn(Optional.empty());
        ResponseEntity<GlobalResponse<ReviewResponse>> response = reviewController.getReviewById(reviewId);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        GlobalResponse<ReviewResponse> body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertNull(body.getData());
    }

    @Test
    @DisplayName("POST /api/reviews")
    void createReview() throws Exception {
        when(reviewService.createReview(any(), any(), anyInt(), anyString())).thenReturn(review);
        ReviewCreateRequest req = new ReviewCreateRequest();
        req.setCourseId(courseId);
        req.setUserId(userId);
        req.setRating(5);
        req.setComment("Great!");
        ResponseEntity<GlobalResponse<ReviewResponse>> response = reviewController.createReview(req);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        GlobalResponse<ReviewResponse> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        assertEquals(reviewResponse, body.getData());
    }

    @Test
    @DisplayName("PUT /api/reviews/{id}")
    void updateReview() throws Exception {
        when(reviewService.updateReview(eq(reviewId), anyInt(), anyString())).thenReturn(Optional.of(review));
        ReviewUpdateRequest req = new ReviewUpdateRequest();
        req.setRating(5);
        req.setComment("Great!");
        ResponseEntity<GlobalResponse<ReviewResponse>> response = reviewController.updateReview(reviewId, req);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        GlobalResponse<ReviewResponse> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        assertEquals(reviewResponse, body.getData());
    }

    @Test
    @DisplayName("PUT /api/reviews/{id} not found")
    void updateReviewNotFound() throws Exception {
        when(reviewService.updateReview(eq(reviewId), anyInt(), anyString())).thenReturn(Optional.empty());
        ReviewUpdateRequest req = new ReviewUpdateRequest();
        req.setRating(5);
        req.setComment("Great!");
        ResponseEntity<GlobalResponse<ReviewResponse>> response = reviewController.updateReview(reviewId, req);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        GlobalResponse<ReviewResponse> body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertNull(body.getData());
    }

    @Test
    @DisplayName("DELETE /api/reviews/{id}")
    void deleteReview() throws Exception {
        when(reviewService.deleteReview(reviewId)).thenReturn(true);
        ResponseEntity<GlobalResponse<Void>> response = reviewController.deleteReview(reviewId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        GlobalResponse<Void> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
    }

    @Test
    @DisplayName("DELETE /api/reviews/{id} not found")
    void deleteReviewNotFound() throws Exception {
        when(reviewService.deleteReview(reviewId)).thenReturn(false);
        ResponseEntity<GlobalResponse<Void>> response = reviewController.deleteReview(reviewId);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        GlobalResponse<Void> body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
    }
}
