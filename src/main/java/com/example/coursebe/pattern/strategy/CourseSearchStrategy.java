package com.example.coursebe.pattern.strategy;

import com.example.coursebe.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseSearchStrategy {
    Page<Course> search(String keyword, Pageable pageable);
}

