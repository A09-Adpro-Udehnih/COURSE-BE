package com.example.coursebe.repository;

import com.example.coursebe.model.Course;
import com.example.coursebe.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Enrollment entity
 * Provides CRUD operations and custom query methods for Enrollment
 */
@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    
    /**
     * Find all enrollments for a specific student
     * @param studentId the ID of the student
     * @return list of enrollments for the student
     */
    List<Enrollment> findByStudentId(UUID studentId);
    
    /**
     * Find all enrollments for a specific course
     * @param course the course entity
     * @return list of enrollments for the course
     */
    List<Enrollment> findByCourse(Course course);
    
    /**
     * Find enrollment record for a specific student and course if it exists
     * @param studentId the ID of the student
     * @param course the course entity
     * @return optional enrollment record
     */
    Optional<Enrollment> findByStudentIdAndCourse(UUID studentId, Course course);
    
    /**
     * Check if a student is enrolled in a specific course
     * @param studentId the ID of the student
     * @param courseId the ID of the course
     * @return true if the student is enrolled, false otherwise
     */
    boolean existsByStudentIdAndCourseId(UUID studentId, UUID courseId);
}