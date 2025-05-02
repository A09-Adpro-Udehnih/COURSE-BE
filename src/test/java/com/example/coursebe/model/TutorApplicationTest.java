package com.example.coursebe.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TutorApplicationTest {
    
    private TutorApplication tutorApplication;
    private UUID studentId;
    
    @BeforeEach
    void setUp() {
        studentId = UUID.randomUUID();
        tutorApplication = new TutorApplication(studentId);
    }
    
    @Test
    void testConstructor() {
        assertNotNull(tutorApplication.getId());
        assertEquals(studentId, tutorApplication.getStudentId());
        assertEquals(TutorApplication.Status.PENDING, tutorApplication.getStatus());
        assertNull(tutorApplication.getCreatedAt()); // Should be null until onCreate is called
        assertNull(tutorApplication.getUpdatedAt()); // Should be null until onCreate is called
    }
    
    @Test
    void testDefaultConstructor() {
        TutorApplication defaultTutorApplication = new TutorApplication();
        assertNotNull(defaultTutorApplication.getId());
        assertNull(defaultTutorApplication.getStudentId());
        assertEquals(TutorApplication.Status.PENDING, defaultTutorApplication.getStatus());
    }
    
    @Test
    void testOnCreate() {
        tutorApplication.onCreate();
        assertNotNull(tutorApplication.getCreatedAt());
        assertNotNull(tutorApplication.getUpdatedAt());
        // Check if timestamps are very close instead of exactly equal
        assertTrue(java.time.Duration.between(tutorApplication.getCreatedAt(), tutorApplication.getUpdatedAt()).abs().toMillis() < 1000);
        
        // Test with null status
        TutorApplication nullStatusApp = new TutorApplication(studentId);
        nullStatusApp.setStatus(null);
        nullStatusApp.onCreate();
        assertEquals(TutorApplication.Status.PENDING, nullStatusApp.getStatus());
    }
    
    @Test
    void testOnUpdate() {
        tutorApplication.onCreate(); // Set initial timestamps
        LocalDateTime initialUpdateTime = tutorApplication.getUpdatedAt();
        
        // Wait a small amount of time to ensure timestamp difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        tutorApplication.onUpdate();
        assertTrue(tutorApplication.getUpdatedAt().isAfter(initialUpdateTime));
    }
    
    @Test
    void testSetters() {
        UUID newStudentId = UUID.randomUUID();
        tutorApplication.setStudentId(newStudentId);
        assertEquals(newStudentId, tutorApplication.getStudentId());
        
        tutorApplication.setStatus(TutorApplication.Status.ACCEPTED);
        assertEquals(TutorApplication.Status.ACCEPTED, tutorApplication.getStatus());
        
        tutorApplication.setStatus(TutorApplication.Status.DENIED);
        assertEquals(TutorApplication.Status.DENIED, tutorApplication.getStatus());
    }
    
    @Test
    void testStatusEnum() {
        // Test all possible enum values
        assertEquals(TutorApplication.Status.PENDING, TutorApplication.Status.valueOf("PENDING"));
        assertEquals(TutorApplication.Status.ACCEPTED, TutorApplication.Status.valueOf("ACCEPTED"));
        assertEquals(TutorApplication.Status.DENIED, TutorApplication.Status.valueOf("DENIED"));
    }
}