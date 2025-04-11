package com.example.coursebe.service;

import com.example.coursebe.model.Course;
import com.example.coursebe.pattern.strategy.CourseSearchContext;
import com.example.coursebe.pattern.strategy.CourseSearchStrategy;
import com.example.coursebe.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseSearchContext courseSearchContext;

    @Autowired
    public CourseService(CourseRepository courseRepository, CourseSearchContext courseSearchContext) {
        this.courseRepository = courseRepository;
        this.courseSearchContext = courseSearchContext;
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Optional<Course> getCourseById(UUID id) {
        return courseRepository.findById(id);
    }

    public List<Course> searchCourses(String query, String type) {
        CourseSearchStrategy strategy = courseSearchContext.getStrategy(type);
        return strategy.search(query);
    }

}
