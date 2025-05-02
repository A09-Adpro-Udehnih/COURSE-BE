package com.example.coursebe.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ReviewTest {
    private Review review;
    private UUID id;
    private UUID courseId;
    private UUID userId;
    private String comment;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        courseId = UUID.randomUUID();
        userId = UUID.randomUUID();
        comment = "Great course!";
        review = Review.builder()
                .id(id)
                .courseId(courseId)
                .userId(userId)
                .rating(5)
                .comment(comment)
                .build();
    }

    @Test
    void testConstructor() {
        assertNotNull(review.getId());
        assertEquals(courseId, review.getCourseId());
        assertEquals(userId, review.getUserId());
        assertEquals(5, review.getRating());
        assertEquals(comment, review.getComment());
    }

    @Test
    void testSettersAndGetters() {
        UUID newCourseId = UUID.randomUUID();
        UUID newUserId = UUID.randomUUID();
        review.setCourseId(newCourseId);
        review.setUserId(newUserId);
        review.setRating(4);
        review.setComment("Updated comment");
        assertEquals(newCourseId, review.getCourseId());
        assertEquals(newUserId, review.getUserId());
        assertEquals(4, review.getRating());
        assertEquals("Updated comment", review.getComment());
    }

    @Test
    void testRatingValidation() {
        assertThrows(IllegalArgumentException.class, () -> review.setRating(0));
        assertThrows(IllegalArgumentException.class, () -> review.setRating(6));
    }

    @Test
    void testOnCreateAndOnUpdate() {
        review.onCreate();
        assertNotNull(review.getCreatedAt());
        assertNotNull(review.getUpdatedAt());
        LocalDateTime initialUpdate = review.getUpdatedAt();
        try { Thread.sleep(10); } catch (InterruptedException e) { }
        review.onUpdate();
        assertTrue(review.getUpdatedAt().isAfter(initialUpdate));
    }
}
