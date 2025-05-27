package com.example.coursebe.repository;

import com.example.coursebe.model.Course;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.example.coursebe.model.Enrollment;

import java.time.LocalDateTime;
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
    @DisplayName("Should find all courses with pagination")
    void findAllCoursesWithPagination() {
        // Test first page with 2 items per page
        Pageable firstPageable = PageRequest.of(0, 2);
        Page<Course> firstPage = courseRepository.findAll(firstPageable);

        assertNotNull(firstPage);
        assertEquals(2, firstPage.getContent().size());
        assertEquals(3, firstPage.getTotalElements());
        assertEquals(2, firstPage.getTotalPages());
        assertEquals(0, firstPage.getNumber());

        // Test second page with 2 items per page
        Pageable secondPageable = PageRequest.of(1, 2);
        Page<Course> secondPage = courseRepository.findAll(secondPageable);

        assertNotNull(secondPage);
        assertEquals(1, secondPage.getContent().size());
        assertEquals(3, secondPage.getTotalElements());
        assertEquals(2, secondPage.getTotalPages());
        assertEquals(1, secondPage.getNumber());

        // Test with sorting
        Pageable sortedPageable = PageRequest.of(0, 3, Sort.by("price").ascending());
        Page<Course> sortedPage = courseRepository.findAll(sortedPageable);

        assertNotNull(sortedPage);
        assertEquals(3, sortedPage.getContent().size());
        assertEquals(course3.getId(), sortedPage.getContent().get(0).getId()); // Python course has lowest price

        // Test empty page (beyond available data)
        Pageable beyondPageable = PageRequest.of(5, 2);
        Page<Course> beyondPage = courseRepository.findAll(beyondPageable);

        assertNotNull(beyondPage);
        assertEquals(0, beyondPage.getContent().size());
        assertEquals(3, beyondPage.getTotalElements());
        assertTrue(beyondPage.isEmpty());
    }

    @Test
    @DisplayName("Should find courses by name or description with pagination")
    void findCoursesByNameOrDescriptionWithPagination() {
        // Test basic functionality
        Pageable pageable = PageRequest.of(0, 2);
        Page<Course> basicCoursesPage = courseRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                "basics", "basics", pageable);

        assertNotNull(basicCoursesPage);
        assertEquals(2, basicCoursesPage.getContent().size());
        assertEquals(2, basicCoursesPage.getTotalElements());
        assertEquals(1, basicCoursesPage.getTotalPages());

        // Verify actual content is correct
        List<UUID> basicCourseIds = basicCoursesPage.getContent().stream()
                .map(Course::getId)
                .toList();
        assertTrue(basicCourseIds.contains(course1.getId())); // Java basics
        assertTrue(basicCourseIds.contains(course3.getId())); // Python basics
    }

    @Test
    @DisplayName("Should find courses by name with pagination")
    void findCoursesByNameWithPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> javaCoursesPage = courseRepository.findByNameContainingIgnoreCase("java", pageable);

        assertNotNull(javaCoursesPage);
        assertEquals(2, javaCoursesPage.getContent().size());
        assertEquals(2, javaCoursesPage.getTotalElements());
        assertEquals(1, javaCoursesPage.getTotalPages());

        // Verify case insensitivity
        Page<Course> upperJavaCoursesPage = courseRepository.findByNameContainingIgnoreCase("JAVA", pageable);
        assertEquals(2, upperJavaCoursesPage.getTotalElements());

        // Test pagination with limited page size
        Pageable smallPage = PageRequest.of(0, 1);
        Page<Course> firstJavaCoursePage = courseRepository.findByNameContainingIgnoreCase("java", smallPage);
        assertEquals(1, firstJavaCoursePage.getContent().size());
        assertEquals(2, firstJavaCoursePage.getTotalElements());
        assertEquals(2, firstJavaCoursePage.getTotalPages());

        // Second page
        Pageable secondSmallPage = PageRequest.of(1, 1);
        Page<Course> secondJavaCoursePage = courseRepository.findByNameContainingIgnoreCase("java", secondSmallPage);
        assertEquals(1, secondJavaCoursePage.getContent().size());
        assertEquals(2, secondJavaCoursePage.getTotalElements());
        assertEquals(1, secondJavaCoursePage.getNumber());
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

    @Test
    @DisplayName("Should find courses by student enrollment with pagination")
    void findCoursesByStudentEnrollment() {
        // Create a student and enrollments
        UUID studentId = UUID.randomUUID();

        // Create enrollment relationships
        // We need to create Enrollment entities and associate them with courses
        Enrollment enrollment1 = new Enrollment();
        enrollment1.setStudentId(studentId);
        enrollment1.setCourse(course1);
        enrollment1.setEnrollmentDate(LocalDateTime.now());

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setStudentId(studentId);
        enrollment2.setCourse(course3);
        enrollment2.setEnrollmentDate(LocalDateTime.now());

        // Save enrollments
        entityManager.persist(enrollment1);
        entityManager.persist(enrollment2);
        entityManager.flush();

        // Find enrolled courses with pagination
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> enrolledCoursesPage = courseRepository.findByEnrollmentsStudentId(studentId, pageable);

        // Assertions
        assertNotNull(enrolledCoursesPage);
        assertEquals(2, enrolledCoursesPage.getTotalElements());
        assertEquals(1, enrolledCoursesPage.getTotalPages());

        // Verify the student is enrolled in the correct courses
        List<UUID> enrolledCourseIds = enrolledCoursesPage.getContent().stream()
                .map(Course::getId)
                .toList();
        assertTrue(enrolledCourseIds.contains(course1.getId())); // Java Programming
        assertTrue(enrolledCourseIds.contains(course3.getId())); // Python Programming
        assertFalse(enrolledCourseIds.contains(course2.getId())); // Not enrolled in Advanced Java

        // Test pagination
        Pageable singleCoursePage = PageRequest.of(0, 1);
        Page<Course> firstEnrolledCoursePage = courseRepository.findByEnrollmentsStudentId(studentId, singleCoursePage);
        assertEquals(1, firstEnrolledCoursePage.getContent().size());
        assertEquals(2, firstEnrolledCoursePage.getTotalElements());
        assertEquals(2, firstEnrolledCoursePage.getTotalPages());
    }

    @Test
    @DisplayName("Should find enrolled courses by name with pagination")
    void findEnrolledCoursesByName() {
        // Create a student and enrollments
        UUID studentId = UUID.randomUUID();

        // Create enrollment relationships
        Enrollment enrollment1 = new Enrollment();
        enrollment1.setStudentId(studentId);
        enrollment1.setCourse(course1); // Java Programming
        enrollment1.setEnrollmentDate(LocalDateTime.now());

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setStudentId(studentId);
        enrollment2.setCourse(course2); // Advanced Java
        enrollment2.setEnrollmentDate(LocalDateTime.now());

        // Save enrollments
        entityManager.persist(enrollment1);
        entityManager.persist(enrollment2);
        entityManager.flush();

        // Find enrolled Java courses
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> javaCoursesPage = courseRepository.findByEnrollmentsStudentIdAndNameContainingIgnoreCase(
                studentId, "java", pageable);

        // Assertions
        assertNotNull(javaCoursesPage);
        assertEquals(2, javaCoursesPage.getTotalElements());

        // Verify case insensitivity
        Page<Course> upperJavaCoursesPage = courseRepository.findByEnrollmentsStudentIdAndNameContainingIgnoreCase(
                studentId, "JAVA", pageable);
        assertEquals(2, upperJavaCoursesPage.getTotalElements());

        // Test specific course search
        Page<Course> advancedCoursesPage = courseRepository.findByEnrollmentsStudentIdAndNameContainingIgnoreCase(
                studentId, "Advanced", pageable);
        assertEquals(1, advancedCoursesPage.getTotalElements());
        assertEquals(course2.getId(), advancedCoursesPage.getContent().get(0).getId());

        // Test non-enrolled course is not returned
        UUID otherStudentId = UUID.randomUUID();
        Page<Course> otherStudentCourses = courseRepository.findByEnrollmentsStudentIdAndNameContainingIgnoreCase(
                otherStudentId, "Java", pageable);
        assertEquals(0, otherStudentCourses.getTotalElements());
    }

    @Test
    @DisplayName("Should find enrolled courses by name or description with pagination")
    void findEnrolledCoursesByNameOrDescription() {
        // Create a student and enrollments
        UUID studentId = UUID.randomUUID();

        // Create enrollment relationships
        Enrollment enrollment1 = new Enrollment();
        enrollment1.setStudentId(studentId);
        enrollment1.setCourse(course1); // Java Programming (has "basics" in description)
        enrollment1.setEnrollmentDate(LocalDateTime.now());

        Enrollment enrollment3 = new Enrollment();
        enrollment3.setStudentId(studentId);
        enrollment3.setCourse(course3); // Python Programming (has "basics" in description)
        enrollment3.setEnrollmentDate(LocalDateTime.now());

        // Save enrollments
        entityManager.persist(enrollment1);
        entityManager.persist(enrollment3);
        entityManager.flush();

        // Find enrolled courses with "basics" in description
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> basicsCoursesPage = courseRepository.findByEnrollmentsStudentIdAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                studentId, "basics", "basics", pageable);

        // Assertions
        assertNotNull(basicsCoursesPage);
        assertEquals(2, basicsCoursesPage.getTotalElements());

        // Verify content
        List<UUID> basicsEnrolledCourseIds = basicsCoursesPage.getContent().stream()
                .map(Course::getId)
                .toList();
        assertTrue(basicsEnrolledCourseIds.contains(course1.getId())); // Java basics
        assertTrue(basicsEnrolledCourseIds.contains(course3.getId())); // Python basics

        // Find courses by name (should find only one enrolled course)
        Page<Course> pythonCoursesPage = courseRepository.findByEnrollmentsStudentIdAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                studentId, "Python", "Python", pageable);

        assertEquals(1, pythonCoursesPage.getTotalElements());
        assertEquals(course3.getId(), pythonCoursesPage.getContent().get(0).getId());

        // Start a new transaction for the other student test
        entityManager.clear();

        // Check behavior with a student not enrolled in any courses
        UUID otherStudentId = UUID.randomUUID();

        Page<Course> otherStudentCourses = courseRepository.findByEnrollmentsStudentIdAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                otherStudentId, "basics", "basics", pageable);

        // Should return empty results for a student with no enrollments
        assertEquals(0, otherStudentCourses.getTotalElements());
    }
}