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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SearchByKeywordStrategyTest {
    @Mock
    private CourseRepository courseRepository;
    private SearchByKeywordStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        strategy = new SearchByKeywordStrategy(courseRepository);
    }

    @Test
    void testSearch_WithPagination() {
        // Given
        List<Course> mockCourseList = List.of(new Course());
        Page<Course> mockCoursePage = new PageImpl<>(mockCourseList);
        Pageable pageable = PageRequest.of(0, 10);

        when(courseRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase("test", "test", pageable))
                .thenReturn(mockCoursePage);

        // When
        Page<Course> result = strategy.search("test", pageable);

        // Then
        assertEquals(mockCoursePage, result);
        assertEquals(mockCourseList, result.getContent());
        verify(courseRepository, times(1))
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase("test", "test", pageable);
    }

    @Test
    void testSearch_EmptyKeyword_WithPagination() {
        // Given
        Page<Course> mockEmptyPage = new PageImpl<>(List.of());
        Pageable pageable = PageRequest.of(0, 5);

        when(courseRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase("", "", pageable))
                .thenReturn(mockEmptyPage);

        // When
        Page<Course> result = strategy.search("", pageable);

        // Then
        assertEquals(mockEmptyPage, result);
        assertTrue(result.isEmpty());
        verify(courseRepository, times(1))
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase("", "", pageable);
    }

    @Test
    void testSearch_NullKeyword_WithPagination() {
        // Given
        Page<Course> mockEmptyPage = new PageImpl<>(List.of());
        Pageable pageable = PageRequest.of(0, 20);

        when(courseRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(null, null, pageable))
                .thenReturn(mockEmptyPage);

        // When
        Page<Course> result = strategy.search(null, pageable);

        // Then
        assertEquals(mockEmptyPage, result);
        assertTrue(result.isEmpty());
        verify(courseRepository, times(1))
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(null, null, pageable);
    }

    @Test
    void testSearch_DifferentPageSizes() {
        // Given
        List<Course> mockCourseList = List.of(new Course(), new Course());
        Pageable firstPageable = PageRequest.of(0, 2);
        Pageable secondPageable = PageRequest.of(1, 1);

        Page<Course> firstPage = new PageImpl<>(mockCourseList, firstPageable, 3);
        Page<Course> secondPage = new PageImpl<>(List.of(new Course()), secondPageable, 3);

        when(courseRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase("java", "java", firstPageable))
                .thenReturn(firstPage);
        when(courseRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase("java", "java", secondPageable))
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

        verify(courseRepository, times(1))
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase("java", "java", firstPageable);
        verify(courseRepository, times(1))
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase("java", "java", secondPageable);
    }

    @Test
    void testSearchForUser_WithValidInputs() {
        // Given
        UUID userId = UUID.randomUUID();
        String keyword = "java";
        Pageable pageable = PageRequest.of(0, 10);

        List<Course> mockCourseList = List.of(new Course(), new Course());
        Page<Course> mockCoursePage = new PageImpl<>(mockCourseList, pageable, 2);

        when(courseRepository.findByEnrollmentsStudentIdAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                userId, keyword, keyword, pageable))
                .thenReturn(mockCoursePage);

        // When
        Page<Course> result = strategy.searchForUser(userId, keyword, pageable);

        // Then
        assertEquals(mockCoursePage, result);
        assertEquals(2, result.getTotalElements());
        assertEquals(mockCourseList, result.getContent());
        verify(courseRepository).findByEnrollmentsStudentIdAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                userId, keyword, keyword, pageable);
    }

    @Test
    void testSearchForUser_WithEmptyKeyword() {
        // Given
        UUID userId = UUID.randomUUID();
        String keyword = "";
        Pageable pageable = PageRequest.of(0, 5);

        Page<Course> mockEmptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(courseRepository.findByEnrollmentsStudentIdAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                userId, keyword, keyword, pageable))
                .thenReturn(mockEmptyPage);

        // When
        Page<Course> result = strategy.searchForUser(userId, keyword, pageable);

        // Then
        assertEquals(mockEmptyPage, result);
        assertTrue(result.isEmpty());
        verify(courseRepository).findByEnrollmentsStudentIdAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                userId, keyword, keyword, pageable);
    }

    @Test
    void testSearchForUser_WithNullKeyword() {
        // Given
        UUID userId = UUID.randomUUID();
        String keyword = null;
        Pageable pageable = PageRequest.of(0, 20);

        Page<Course> mockEmptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(courseRepository.findByEnrollmentsStudentIdAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                userId, keyword, keyword, pageable))
                .thenReturn(mockEmptyPage);

        // When
        Page<Course> result = strategy.searchForUser(userId, keyword, pageable);

        // Then
        assertEquals(mockEmptyPage, result);
        assertTrue(result.isEmpty());
        verify(courseRepository).findByEnrollmentsStudentIdAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                userId, keyword, keyword, pageable);
    }

    @Test
    void testSearchForUser_WithNullUserId() {
        // Given
        UUID userId = null;
        String keyword = "java";
        Pageable pageable = PageRequest.of(0, 10);

        Page<Course> mockEmptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(courseRepository.findByEnrollmentsStudentIdAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                userId, keyword, keyword, pageable))
                .thenReturn(mockEmptyPage);

        // When
        Page<Course> result = strategy.searchForUser(userId, keyword, pageable);

        // Then
        assertEquals(mockEmptyPage, result);
        assertTrue(result.isEmpty());
        verify(courseRepository).findByEnrollmentsStudentIdAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                userId, keyword, keyword, pageable);
    }

    @Test
    void testSearchForUser_Pagination() {
        // Given
        UUID userId = UUID.randomUUID();
        String keyword = "programming";

        // First page (2 items per page)
        Pageable firstPageable = PageRequest.of(0, 2);
        List<Course> firstPageCourses = List.of(new Course(), new Course());
        Page<Course> firstPage = new PageImpl<>(firstPageCourses, firstPageable, 5); // 5 total items

        // Second page (2 items per page)
        Pageable secondPageable = PageRequest.of(1, 2);
        List<Course> secondPageCourses = List.of(new Course(), new Course());
        Page<Course> secondPage = new PageImpl<>(secondPageCourses, secondPageable, 5);

        when(courseRepository.findByEnrollmentsStudentIdAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                userId, keyword, keyword, firstPageable))
                .thenReturn(firstPage);

        when(courseRepository.findByEnrollmentsStudentIdAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                userId, keyword, keyword, secondPageable))
                .thenReturn(secondPage);

        // When
        Page<Course> firstResult = strategy.searchForUser(userId, keyword, firstPageable);
        Page<Course> secondResult = strategy.searchForUser(userId, keyword, secondPageable);

        // Then
        // Verify first page
        assertEquals(5, firstResult.getTotalElements());
        assertEquals(3, firstResult.getTotalPages());
        assertEquals(0, firstResult.getNumber());
        assertEquals(2, firstResult.getNumberOfElements());

        // Verify second page
        assertEquals(5, secondResult.getTotalElements());
        assertEquals(3, secondResult.getTotalPages());
        assertEquals(1, secondResult.getNumber());
        assertEquals(2, secondResult.getNumberOfElements());

        // Verify repository calls
        verify(courseRepository).findByEnrollmentsStudentIdAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                userId, keyword, keyword, firstPageable);
        verify(courseRepository).findByEnrollmentsStudentIdAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                userId, keyword, keyword, secondPageable);
    }

    @Test
    void testSearchForUser_WithDifferentPageSizes() {
        // Given
        UUID userId = UUID.randomUUID();
        String keyword = "java";

        // Page with 10 items per page
        Pageable largePageable = PageRequest.of(0, 10);
        List<Course> largeCourseList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            largeCourseList.add(new Course());
        }
        Page<Course> largePage = new PageImpl<>(largeCourseList, largePageable, 5);

        // Page with 2 items per page
        Pageable smallPageable = PageRequest.of(0, 2);
        List<Course> smallCourseList = largeCourseList.subList(0, 2);
        Page<Course> smallPage = new PageImpl<>(smallCourseList, smallPageable, 5);

        when(courseRepository.findByEnrollmentsStudentIdAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                userId, keyword, keyword, largePageable))
                .thenReturn(largePage);

        when(courseRepository.findByEnrollmentsStudentIdAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                userId, keyword, keyword, smallPageable))
                .thenReturn(smallPage);

        // When
        Page<Course> largeResult = strategy.searchForUser(userId, keyword, largePageable);
        Page<Course> smallResult = strategy.searchForUser(userId, keyword, smallPageable);

        // Then
        // Large page should have all 5 courses
        assertEquals(5, largeResult.getTotalElements());
        assertEquals(5, largeResult.getContent().size());
        assertEquals(1, largeResult.getTotalPages());

        // Small page should have 2 courses but still show total of 5
        assertEquals(5, smallResult.getTotalElements());
        assertEquals(2, smallResult.getContent().size());
        assertEquals(3, smallResult.getTotalPages());

        verify(courseRepository).findByEnrollmentsStudentIdAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                userId, keyword, keyword, largePageable);
        verify(courseRepository).findByEnrollmentsStudentIdAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                userId, keyword, keyword, smallPageable);
    }
}