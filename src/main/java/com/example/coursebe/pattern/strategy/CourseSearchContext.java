package com.example.coursebe.pattern.strategy;

import org.springframework.stereotype.Component;
import java.util.Map;


@Component
public class CourseSearchContext {
    private final Map<String, CourseSearchStrategy> strategies;

    public CourseSearchContext(Map<String, CourseSearchStrategy> strategies) {
        this.strategies = strategies;
    }

    public CourseSearchStrategy getStrategy(String type) {
        return strategies.get(type);
    }

    public boolean isValidStrategy(String type) {
        return type != null && strategies.containsKey(type);
    }
}
