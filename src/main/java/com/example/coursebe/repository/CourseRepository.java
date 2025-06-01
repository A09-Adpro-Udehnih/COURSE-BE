package com.example.coursebe.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.coursebe.model.Course;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {
    List<Course> findByTutorId(UUID tutorId);
    List<Course> findByNameContainingIgnoreCase(String name);
    List<Course> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);
    Page<Course> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Course> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description, Pageable pageable);

    @Query("SELECT c FROM Course c JOIN c.enrollments e WHERE e.studentId = :userId")
    Page<Course> findByEnrollmentsStudentId(UUID userId, Pageable pageable);

    @Query("SELECT c FROM Course c JOIN c.enrollments e WHERE e.studentId = :userId AND " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Course> findByEnrollmentsStudentIdAndNameContainingIgnoreCase(UUID userId, String keyword, Pageable pageable);

    @Query("SELECT c FROM Course c JOIN c.enrollments e WHERE e.studentId = :userId AND " +
            "(LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :keywordAgain, '%')))")
    Page<Course> findByEnrollmentsStudentIdAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            UUID userId, String keyword, String keywordAgain, Pageable pageable);
}
