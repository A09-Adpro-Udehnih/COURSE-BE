package com.example.coursebe.pattern.strategy;

import com.example.coursebe.model.Course;
import com.example.coursebe.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("name")
public class SearchByNameStrategy implements CourseSearchStrategy {
    private final CourseRepository courseRepository;

    @Autowired
    public SearchByNameStrategy(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public List<Course> search(String keyword) {
        return courseRepository.findByNameContainingIgnoreCase(keyword);
    }
}