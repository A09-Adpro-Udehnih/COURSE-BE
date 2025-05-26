package com.example.coursebe.service;

import com.example.coursebe.exception.UnsupportedSearchTypeException;
import com.example.coursebe.model.Course;
import com.example.coursebe.pattern.strategy.CourseSearchContext;
import com.example.coursebe.pattern.strategy.CourseSearchStrategy;
import com.example.coursebe.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of CourseService
 * Uses the Repository pattern to abstract data access
 */
@Service
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;
    private final CourseSearchContext courseSearchContext;

    @Autowired
    public CourseServiceImpl(CourseRepository courseRepository, CourseSearchContext courseSearchContext) {
        this.courseRepository = courseRepository;
        this.courseSearchContext = courseSearchContext;
    }

    @Override
    public Page<Course> getAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable);
    }

    @Override
    public Optional<Course> getCourseById(UUID id) {
        return courseRepository.findById(id);
    }

    @Override
    public List<Course> getCoursesByTutorId(UUID tutorId) {
        return courseRepository.findByTutorId(tutorId);
    }

    @Override
    public Page<Course> searchCourses(String type, String keyword, Pageable pageable) {
        if (!courseSearchContext.isValidStrategy(type)) {
            throw new UnsupportedSearchTypeException(type);
        }
        CourseSearchStrategy strategy = courseSearchContext.getStrategy(type);
        return strategy.search(keyword, pageable);
    }

    @Override
    @Transactional
    public Course createCourse(String name, String description, UUID tutorId, BigDecimal price) {
        // Validate inputs
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Course name cannot be empty");
        }
        if (tutorId == null) {
            throw new IllegalArgumentException("Tutor ID cannot be null");
        }

        // Create course using the Template Method pattern
        Course course = new Course(name, description, tutorId, price);
        
        // Use repository to save the course
        return courseRepository.save(course);
    }

    @Override
    @Transactional
    public Optional<Course> updateCourse(UUID id, String name, String description, BigDecimal price) {
        // Validate inputs
        if (id == null) {
            throw new IllegalArgumentException("Course ID cannot be null");
        }

        // Find the course
        Optional<Course> optionalCourse = courseRepository.findById(id);
        
        // If course exists, update it
        if (optionalCourse.isPresent()) {
            Course course = optionalCourse.get();
            
            if (name != null && !name.trim().isEmpty()) {
                course.setName(name);
            }
            
            if (description != null) {
                course.setDescription(description);
            }
            
            if (price != null) {
                course.setPrice(price);
            }
            
            Course updatedCourse = courseRepository.save(course);
            return Optional.of(updatedCourse);
        }
        
        return Optional.empty();
    }

    @Override
    @Transactional
    public boolean deleteCourse(UUID id) {
        // Validate inputs
        if (id == null) {
            throw new IllegalArgumentException("Course ID cannot be null");
        }
        
        // Check if course exists
        if (courseRepository.existsById(id)) {
            courseRepository.deleteById(id);
            return true;
        }
        
        return false;
    }

    @Override
    public List<String> getEnrolledStudents(UUID courseId) {
        // Ambil course
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isEmpty()) return List.of();
        // Ambil enrollments dari repository
        // EnrollmentRepository harus diinject
        // Untuk sekarang, pseudo-code: return enrollmentRepository.findByCourse(courseOpt.get()).stream().map(e -> e.getStudentId().toString()).toList();
        throw new UnsupportedOperationException("Implementasi getEnrolledStudents harus menginject EnrollmentRepository dan mengembalikan daftar studentId/email.");
    }
}