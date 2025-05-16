package com.example.coursebe.service;

import com.example.coursebe.model.Course;
import com.example.coursebe.model.Enrollment;
import com.example.coursebe.repository.CourseRepository;
import com.example.coursebe.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @Override
    @Transactional
    public Enrollment enroll(UUID studentId, UUID courseId) {
        // Validate inputs
        if (studentId == null || courseId == null) {
            throw new IllegalArgumentException("Student ID and Course ID cannot be null");
        }
        
        // Check if already enrolled
        if (isEnrolled(studentId, courseId)) {
            return null;
        }
        
        // Get the course
        Optional<Course> optionalCourse = courseRepository.findById(courseId);
        if (optionalCourse.isEmpty()) {
            return null;
        }
        
        // Create and save enrollment
        Enrollment enrollment = new Enrollment(studentId, optionalCourse.get());
        return enrollmentRepository.save(enrollment);
    }

    @Override
    @Transactional
    public boolean unenroll(UUID studentId, UUID courseId) {
        // Validate inputs
        if (studentId == null || courseId == null) {
            throw new IllegalArgumentException("Student ID and Course ID cannot be null");
        }
        
        // Find the enrollment
        Optional<Course> optionalCourse = courseRepository.findById(courseId);
        if (optionalCourse.isEmpty()) {
            return false;
        }
        
        Optional<Enrollment> optionalEnrollment = enrollmentRepository.findByStudentIdAndCourse(
            studentId, optionalCourse.get());
            
        if (optionalEnrollment.isEmpty()) {
            return false;
        }
        
        // Delete the enrollment
        enrollmentRepository.delete(optionalEnrollment.get());
        return true;
    }
}