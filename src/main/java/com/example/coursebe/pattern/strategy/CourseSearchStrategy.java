package com.example.coursebe.pattern.strategy;

import com.example.coursebe.model.Course;

import java.util.List;

public interface CourseSearchStrategy {
    List<Course> search(String keyword);
}

