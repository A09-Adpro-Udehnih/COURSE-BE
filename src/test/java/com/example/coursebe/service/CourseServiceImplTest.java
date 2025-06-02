package com.example.coursebe.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.coursebe.exception.UnsupportedSearchTypeException;
import com.example.coursebe.model.Course;
import java.util.Collections;
import com.example.coursebe.pattern.strategy.CourseSearchContext;
import com.example.coursebe.pattern.strategy.CourseSearchStrategy;
import com.example.coursebe.repository.CourseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class CourseServiceImplTest {
    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseSearchContext courseSearchContext;

    @Mock
    private CourseSearchStrategy mockSearchStrategy;

    @InjectMocks
    private CourseServiceImpl courseService;

    private UUID courseId;
    private UUID tutorId;
    private Course testCourse;
    private List<Course> testCourses;

    @BeforeEach
    void setUp() {
        courseId = UUID.randomUUID();
        tutorId = UUID.randomUUID();

        testCourse = new Course("Test Course", "Test Description", tutorId, new BigDecimal("99.99"));
        // Use reflection to set the ID since it's normally generated
        try {
            java.lang.reflect.Field field = Course.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(testCourse, courseId);
        } catch (Exception e) {
            fail("Failed to set course ID");
        }

        UUID courseId2 = UUID.randomUUID();
        Course course2 = new Course("Java Course", "Learn Java", tutorId, new BigDecimal("149.99"));
        try {
            java.lang.reflect.Field field = Course.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(course2, courseId2);
        } catch (Exception e) {
            fail("Failed to set course ID");
        }

        testCourses = Arrays.asList(testCourse, course2);
    }

    @Test
    @DisplayName("Should get all courses with pagination")
    void getAllCoursesWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> coursePage = new PageImpl<>(testCourses, pageable, 2);
        when(courseRepository.findAll(any(Pageable.class))).thenReturn(coursePage);

        // When
        Page<Course> result = courseService.getAllCourses(pageable);

        // Then
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(0, result.getNumber());
        assertEquals(testCourses, result.getContent());
        verify(courseRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Should get empty page when no courses exist")
    void getAllCoursesWithPaginationEmpty() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(courseRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        // When
        Page<Course> result = courseService.getAllCourses(pageable);

        // Then
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
        verify(courseRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Should get course by ID")
    void getCourseById() {
        // Given
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));

        // When
        Optional<Course> result = courseService.getCourseById(courseId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testCourse, result.get());
        verify(courseRepository).findById(courseId);
    }

    @Test
    @DisplayName("Should return empty optional when course not found")
    void getCourseByIdNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(courseRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<Course> result = courseService.getCourseById(nonExistentId);

        // Then
        assertFalse(result.isPresent());
        verify(courseRepository).findById(nonExistentId);
    }

    @Test
    @DisplayName("Should get courses by tutor ID")
    void getCoursesByTutorId() {
        // Given
        when(courseRepository.findByTutorId(tutorId)).thenReturn(testCourses);

        // When
        List<Course> result = courseService.getCoursesByTutorId(tutorId);

        // Then
        assertEquals(2, result.size());
        assertEquals(testCourses, result);
        verify(courseRepository).findByTutorId(tutorId);
    }

    @Test
    @DisplayName("Should search courses using keyword strategy with pagination")
    void searchCourses_WithKeywordStrategy() {
        // Given
        String type = "keyword";
        String keyword = "Java";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> keywordPage = new PageImpl<>(List.of(testCourses.get(1)), pageable, 1);

        when(courseSearchContext.isValidStrategy(type)).thenReturn(true);
        when(courseSearchContext.getStrategy(type)).thenReturn(mockSearchStrategy);
        when(mockSearchStrategy.search(keyword, pageable)).thenReturn(keywordPage);

        // When
        Page<Course> result = courseService.searchCourses(type, keyword, pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals("Java Course", result.getContent().get(0).getName());
        verify(courseSearchContext).isValidStrategy(type);
        verify(courseSearchContext).getStrategy(type);
        verify(mockSearchStrategy).search(keyword, pageable);
    }

    @Test
    @DisplayName("Should search courses using name strategy with pagination")
    void searchCourses_WithNameStrategy() {
        // Given
        String type = "name";
        String keyword = "Test";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> namePage = new PageImpl<>(List.of(testCourses.get(0)), pageable, 1);

        when(courseSearchContext.isValidStrategy(type)).thenReturn(true);
        when(courseSearchContext.getStrategy(type)).thenReturn(mockSearchStrategy);
        when(mockSearchStrategy.search(keyword, pageable)).thenReturn(namePage);

        // When
        Page<Course> result = courseService.searchCourses(type, keyword, pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Course", result.getContent().get(0).getName());
        verify(courseSearchContext).isValidStrategy(type);
        verify(courseSearchContext).getStrategy(type);
        verify(mockSearchStrategy).search(keyword, pageable);
    }

    @Test
    @DisplayName("Should throw UnsupportedSearchTypeException when invalid type is provided")
    void searchCourses_WithInvalidStrategy() {
        // Given
        String type = "invalid";
        String keyword = "Course";
        Pageable pageable = PageRequest.of(0, 10);

        when(courseSearchContext.isValidStrategy(type)).thenReturn(false);

        // When & Then
        Exception exception = assertThrows(UnsupportedSearchTypeException.class, () -> {
            courseService.searchCourses(type, keyword, pageable);
        });

        verify(courseSearchContext).isValidStrategy(type);
        verify(courseSearchContext, never()).getStrategy(anyString());
        verify(mockSearchStrategy, never()).search(anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("Should handle empty search results with pagination")
    void searchCourses_WithNoResults() {
        // Given
        String type = "keyword";
        String keyword = "Nonexistent";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(courseSearchContext.isValidStrategy(type)).thenReturn(true);
        when(courseSearchContext.getStrategy(type)).thenReturn(mockSearchStrategy);
        when(mockSearchStrategy.search(keyword, pageable)).thenReturn(emptyPage);

        // When
        Page<Course> result = courseService.searchCourses(type, keyword, pageable);

        // Then
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
        verify(courseSearchContext).isValidStrategy(type);
        verify(courseSearchContext).getStrategy(type);
        verify(mockSearchStrategy).search(keyword, pageable);
    }

    @Test
    @DisplayName("Should get enrolled courses for a user with pagination")
    void getEnrolledCourses() {
        // Given
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> enrolledCoursesPage = new PageImpl<>(testCourses, pageable, 2);

        when(courseRepository.findByEnrollmentsStudentId(userId, pageable)).thenReturn(enrolledCoursesPage);

        // When
        Page<Course> result = courseService.getEnrolledCourses(userId, pageable);

        // Then
        assertEquals(2, result.getTotalElements());
        assertEquals(testCourses, result.getContent());
        verify(courseRepository).findByEnrollmentsStudentId(userId, pageable);
    }

    @Test
    @DisplayName("Should return empty page when user has no enrolled courses")
    void getEnrolledCoursesEmpty() {
        // Given
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(courseRepository.findByEnrollmentsStudentId(userId, pageable)).thenReturn(emptyPage);

        // When
        Page<Course> result = courseService.getEnrolledCourses(userId, pageable);

        // Then
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
        verify(courseRepository).findByEnrollmentsStudentId(userId, pageable);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when getting enrolled courses with null userId")
    void getEnrolledCoursesNullUserId() {
        // Given
        UUID userId = null;
        Pageable pageable = PageRequest.of(0, 10);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            courseService.getEnrolledCourses(userId, pageable);
        });

        assertEquals("User ID cannot be null", exception.getMessage());
        verify(courseRepository, never()).findByEnrollmentsStudentId(any(), any());
    }

    @Test
    @DisplayName("Should search enrolled courses using strategy with pagination")
    void searchEnrolledCourses_WithValidStrategy() {
        // Given
        UUID userId = UUID.randomUUID();
        String type = "name";
        String keyword = "Java";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> coursePage = new PageImpl<>(List.of(testCourses.get(1)), pageable, 1);

        when(courseSearchContext.isValidStrategy(type)).thenReturn(true);
        when(courseSearchContext.getStrategy(type)).thenReturn(mockSearchStrategy);
        when(mockSearchStrategy.searchForUser(userId, keyword, pageable)).thenReturn(coursePage);

        // When
        Page<Course> result = courseService.searchEnrolledCourses(userId, type, keyword, pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals("Java Course", result.getContent().get(0).getName());
        verify(courseSearchContext).isValidStrategy(type);
        verify(courseSearchContext).getStrategy(type);
        verify(mockSearchStrategy).searchForUser(userId, keyword, pageable);
    }

    @Test
    @DisplayName("Should throw UnsupportedSearchTypeException when searching enrolled courses with invalid strategy")
    void searchEnrolledCourses_WithInvalidStrategy() {
        // Given
        UUID userId = UUID.randomUUID();
        String type = "invalid";
        String keyword = "Java";
        Pageable pageable = PageRequest.of(0, 10);

        when(courseSearchContext.isValidStrategy(type)).thenReturn(false);

        // When & Then
        Exception exception = assertThrows(UnsupportedSearchTypeException.class, () -> {
            courseService.searchEnrolledCourses(userId, type, keyword, pageable);
        });

        verify(courseSearchContext).isValidStrategy(type);
        verify(courseSearchContext, never()).getStrategy(anyString());
        verify(mockSearchStrategy, never()).searchForUser(any(), anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when searching enrolled courses with null userId")
    void searchEnrolledCourses_WithNullUserId() {
        // Given
        UUID userId = null;
        String type = "name";
        String keyword = "Java";
        Pageable pageable = PageRequest.of(0, 10);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            courseService.searchEnrolledCourses(userId, type, keyword, pageable);
        });

        assertEquals("User ID cannot be null", exception.getMessage());
        verify(courseSearchContext, never()).isValidStrategy(anyString());
        verify(courseSearchContext, never()).getStrategy(anyString());
        verify(mockSearchStrategy, never()).searchForUser(any(), anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("Should handle empty search results when searching enrolled courses")
    void searchEnrolledCourses_WithNoResults() {
        // Given
        UUID userId = UUID.randomUUID();
        String type = "keyword";
        String keyword = "Nonexistent";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(courseSearchContext.isValidStrategy(type)).thenReturn(true);
        when(courseSearchContext.getStrategy(type)).thenReturn(mockSearchStrategy);
        when(mockSearchStrategy.searchForUser(userId, keyword, pageable)).thenReturn(emptyPage);

        // When
        Page<Course> result = courseService.searchEnrolledCourses(userId, type, keyword, pageable);

        // Then
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
        verify(courseSearchContext).isValidStrategy(type);
        verify(courseSearchContext).getStrategy(type);
        verify(mockSearchStrategy).searchForUser(userId, keyword, pageable);
    }

    @Test
    @DisplayName("Should update course")
    void updateCourse() {
        // Given
        String updatedName = "Updated Course";
        String updatedDescription = "Updated Description";
        BigDecimal updatedPrice = new BigDecimal("129.99");

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));
        when(courseRepository.save(any(Course.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        Optional<Course> result = courseService.updateCourse(courseId, updatedName, updatedDescription, updatedPrice, Collections.emptyList());
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(updatedName, result.get().getName());
        assertEquals(updatedDescription, result.get().getDescription());
        assertEquals(updatedPrice, result.get().getPrice());
        verify(courseRepository).findById(courseId);
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("Should return empty optional when updating non-existent course")
    void updateNonExistentCourse() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(courseRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<Course> result = courseService.updateCourse(nonExistentId, "Name", "Description", new BigDecimal("99.99"), Collections.emptyList());
        
        // Then
        assertFalse(result.isPresent());
        verify(courseRepository).findById(nonExistentId);
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("Should throw exception when updating course with null ID")
    void updateCourseWithNullId() {
        // Given
        String name = "Updated Name";
        String description = "Updated Description";
        BigDecimal price = new BigDecimal("129.99");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            courseService.updateCourse(null, name, description, price, Collections.emptyList());
        });

        assertEquals("Course ID cannot be null", exception.getMessage());
        verify(courseRepository, never()).findById(any());
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("Should update course with null price")
    void updateCourseWithNullPrice() {
        // Given
        String updatedName = "Updated Course";
        String updatedDescription = "Updated Description";
        BigDecimal updatedPrice = null;

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));
        when(courseRepository.save(any(Course.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        Optional<Course> result = courseService.updateCourse(courseId, updatedName, updatedDescription, updatedPrice, Collections.emptyList());

        // Then
        assertTrue(result.isPresent());
        assertEquals(updatedName, result.get().getName());
        assertEquals(updatedDescription, result.get().getDescription());
        assertEquals(testCourse.getPrice(), result.get().getPrice());
        verify(courseRepository).findById(courseId);
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("Should throw exception when searching courses with invalid type")
    void searchCoursesWithInvalidType() {
        // Given
        String type = "invalid";
        String keyword = "Java";
        Pageable pageable = PageRequest.of(0, 10);

        when(courseSearchContext.isValidStrategy(type)).thenReturn(false);

        // When & Then
        UnsupportedSearchTypeException exception = assertThrows(UnsupportedSearchTypeException.class, () -> {
            courseService.searchCourses(type, keyword, pageable);
        });

        assertEquals("Unsupported search type: invalid", exception.getMessage());
        verify(courseSearchContext).isValidStrategy(type);
        verify(courseSearchContext, never()).getStrategy(any());
    }

    @Test
    @DisplayName("Should return empty list when getting courses for non-existent tutor")
    void getCoursesByNonExistentTutorId() {
        // Given
        UUID nonExistentTutorId = UUID.randomUUID();
        when(courseRepository.findByTutorId(nonExistentTutorId)).thenReturn(List.of());

        // When
        List<Course> result = courseService.getCoursesByTutorId(nonExistentTutorId);

        // Then
        assertTrue(result.isEmpty());
        verify(courseRepository).findByTutorId(nonExistentTutorId);
    }
}