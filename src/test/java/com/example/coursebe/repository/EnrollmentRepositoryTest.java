package com.example.coursebe.repository;

import com.example.coursebe.model.Course;
import com.example.coursebe.model.Enrollment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for EnrollmentRepository
 */
@DataJpaTest
public class EnrollmentRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    private UUID studentId1;
    private UUID studentId2;
    private Course course1;
    private Course course2;
    private Enrollment enrollment1;
    private Enrollment enrollment2;
    private Enrollment enrollment3;
    
    @BeforeEach
    void setUp() {
        UUID tutorId = UUID.randomUUID();
        studentId1 = UUID.randomUUID();
        studentId2 = UUID.randomUUID();
        
        // Create test courses
        course1 = new Course("Java Programming", "Learn Java basics", tutorId, new BigDecimal("99.99"));
        course2 = new Course("Python Programming", "Learn Python basics", tutorId, new BigDecimal("89.99"));
        
        entityManager.persist(course1);
        entityManager.persist(course2);
        
        // Create test enrollments
        enrollment1 = new Enrollment(studentId1, course1);
        enrollment2 = new Enrollment(studentId1, course2);
        enrollment3 = new Enrollment(studentId2, course1);
        
        entityManager.persist(enrollment1);
        entityManager.persist(enrollment2);
        entityManager.persist(enrollment3);
        entityManager.flush();
    }
    
    @Test
    @DisplayName("Should find all enrollments")
    void findAllEnrollments() {
        // when
        List<Enrollment> enrollments = enrollmentRepository.findAll();
        
        // then
        assertNotNull(enrollments);
        assertEquals(3, enrollments.size());
    }
    
    @Test
    @DisplayName("Should find enrollment by ID")
    void findEnrollmentById() {
        // when
        Optional<Enrollment> found = enrollmentRepository.findById(enrollment1.getId());
        
        // then
        assertTrue(found.isPresent());
        assertEquals(studentId1, found.get().getStudentId());
        assertEquals(course1.getId(), found.get().getCourse().getId());
        assertNotNull(found.get().getEnrollmentDate());
    }
    
    @Test
    @DisplayName("Should find enrollments by student ID")
    void findEnrollmentsByStudentId() {
        // when
        List<Enrollment> studentEnrollments = enrollmentRepository.findByStudentId(studentId1);
        
        // then
        assertNotNull(studentEnrollments);
        assertEquals(2, studentEnrollments.size());
        assertTrue(studentEnrollments.stream().allMatch(enrollment -> enrollment.getStudentId().equals(studentId1)));
    }
    
    @Test
    @DisplayName("Should find enrollments by course")
    void findEnrollmentsByCourse() {
        // when
        List<Enrollment> courseEnrollments = enrollmentRepository.findByCourse(course1);
        
        // then
        assertNotNull(courseEnrollments);
        assertEquals(2, courseEnrollments.size());
        assertTrue(courseEnrollments.stream().allMatch(enrollment -> enrollment.getCourse().getId().equals(course1.getId())));
    }
    
    @Test
    @DisplayName("Should find enrollment by student ID and course")
    void findEnrollmentByStudentIdAndCourse() {
        // when
        Optional<Enrollment> found = enrollmentRepository.findByStudentIdAndCourse(studentId1, course1);
        
        // then
        assertTrue(found.isPresent());
        assertEquals(studentId1, found.get().getStudentId());
        assertEquals(course1.getId(), found.get().getCourse().getId());
    }
    
    @Test
    @DisplayName("Should check if student is enrolled in course")
    void existsByStudentIdAndCourseId() {
        // when - student is enrolled
        boolean isEnrolled = enrollmentRepository.existsByStudentIdAndCourseId(studentId1, course1.getId());
        
        // then
        assertTrue(isEnrolled);
        
        // when - student is not enrolled
        UUID studentId3 = UUID.randomUUID();
        boolean isNotEnrolled = enrollmentRepository.existsByStudentIdAndCourseId(studentId3, course1.getId());
        
        // then
        assertFalse(isNotEnrolled);
    }
    
    @Test
    @DisplayName("Should save enrollment")
    void saveEnrollment() {
        // given
        UUID studentId3 = UUID.randomUUID();
        Enrollment newEnrollment = new Enrollment(studentId3, course2);
        
        // when
        Enrollment saved = enrollmentRepository.save(newEnrollment);
        
        // then
        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals(studentId3, saved.getStudentId());
        assertEquals(course2.getId(), saved.getCourse().getId());
        
        // when
        Optional<Enrollment> found = enrollmentRepository.findById(saved.getId());
        
        // then
        assertTrue(found.isPresent());
    }
    
    @Test
    @DisplayName("Should delete enrollment")
    void deleteEnrollment() {
        // given
        Enrollment enrollmentToDelete = enrollment3;
        
        // when
        enrollmentRepository.delete(enrollmentToDelete);
        Optional<Enrollment> found = enrollmentRepository.findById(enrollmentToDelete.getId());
        
        // then
        assertFalse(found.isPresent());
    }
}