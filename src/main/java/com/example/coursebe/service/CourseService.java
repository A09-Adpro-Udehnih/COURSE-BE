package com.example.coursebe.service;

import com.example.coursebe.model.Course;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for managing Course entities
 */
public interface CourseService {
    
    /**
     * Get all courses
     * @return List of all courses
     */
    List<Course> getAllCourses();
    
    /**
     * Get course by ID
     * @param id Course ID
     * @return Optional containing course if found
     */
    Optional<Course> getCourseById(UUID id);
    
    /**
     * Get courses for a specific tutor
     * @param tutorId Tutor ID
     * @return List of courses created by the tutor
     */
    List<Course> getCoursesByTutorId(UUID tutorId);
    
    /**
     * Search courses by name (case insensitive)
     * @param keyword Keyword to search for in course names
     * @return List of matching courses
     */
    List<Course> searchCoursesByName(String keyword);
    
    /**
     * Create a new course
     * @param name Course name
     * @param description Course description
     * @param tutorId ID of the tutor creating the course
     * @param price Course price
     * @return Created course
     */
    Course createCourse(String name, String description, UUID tutorId, BigDecimal price);
    
    /**
     * Update an existing course
     * @param id Course ID
     * @param name Updated name
     * @param description Updated description
     * @param price Updated price
     * @return Updated course or empty optional if course not found
     */
    Optional<Course> updateCourse(UUID id, String name, String description, BigDecimal price);
    
    /**
     * Delete a course
     * @param id Course ID
     * @return true if deleted, false if not found
     */
    boolean deleteCourse(UUID id);
    
    /**
     * Get list of students enrolled in a course
     * @param courseId Course ID
     * @return List of students (misal: email, id, atau objek Student)
     */
    List<String> getEnrolledStudents(UUID courseId);
}