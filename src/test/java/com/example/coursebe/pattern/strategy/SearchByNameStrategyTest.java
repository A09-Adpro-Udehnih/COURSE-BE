package com.example.coursebe.pattern.strategy;

import com.example.coursebe.model.Course;
import com.example.coursebe.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
    void testSearch_WithPagination() {
        // Given
        List<Course> mockCourseList = List.of(new Course());
        Page<Course> mockCoursePage = new PageImpl<>(mockCourseList);
        Pageable pageable = PageRequest.of(0, 10);

        when(courseRepository.findByNameContainingIgnoreCase("test", pageable))
                .thenReturn(mockCoursePage);

        // When
        Page<Course> result = strategy.search("test", pageable);

        // Then
        assertEquals(mockCoursePage, result);
        assertEquals(mockCourseList, result.getContent());
        verify(courseRepository, times(1)).findByNameContainingIgnoreCase("test", pageable);
    }

    @Test
    void testSearch_EmptyKeyword_WithPagination() {
        // Given
        Page<Course> mockEmptyPage = new PageImpl<>(List.of());
        Pageable pageable = PageRequest.of(0, 5);

        when(courseRepository.findByNameContainingIgnoreCase("", pageable))
                .thenReturn(mockEmptyPage);

        // When
        Page<Course> result = strategy.search("", pageable);

        // Then
        assertEquals(mockEmptyPage, result);
        assertTrue(result.isEmpty());
        verify(courseRepository, times(1)).findByNameContainingIgnoreCase("", pageable);
    }

    @Test
    void testSearch_NullKeyword_WithPagination() {
        // Given
        Page<Course> mockEmptyPage = new PageImpl<>(List.of());
        Pageable pageable = PageRequest.of(0, 20);

        when(courseRepository.findByNameContainingIgnoreCase(null, pageable))
                .thenReturn(mockEmptyPage);

        // When
        Page<Course> result = strategy.search(null, pageable);

        // Then
        assertEquals(mockEmptyPage, result);
        assertTrue(result.isEmpty());
        verify(courseRepository, times(1)).findByNameContainingIgnoreCase(null, pageable);
    }

    @Test
    void testSearch_DifferentPageSizes() {
        // Given
        List<Course> mockCourseList = List.of(new Course(), new Course());
        Pageable firstPageable = PageRequest.of(0, 2);
        Pageable secondPageable = PageRequest.of(1, 1);

        Page<Course> firstPage = new PageImpl<>(mockCourseList, firstPageable, 3);
        Page<Course> secondPage = new PageImpl<>(List.of(new Course()), secondPageable, 3);

        when(courseRepository.findByNameContainingIgnoreCase("java", firstPageable))
                .thenReturn(firstPage);
        when(courseRepository.findByNameContainingIgnoreCase("java", secondPageable))
                .thenReturn(secondPage);

        // When
        Page<Course> firstResult = strategy.search("java", firstPageable);
        Page<Course> secondResult = strategy.search("java", secondPageable);

        // Then
        assertEquals(firstPage, firstResult);
        assertEquals(secondPage, secondResult);
        assertEquals(2, firstResult.getContent().size());
        assertEquals(1, secondResult.getContent().size());
        assertEquals(0, firstResult.getNumber());
        assertEquals(1, secondResult.getNumber());

        verify(courseRepository, times(1)).findByNameContainingIgnoreCase("java", firstPageable);
        verify(courseRepository, times(1)).findByNameContainingIgnoreCase("java", secondPageable);
    }
}