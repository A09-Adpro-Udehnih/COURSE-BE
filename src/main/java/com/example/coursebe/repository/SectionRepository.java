package com.example.coursebe.repository;

import com.example.coursebe.model.Course;
import com.example.coursebe.model.Section;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface SectionRepository extends JpaRepository<Section, UUID> {
    List<Section> findByCourse(Course course);
}
