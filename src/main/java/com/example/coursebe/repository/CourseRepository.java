package com.example.coursebe.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
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
     * @param tutorId the ID of the tutor
     * @return list of courses created by the tutor
     */
    List<Course> findByTutorId(UUID tutorId);
    
    /**
     * Find courses whose names contain the given keyword (case-insensitive)
     * @param keyword search term to match against course names
     * @return list of courses with matching names
     */
    List<Course> findByNameContainingIgnoreCase(String keyword);
}