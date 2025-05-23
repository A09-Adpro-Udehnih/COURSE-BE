package com.example.coursebe.service;

import com.example.coursebe.model.Course;
import com.example.coursebe.model.Enrollment;
import com.example.coursebe.repository.CourseRepository;
import com.example.coursebe.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Async;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of EnrollmentService
 */
@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    @Autowired
    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository, CourseRepository courseRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public List<Enrollment> getEnrollmentsByStudentId(UUID studentId) {
        // Validate input
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        
        return enrollmentRepository.findByStudentId(studentId);
    }

    @Override
    public List<Enrollment> getEnrollmentsByCourseId(UUID courseId) {
        // Validate input
        if (courseId == null) {
            throw new IllegalArgumentException("Course ID cannot be null");
        }
        
        Optional<Course> optionalCourse = courseRepository.findById(courseId);
        if (optionalCourse.isEmpty()) {
            return new ArrayList<>();
        }
        
        return enrollmentRepository.findByCourse(optionalCourse.get());
    }

    @Override
    public Optional<Enrollment> getEnrollment(UUID studentId, UUID courseId) {
        // Validate inputs
        if (studentId == null || courseId == null) {
            throw new IllegalArgumentException("Student ID and Course ID cannot be null");
        }
        
        Optional<Course> optionalCourse = courseRepository.findById(courseId);
        if (optionalCourse.isEmpty()) {
            return Optional.empty();
        }
        
        return enrollmentRepository.findByStudentIdAndCourse(studentId, optionalCourse.get());
    }

    @Override
    public boolean isEnrolled(UUID studentId, UUID courseId) {
        // Validate inputs
        if (studentId == null || courseId == null) {
            throw new IllegalArgumentException("Student ID and Course ID cannot be null");
        }
        
        return enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId);
    }

    @Async
    @Override
    @Transactional
    public CompletableFuture<Enrollment> enroll(UUID studentId, UUID courseId) {
        try {
            // Validate inputs
            if (studentId == null || courseId == null) {
                throw new IllegalArgumentException("Student ID and Course ID cannot be null");
            }

            // Check if already enrolled
            if (isEnrolled(studentId, courseId)) {
                return CompletableFuture.completedFuture(null);
            }

            // Get the course
            Optional<Course> optionalCourse = courseRepository.findById(courseId);
            if (optionalCourse.isEmpty()) {
                return CompletableFuture.completedFuture(null);
            }

            // Create and save enrollment
            Enrollment enrollment = new Enrollment(studentId, optionalCourse.get());
            Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
            return CompletableFuture.completedFuture(savedEnrollment);
        } catch (Exception ex) {
            // Return a completed future with the exception
            CompletableFuture<Enrollment> futureResult = new CompletableFuture<>();
            futureResult.completeExceptionally(ex);
            return futureResult;
        }
    }

    @Async
    @Override
    @Transactional
    public CompletableFuture<Boolean> unenroll(UUID studentId, UUID courseId) {
        try {
            // Validate inputs
            if (studentId == null || courseId == null) {
                throw new IllegalArgumentException("Student ID and Course ID cannot be null");
            }

            // Find the enrollment
            Optional<Course> optionalCourse = courseRepository.findById(courseId);
            if (optionalCourse.isEmpty()) {
                return CompletableFuture.completedFuture(false);
            }

            Optional<Enrollment> optionalEnrollment = enrollmentRepository.findByStudentIdAndCourse(
                    studentId, optionalCourse.get());

            if (optionalEnrollment.isEmpty()) {
                return CompletableFuture.completedFuture(false);
            }

            // Delete the enrollment
            enrollmentRepository.delete(optionalEnrollment.get());
            return CompletableFuture.completedFuture(true);
        } catch (Exception ex) {
            CompletableFuture<Boolean> futureResult = new CompletableFuture<>();
            futureResult.completeExceptionally(ex);
            return futureResult;
        }
    }
}