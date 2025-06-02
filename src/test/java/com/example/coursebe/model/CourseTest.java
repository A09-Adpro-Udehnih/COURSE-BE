package com.example.coursebe.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CourseTest {
    
    private Course course;
    private UUID tutorId;
    private String name;
    private String description;
    private BigDecimal price;
    
    @BeforeEach
    void setUp() {
        tutorId = UUID.randomUUID();
        name = "Test Course";
        description = "Test Description";
        price = new BigDecimal("99.99");
        course = new Course(name, description, tutorId, price);
    }
    
    @Test
    void testConstructor() {
        assertNotNull(course.getId());
        assertEquals(name, course.getName());
        assertEquals(description, course.getDescription());
        assertEquals(tutorId, course.getTutorId());
        assertEquals(price, course.getPrice());
        assertNotNull(course.getSections());
        assertTrue(course.getSections().isEmpty());
        assertNotNull(course.getEnrollments());
        assertTrue(course.getEnrollments().isEmpty());
    }
    
    @Test
    void testDefaultConstructor() {
        Course defaultCourse = new Course();
        assertNotNull(defaultCourse.getId());
        assertNotNull(defaultCourse.getSections());
        assertNotNull(defaultCourse.getEnrollments());
    }
    
    @Test
    void testOnCreate() {
        course.onCreate();
        assertNotNull(course.getCreatedAt());
        assertNotNull(course.getUpdatedAt());
        // Check if timestamps are very close instead of exactly equal
        assertTrue(java.time.Duration.between(course.getCreatedAt(), course.getUpdatedAt()).abs().toMillis() < 1000);
        
        Course courseWithNullPrice = new Course(name, description, tutorId, null);
        courseWithNullPrice.onCreate();
        assertEquals(BigDecimal.ZERO, courseWithNullPrice.getPrice());
    }
    
    @Test
    void testOnUpdate() {
        course.onCreate(); // Set initial timestamps
        LocalDateTime initialUpdateTime = course.getUpdatedAt();
        
        // Wait a small amount of time to ensure timestamp difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        course.onUpdate();
        assertTrue(course.getUpdatedAt().isAfter(initialUpdateTime));
    }
    
    @Test
    void testSetters() {
        String newName = "Updated Course Name";
        course.setName(newName);
        assertEquals(newName, course.getName());
        
        String newDescription = "Updated Course Description";
        course.setDescription(newDescription);
        assertEquals(newDescription, course.getDescription());
        
        UUID newTutorId = UUID.randomUUID();
        course.setTutorId(newTutorId);
        assertEquals(newTutorId, course.getTutorId());
        
        BigDecimal newPrice = new BigDecimal("149.99");
        course.setPrice(newPrice);
        assertEquals(newPrice, course.getPrice());
    }
}