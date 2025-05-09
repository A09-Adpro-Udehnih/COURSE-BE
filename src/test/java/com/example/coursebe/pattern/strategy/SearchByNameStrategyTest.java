package com.example.coursebe.pattern.strategy;

import com.example.coursebe.model.Course;
import com.example.coursebe.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SearchByNameStrategyTest {

    @Mock
    private CourseRepository courseRepository;

    private SearchByNameStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        strategy = new SearchByNameStrategy(courseRepository);
    }

    @Test
    void testSearch() {
        List<Course> mockCourses = List.of(new Course());
        when(courseRepository.findByNameContainingIgnoreCase("test"))
                .thenReturn(mockCourses);

        List<Course> result = strategy.search("test");
        assertEquals(mockCourses, result);
        verify(courseRepository, times(1)).findByNameContainingIgnoreCase("test");
    }

    @Test
    void testSearch_EmptyKeyword() {
        List<Course> mockCourses = new ArrayList<>();
        when(courseRepository.findByNameContainingIgnoreCase(""))
                .thenReturn(mockCourses);

        List<Course> result = strategy.search("");
        assertEquals(mockCourses, result);
        verify(courseRepository, times(1)).findByNameContainingIgnoreCase("");
    }

    @Test
    void testSearch_NullKeyword() {
        List<Course> mockCourses = new ArrayList<>();
        when(courseRepository.findByNameContainingIgnoreCase(null))
                .thenReturn(mockCourses);

        List<Course> result = strategy.search(null);
        assertEquals(mockCourses, result);
        verify(courseRepository, times(1)).findByNameContainingIgnoreCase(null);
    }
}