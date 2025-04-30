package com.example.coursebe.service;

import com.example.coursebe.model.Course;
import com.example.coursebe.model.Section;
import com.example.coursebe.repository.CourseRepository;
import com.example.coursebe.repository.SectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of SectionService
 */
@Service
public class SectionServiceImpl implements SectionService {

    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;

    @Autowired
    public SectionServiceImpl(SectionRepository sectionRepository, CourseRepository courseRepository) {
        this.sectionRepository = sectionRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public List<Section> getSectionsByCourseId(UUID courseId) {
        return sectionRepository.findByCourseId(courseId).stream()
                .sorted((s1, s2) -> s1.getPosition().compareTo(s2.getPosition()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Section> getSectionById(UUID id) {
        return sectionRepository.findById(id);
    }

    @Override
    @Transactional
    public Section createSection(UUID courseId, String title, Integer position) {
        // Validate inputs
        if (courseId == null) {
            throw new IllegalArgumentException("Course ID cannot be null");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Section title cannot be empty");
        }

        // Find course
        Optional<Course> optionalCourse = courseRepository.findById(courseId);
        if (optionalCourse.isEmpty()) {
            return null;
        }
        Course course = optionalCourse.get();

        // If position is null, calculate next position
        if (position == null) {
            List<Section> existingSections = sectionRepository.findByCourse(course);
            position = existingSections.size();
        }

        // Create and link section
        Section section = new Section(title, position);
        section.setCourse(course);

        // Save and return
        return sectionRepository.save(section);
    }

    @Override
    @Transactional
    public Optional<Section> updateSection(UUID id, String title, Integer position) {
        // Validate inputs
        if (id == null) {
            throw new IllegalArgumentException("Section ID cannot be null");
        }

        // Find section
        Optional<Section> optionalSection = sectionRepository.findById(id);
        if (optionalSection.isEmpty()) {
            return Optional.empty();
        }
        
        Section section = optionalSection.get();
        
        // Update section
        if (title != null && !title.trim().isEmpty()) {
            section.setTitle(title);
        }
        
        if (position != null) {
            section.setPosition(position);
        }
        
        // Save and return
        Section updatedSection = sectionRepository.save(section);
        return Optional.of(updatedSection);
    }

    @Override
    @Transactional
    public boolean deleteSection(UUID id) {
        // Validate inputs
        if (id == null) {
            throw new IllegalArgumentException("Section ID cannot be null");
        }
        
        // Check if section exists
        if (sectionRepository.existsById(id)) {
            sectionRepository.deleteById(id);
            return true;
        }
        
        return false;
    }

    @Override
    @Transactional
    public List<Section> reorderSections(UUID courseId, List<UUID> sectionIds) {
        // Validate inputs
        if (courseId == null) {
            throw new IllegalArgumentException("Course ID cannot be null");
        }
        if (sectionIds == null || sectionIds.isEmpty()) {
            throw new IllegalArgumentException("Section IDs list cannot be null or empty");
        }

        // Check course exists
        Optional<Course> optionalCourse = courseRepository.findById(courseId);
        if (optionalCourse.isEmpty()) {
            return new ArrayList<>();
        }
        Course course = optionalCourse.get();

        // Get all sections of the course
        List<Section> sections = sectionRepository.findByCourse(course);

        // Make sure all specified sections belong to the course
        for (UUID id : sectionIds) {
            boolean found = sections.stream().anyMatch(section -> section.getId().equals(id));
            if (!found) {
                throw new IllegalArgumentException("All sections must belong to the specified course");
            }
        }

        // Update positions
        List<Section> updatedSections = new ArrayList<>();
        for (int i = 0; i < sectionIds.size(); i++) {
            UUID sectionId = sectionIds.get(i);
            Optional<Section> optionalSection = sections.stream()
                    .filter(s -> s.getId().equals(sectionId))
                    .findFirst();
            if (optionalSection.isPresent()) {
                Section section = optionalSection.get();
                section.setPosition(i);
                updatedSections.add(sectionRepository.save(section));
            }
        }
        return updatedSections;
    }
}