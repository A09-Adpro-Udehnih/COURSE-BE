package com.example.coursebe.controller;

import com.example.coursebe.model.Review;
import com.example.coursebe.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID courseId;
    private UUID userId;
    private UUID reviewId;
    private Review review;

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
    }

    @Test
    @DisplayName("GET /api/reviews/course/{courseId} paginated")
    void getReviewsByCourseId() throws Exception {
        List<Review> reviews = Arrays.asList(review, review, review, review, review, review, review, review, review, review, review);
        when(reviewService.getReviewsByCourseId(courseId)).thenReturn(reviews);
        mockMvc.perform(get("/api/reviews/course/" + courseId + "?page=0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)));
        mockMvc.perform(get("/api/reviews/course/" + courseId + "?page=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/reviews/user/{userId}")
    void getReviewsByUserId() throws Exception {
        when(reviewService.getReviewsByUserId(userId)).thenReturn(List.of(review));
        mockMvc.perform(get("/api/reviews/user/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId", is(userId.toString())));
    }

    @Test
    @DisplayName("GET /api/reviews/{id}")
    void getReviewById() throws Exception {
        when(reviewService.getReviewById(reviewId)).thenReturn(Optional.of(review));
        mockMvc.perform(get("/api/reviews/" + reviewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(reviewId.toString())));
    }

    @Test
    @DisplayName("GET /api/reviews/{id} not found")
    void getReviewByIdNotFound() throws Exception {
        when(reviewService.getReviewById(reviewId)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/reviews/" + reviewId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/reviews")
    void createReview() throws Exception {
        when(reviewService.createReview(any(), any(), anyInt(), anyString())).thenReturn(review);
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(review)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(reviewId.toString())));
    }

    @Test
    @DisplayName("PUT /api/reviews/{id}")
    void updateReview() throws Exception {
        when(reviewService.updateReview(eq(reviewId), anyInt(), anyString())).thenReturn(Optional.of(review));
        mockMvc.perform(put("/api/reviews/" + reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(review)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(reviewId.toString())));
    }

    @Test
    @DisplayName("PUT /api/reviews/{id} not found")
    void updateReviewNotFound() throws Exception {
        when(reviewService.updateReview(eq(reviewId), anyInt(), anyString())).thenReturn(Optional.empty());
        mockMvc.perform(put("/api/reviews/" + reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(review)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/reviews/{id}")
    void deleteReview() throws Exception {
        when(reviewService.deleteReview(reviewId)).thenReturn(true);
        mockMvc.perform(delete("/api/reviews/" + reviewId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/reviews/{id} not found")
    void deleteReviewNotFound() throws Exception {
        when(reviewService.deleteReview(reviewId)).thenReturn(false);
        mockMvc.perform(delete("/api/reviews/" + reviewId))
                .andExpect(status().isNotFound());
    }
}
