package com.example.coursebe.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.coursebe.model.Course;
import com.example.coursebe.model.Section;

@Repository
public interface SectionRepository extends JpaRepository<Section, UUID> {
    List<Section> findByCourse(Course course);
    List<Section> findByCourseOrderByPositionAsc(Course course);
    List<Section> findByCourseId(UUID courseId);

}