package com.example.coursebe.repository;

import com.example.coursebe.model.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ReviewRepository
 */
@DataJpaTest
public class ReviewRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReviewRepository reviewRepository;

    private UUID courseId1;
    private UUID courseId2;
    private UUID userId1;
    private UUID userId2;
    private Review review1;
    private Review review2;
    private Review review3;

    @BeforeEach
    void setUp() {
        courseId1 = UUID.randomUUID();
        courseId2 = UUID.randomUUID();
        userId1 = UUID.randomUUID();
        userId2 = UUID.randomUUID();

        review1 = Review.builder()
                .courseId(courseId1)
                .userId(userId1)
                .rating(5)
                .comment("Excellent!")
                .build();
        review2 = Review.builder()
                .courseId(courseId1)
                .userId(userId2)
                .rating(4)
                .comment("Good course")
                .build();
        review3 = Review.builder()
                .courseId(courseId2)
                .userId(userId1)
                .rating(3)
                .comment("Average")
                .build();

        entityManager.persist(review1);
        entityManager.persist(review2);
        entityManager.persist(review3);
        entityManager.flush();
    }

    @Test
    @DisplayName("Should find all reviews by courseId")
    void findByCourseId() {
        List<Review> reviews = reviewRepository.findByCourseId(courseId1);
        assertEquals(2, reviews.size());
        assertTrue(reviews.stream().allMatch(r -> r.getCourseId().equals(courseId1)));
    }

    @Test
    @DisplayName("Should find all reviews by userId")
    void findByUserId() {
        List<Review> reviews = reviewRepository.findByUserId(userId1);
        assertEquals(2, reviews.size());
        assertTrue(reviews.stream().allMatch(r -> r.getUserId().equals(userId1)));
    }

    @Test
    @DisplayName("Should save and find review by id")
    void saveAndFindById() {
        Review newReview = Review.builder()
                .courseId(courseId2)
                .userId(userId2)
                .rating(2)
                .comment("Not great")
                .build();
        Review saved = reviewRepository.save(newReview);
        Optional<Review> found = reviewRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Not great", found.get().getComment());
    }

    @Test
    @DisplayName("Should update review")
    void updateReview() {
        review1.setComment("Updated comment");
        review1.setRating(4);
        Review updated = reviewRepository.save(review1);
        assertEquals("Updated comment", updated.getComment());
        assertEquals(4, updated.getRating());
    }

    @Test
    @DisplayName("Should delete review")
    void deleteReview() {
        reviewRepository.delete(review2);
        Optional<Review> found = reviewRepository.findById(review2.getId());
        assertFalse(found.isPresent());
    }
}
