package com.example.coursebe.pattern.strategy;

import com.example.coursebe.model.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Component
public class CourseSearchContext {

    private final Map<String, CourseSearchStrategy> strategies;

    @Autowired
    public CourseSearchContext(Map<String, CourseSearchStrategy> strategies) {
        this.strategies = strategies;
    }

    public CourseSearchStrategy getStrategy(String type) {
        return strategies.getOrDefault(type, strategies.get("keyword"));
    }
}
