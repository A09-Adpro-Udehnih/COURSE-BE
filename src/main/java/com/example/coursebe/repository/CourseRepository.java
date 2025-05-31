package com.example.coursebe.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.coursebe.model.Course;

/**
 * Repository interface for Course entity
 * Provides CRUD operations and custom query methods for Course
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {

    /**
     * Find all courses created by a specific tutor
     * 
     * @param tutorId the ID of the tutor
     * @return list of courses created by the tutor
     */
    List<Course> findByTutorId(UUID tutorId);

    /**
     * Find courses whose names contain the given keyword (case-insensitive)
     * 
     * @param name search term to match against course names
     * @return list of courses with matching names
     */
    List<Course> findByNameContainingIgnoreCase(String name);

    /**
     * Find courses whose names or description contain the given keyword
     * (case-insensitive)
     * 
     * @param name        search term to match against course name
     * @param description search term to match against course description
     * @return list of courses with matching names or description
     */
    List<Course> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);


    /**
     * Find courses whose names contain the given keyword (case-insensitive) with pagination
     *
     * @param name     search term to match against course names
     * @param pageable pagination information including page number and size
     * @return paginated list of courses with matching names
     */
    Page<Course> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Find courses whose names or description contain the given keyword (case-insensitive) with pagination
     *
     * @param name        search term to match against course name
     * @param description search term to match against course description
     * @param pageable    pagination information including page number and size
     * @return paginated list of courses with matching names or descriptions
     */
    Page<Course> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description, Pageable pageable);

    /**
     * Find all courses that a user is enrolled in, with pagination support
     *
     * @param userId   The ID of the student/user whose enrollments to find
     * @param pageable Pagination information (page number, size, and sorting options)
     * @return A paginated list of courses the user is enrolled in
     */
    @Query("SELECT c FROM Course c JOIN c.enrollments e WHERE e.studentId = :userId")
    Page<Course> findByEnrollmentsStudentId(UUID userId, Pageable pageable);

    /**
     * Find courses that a user is enrolled in where the course name contains the provided keyword
     *
     * @param userId   The ID of the student/user whose enrollments to find
     * @param keyword  The search term to match against course names (case insensitive)
     * @param pageable Pagination information (page number, size, and sorting options)
     * @return A paginated list of matching enrolled courses
     */
    @Query("SELECT c FROM Course c JOIN c.enrollments e WHERE e.studentId = :userId AND " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Course> findByEnrollmentsStudentIdAndNameContainingIgnoreCase(UUID userId, String keyword, Pageable pageable);

    /**
     * Find courses that a user is enrolled in where the course name OR description contains the provided keyword
     *
     * @param userId       The ID of the student/user whose enrollments to find
     * @param keyword      The search term to match against course names (case insensitive)
     * @param keywordAgain The same search term to match against course descriptions (duplicate parameter required by Spring Data JPA)
     * @param pageable     Pagination information (page number, size, and sorting options)
     * @return A paginated list of matching enrolled courses
     */
    @Query("SELECT c FROM Course c JOIN c.enrollments e WHERE e.studentId = :userId AND " +
            "(LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :keywordAgain, '%')))")
    Page<Course> findByEnrollmentsStudentIdAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            UUID userId, String keyword, String keywordAgain, Pageable pageable);
}
