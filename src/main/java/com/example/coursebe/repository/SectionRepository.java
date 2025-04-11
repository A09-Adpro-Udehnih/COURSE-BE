package com.example.coursebe.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.coursebe.model.Course;
import com.example.coursebe.model.Section;

/**
 * Repository interface for Section entity
 * Provides CRUD operations and custom query methods for Section
 */
@Repository
public interface SectionRepository extends JpaRepository<Section, UUID> {
    
    /**
     * Find all sections belonging to a specific course
     * @param course the course entity
     * @return list of sections for the course
     */
    List<Section> findByCourse(Course course);
    
    /**
     * Find all sections belonging to a specific course, ordered by position
     * @param course the course entity
     * @return ordered list of sections for the course
     */
    List<Section> findByCourseOrderByPositionAsc(Course course);
    
    /**
     * Find all sections belonging to a course with the given ID
     * @param courseId the course ID
     * @return list of sections for the course
     */
    List<Section> findByCourseId(UUID courseId);
}