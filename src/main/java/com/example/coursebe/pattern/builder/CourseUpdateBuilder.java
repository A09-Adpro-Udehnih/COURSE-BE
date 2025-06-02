package com.example.coursebe.pattern.builder;

import com.example.coursebe.model.Course;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;

public class CourseUpdateBuilder {
    private final Course course;
    
    public CourseUpdateBuilder(Course course) {
        this.course = course;
    }
    
    public CourseUpdateBuilder updateName(String name) {
        if (StringUtils.hasText(name)) {
            course.setName(name.trim());
        }
        return this;
    }
    
    public CourseUpdateBuilder updateDescription(String description) {
        if (StringUtils.hasText(description)) {
            course.setDescription(description.trim());
        }
        return this;
    }
    
    public CourseUpdateBuilder updatePrice(BigDecimal price) {
        if (price != null) {
            course.setPrice(price);
        }
        return this;
    }
    
    public Course build() {
        return course;
    }
}
