package com.example.coursebe.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class EnrollmentTest {
    
    private Enrollment enrollment;
    private UUID studentId;
    private Course course;
    
    @BeforeEach
    void setUp() {
        studentId = UUID.randomUUID();
        course = new Course("Test Course", "Test Description", UUID.randomUUID(), new BigDecimal("99.99"));
        enrollment = new Enrollment(studentId, course);
    }
    
    @Test
    void testConstructor() {
        assertNotNull(enrollment.getId());
        assertEquals(studentId, enrollment.getStudentId());
        assertEquals(course, enrollment.getCourse());
        assertNull(enrollment.getEnrollmentDate()); // Should be null until onCreate is called
    }
    
    @Test
    void testDefaultConstructor() {
        Enrollment defaultEnrollment = new Enrollment();
        assertNotNull(defaultEnrollment.getId());
        assertNull(defaultEnrollment.getStudentId());
        assertNull(defaultEnrollment.getCourse());
    }
    
    @Test
    void testOnCreate() {
        enrollment.onCreate();
        assertNotNull(enrollment.getEnrollmentDate());
        
        LocalDateTime now = LocalDateTime.now();
        // Enrollment date should be around current time (within a few seconds)
        assertTrue(enrollment.getEnrollmentDate().isBefore(now.plusSeconds(1)));
        assertTrue(enrollment.getEnrollmentDate().isAfter(now.minusSeconds(1)));
    }
    
    @Test
    void testSetters() {
        UUID newStudentId = UUID.randomUUID();
        enrollment.setStudentId(newStudentId);
        assertEquals(newStudentId, enrollment.getStudentId());
        
        Course newCourse = new Course("New Course", "New Description", UUID.randomUUID(), new BigDecimal("149.99"));
        enrollment.setCourse(newCourse);
        assertEquals(newCourse, enrollment.getCourse());
    }
}