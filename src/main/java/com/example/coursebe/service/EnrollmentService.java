package com.example.coursebe.service;

import com.example.coursebe.model.Course;
import com.example.coursebe.model.Enrollment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for managing Enrollment entities
 */
public interface EnrollmentService {
    
    /**
     * Get all enrollments for a student
     * @param studentId Student ID
     * @return List of enrollments for the student
     */
    List<Enrollment> getEnrollmentsByStudentId(UUID studentId);
    
    /**
     * Get all enrollments for a course
     * @param courseId Course ID
     * @return List of enrollments for the course
     */
    List<Enrollment> getEnrollmentsByCourseId(UUID courseId);
    
    /**
     * Get a student's enrollment in a specific course
     * @param studentId Student ID
     * @param courseId Course ID
     * @return Optional containing the enrollment if it exists
     */
    Optional<Enrollment> getEnrollment(UUID studentId, UUID courseId);
    
    /**
     * Check if a student is enrolled in a course
     * @param studentId Student ID
     * @param courseId Course ID
     * @return true if enrolled, false otherwise
     */
    boolean isEnrolled(UUID studentId, UUID courseId);
    
    /**
     * Enroll a student in a course
     * @param studentId Student ID
     * @param courseId Course ID
     * @return Created enrollment or null if course not found or student is already enrolled
     */
    Enrollment enroll(UUID studentId, UUID courseId);
    
    /**
     * Unenroll a student from a course
     * @param studentId Student ID
     * @param courseId Course ID
     * @return true if unenrolled successfully, false otherwise
     */
    boolean unenroll(UUID studentId, UUID courseId);
}