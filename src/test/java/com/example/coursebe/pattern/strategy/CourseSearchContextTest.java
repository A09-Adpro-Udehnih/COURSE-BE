package com.example.coursebe.pattern.strategy;

import com.example.coursebe.model.Course;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CourseSearchContextTest {
    private CourseSearchContext courseSearchContext;
    private CourseSearchStrategy keywordStrategy;
    private CourseSearchStrategy nameStrategy;
    private Pageable pageable;
    private Page<Course> mockPage;

    @BeforeEach
    void setUp() {
        keywordStrategy = mock(CourseSearchStrategy.class);
        nameStrategy = mock(CourseSearchStrategy.class);
        pageable = PageRequest.of(0, 10);
        mockPage = new PageImpl<>(List.of(new Course()));

        // Setup mocks for pagination
        when(keywordStrategy.search(anyString(), any(Pageable.class))).thenReturn(mockPage);
        when(nameStrategy.search(anyString(), any(Pageable.class))).thenReturn(mockPage);

        courseSearchContext = new CourseSearchContext(Map.of(
                "keyword", keywordStrategy,
                "name", nameStrategy
        ));
    }

    @Test
    void testGetStrategy_ValidType() {
        // Get strategies
        CourseSearchStrategy keywordResult = courseSearchContext.getStrategy("keyword");
        CourseSearchStrategy nameResult = courseSearchContext.getStrategy("name");

        // Verify correct strategies returned
        assertEquals(keywordStrategy, keywordResult);
        assertEquals(nameStrategy, nameResult);

        // Verify pagination works on returned strategies
        assertEquals(mockPage, keywordResult.search("test", pageable));
        assertEquals(mockPage, nameResult.search("test", pageable));
    }

    @Test
    void testGetStrategy_InvalidTypeReturnsNull() {
        assertNull(courseSearchContext.getStrategy("invalid"));
    }

    @Test
    void testIsValidStrategy() {
        assertTrue(courseSearchContext.isValidStrategy("keyword"));
        assertTrue(courseSearchContext.isValidStrategy("name"));
        assertFalse(courseSearchContext.isValidStrategy("invalid"));
        assertFalse(courseSearchContext.isValidStrategy(null));
    }

    @Test
    void testStrategyExecutionWithPagination() {
        // Get a strategy and execute search with pagination
        CourseSearchStrategy strategy = courseSearchContext.getStrategy("keyword");
        Page<Course> result = strategy.search("java", pageable);

        // Verify pagination results
        assertNotNull(result);
        assertEquals(mockPage, result);
        assertEquals(mockPage.getContent(), result.getContent());
        assertEquals(mockPage.getTotalElements(), result.getTotalElements());
    }

    @Test
    void testMultipleStrategiesWithDifferentPagination() {
        // Setup different pages for different strategies
        Page<Course> keywordPage = new PageImpl<>(List.of(new Course(), new Course()),
                PageRequest.of(0, 2), 5);
        Page<Course> namePage = new PageImpl<>(List.of(new Course()),
                PageRequest.of(1, 1), 3);

        when(keywordStrategy.search("test", pageable)).thenReturn(keywordPage);
        when(nameStrategy.search("test", pageable)).thenReturn(namePage);

        // Execute searches with both strategies
        Page<Course> keywordResult = courseSearchContext.getStrategy("keyword").search("test", pageable);
        Page<Course> nameResult = courseSearchContext.getStrategy("name").search("test", pageable);

        // Verify each strategy returns the correct page
        assertEquals(2, keywordResult.getContent().size());
        assertEquals(5, keywordResult.getTotalElements());

        assertEquals(1, nameResult.getContent().size());
        assertEquals(3, nameResult.getTotalElements());
    }
}