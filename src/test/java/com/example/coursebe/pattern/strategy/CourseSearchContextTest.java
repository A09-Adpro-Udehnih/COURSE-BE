package com.example.coursebe.pattern.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class CourseSearchContextTest {
    private CourseSearchContext courseSearchContext;
    private CourseSearchStrategy keywordStrategy;
    private CourseSearchStrategy nameStrategy;

    @BeforeEach
    void setUp() {
        keywordStrategy = mock(CourseSearchStrategy.class);
        nameStrategy = mock(CourseSearchStrategy.class);

        courseSearchContext = new CourseSearchContext(Map.of(
                "keyword", keywordStrategy,
                "name", nameStrategy
        ));
    }

    @Test
    void testGetStrategy_ValidType() {
        assertEquals(keywordStrategy, courseSearchContext.getStrategy("keyword"));
        assertEquals(nameStrategy, courseSearchContext.getStrategy("name"));
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
}