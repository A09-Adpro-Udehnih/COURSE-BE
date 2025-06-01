package com.example.coursebe.service;

import com.example.coursebe.model.Section;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SectionService {
    List<Section> getSectionsByCourseId(UUID courseId);
    Optional<Section> getSectionById(UUID id);
    Section createSection(UUID courseId, String title, Integer position);
    Optional<Section> updateSection(UUID id, String title, Integer position);
    boolean deleteSection(UUID id);
    List<Section> reorderSections(UUID courseId, List<UUID> sectionIds);
}