package com.example.coursebe.pattern.strategy;

import com.example.coursebe.model.Course;
import com.example.coursebe.repository.CourseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;


@Component("keyword")
public class SearchByKeywordStrategy implements CourseSearchStrategy {
    private final CourseRepository courseRepository;

    public SearchByKeywordStrategy(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public Page<Course> search(String keyword, Pageable pageable) {
        return courseRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword, pageable);
    }
}
