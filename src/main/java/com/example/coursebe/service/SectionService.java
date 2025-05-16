package com.example.coursebe.service;

import com.example.coursebe.model.Section;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for managing Section entities
 */
public interface SectionService {
    
    /**
     * Get all sections for a course
     * @param courseId Course ID
     * @return List of sections for the course, ordered by position
     */
    List<Section> getSectionsByCourseId(UUID courseId);
    
    /**
     * Get section by ID
     * @param id Section ID
     * @return Optional containing section if found
     */
    Optional<Section> getSectionById(UUID id);
    
    /**
     * Create a new section for a course
     * @param courseId Course ID
     * @param title Section title
     * @param position Section position (optional, will determine automatically if null)
     * @return Created section or null if course not found
     */
    Section createSection(UUID courseId, String title, Integer position);
    
    /**
     * Update an existing section
     * @param id Section ID
     * @param title Updated title
     * @param position Updated position
     * @return Updated section or empty optional if not found
     */
    Optional<Section> updateSection(UUID id, String title, Integer position);
    
    /**
     * Delete a section
     * @param id Section ID
     * @return true if deleted, false if not found
     */
    boolean deleteSection(UUID id);
    
    /**
     * Reorder sections within a course
     * @param courseId Course ID
     * @param sectionIds Ordered list of section IDs
     * @return List of updated sections or empty list if course not found
     */
    List<Section> reorderSections(UUID courseId, List<UUID> sectionIds);
}