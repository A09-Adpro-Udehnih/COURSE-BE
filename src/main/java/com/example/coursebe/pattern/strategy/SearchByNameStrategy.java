package com.example.coursebe.pattern.strategy;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.example.coursebe.model.Course;
import com.example.coursebe.repository.CourseRepository;

@Component("name")
public class SearchByNameStrategy implements CourseSearchStrategy {
    private final CourseRepository courseRepository;

    public SearchByNameStrategy(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public Page<Course> search(String keyword, Pageable pageable) {
        return courseRepository.findByNameContainingIgnoreCase(keyword, pageable);
    }

    @Override
    public Page<Course> searchForUser(UUID userId, String keyword, Pageable pageable) {
        return courseRepository.findByEnrollmentsStudentIdAndNameContainingIgnoreCase(userId, keyword, pageable);
    }
}