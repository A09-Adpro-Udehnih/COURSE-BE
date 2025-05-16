package com.example.coursebe.repository;

import com.example.coursebe.model.Course;
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
 * Test class for CourseRepository
 * Uses Spring Boot's @DataJpaTest which provides an embedded database and
 * configures Spring Data JPA repositories
 */
@DataJpaTest
public class CourseRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private CourseRepository courseRepository;
    
    private UUID tutorId1;
    private UUID tutorId2;
    private Course course1;
    private Course course2;
    private Course course3;
    
    @BeforeEach
    void setUp() {
        tutorId1 = UUID.randomUUID();
        tutorId2 = UUID.randomUUID();
        
        // Create test courses
        course1 = new Course("Java Programming", "Learn Java basics", tutorId1, new BigDecimal("99.99"));
        course2 = new Course("Advanced Java", "Learn advanced Java concepts", tutorId1, new BigDecimal("149.99"));
        course3 = new Course("Python Programming", "Learn Python basics", tutorId2, new BigDecimal("89.99"));
        
        // Save courses to the database using TestEntityManager
        entityManager.persist(course1);
        entityManager.persist(course2);
        entityManager.persist(course3);
        entityManager.flush();
    }
    
    @Test
    @DisplayName("Should find all courses")
    void findAllCourses() {
        // when
        List<Course> courses = courseRepository.findAll();
        
        // then
        assertNotNull(courses);
        assertEquals(3, courses.size());
    }
    
    @Test
    @DisplayName("Should find course by ID")
    void findCourseById() {
        // when
        Optional<Course> found = courseRepository.findById(course1.getId());
        
        // then
        assertTrue(found.isPresent());
        assertEquals(course1.getName(), found.get().getName());
        assertEquals(course1.getDescription(), found.get().getDescription());
        assertEquals(course1.getTutorId(), found.get().getTutorId());
        assertEquals(course1.getPrice(), found.get().getPrice());
    }
    
    @Test
    @DisplayName("Should find courses by tutor ID")
    void findCoursesByTutorId() {
        // when
        List<Course> tutorCourses = courseRepository.findByTutorId(tutorId1);
        
        // then
        assertNotNull(tutorCourses);
        assertEquals(2, tutorCourses.size());
        assertTrue(tutorCourses.stream().allMatch(course -> course.getTutorId().equals(tutorId1)));
    }
    
    @Test
    @DisplayName("Should find courses containing name keyword (case insensitive)")
    void findCoursesByNameContaining() {
        // when - searching with lowercase
        List<Course> javaCoursesLower = courseRepository.findByNameContainingIgnoreCase("java");
        
        // then
        assertEquals(2, javaCoursesLower.size());
        
        // when - searching with mixed case
        List<Course> javaCoursesUpper = courseRepository.findByNameContainingIgnoreCase("JAVA");
        
        // then
        assertEquals(2, javaCoursesUpper.size());
        
        // when - searching with mixed case specific term
        List<Course> advancedCourses = courseRepository.findByNameContainingIgnoreCase("Advanced");
        
        // then
        assertEquals(1, advancedCourses.size());
        assertEquals("Advanced Java", advancedCourses.get(0).getName());
    }
    
    @Test
    @DisplayName("Should save course")
    void saveCourse() {
        // given
        UUID tutorId3 = UUID.randomUUID();
        Course newCourse = new Course("JavaScript", "Learn JavaScript", tutorId3, new BigDecimal("79.99"));
        
        // when
        Course saved = courseRepository.save(newCourse);
        
        // then
        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("JavaScript", saved.getName());
        assertEquals("Learn JavaScript", saved.getDescription());
        assertEquals(tutorId3, saved.getTutorId());
        assertEquals(new BigDecimal("79.99"), saved.getPrice());
        
        // when
        Optional<Course> found = courseRepository.findById(saved.getId());
        
        // then
        assertTrue(found.isPresent());
    }
    
    @Test
    @DisplayName("Should delete course")
    void deleteCourse() {
        // given
        Course courseToDelete = course3;
        
        // when
        courseRepository.delete(courseToDelete);
        Optional<Course> found = courseRepository.findById(courseToDelete.getId());
        
        // then
        assertFalse(found.isPresent());
    }
}