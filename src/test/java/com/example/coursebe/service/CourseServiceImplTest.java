package com.example.coursebe.service;

import com.example.coursebe.model.Course;
import com.example.coursebe.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CourseServiceImplTest {

    @Mock
    private CourseRepository courseRepository;

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
    @DisplayName("Should get all courses")
    void getAllCourses() {
        // Given
        when(courseRepository.findAll()).thenReturn(testCourses);
        
        // When
        List<Course> result = courseService.getAllCourses();
        
        // Then
        assertEquals(2, result.size());
        assertEquals(testCourses, result);
        verify(courseRepository).findAll();
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
    @DisplayName("Should search courses by name")
    void searchCoursesByName() {
        // Given
        String keyword = "Java";
        when(courseRepository.findByNameContainingIgnoreCase(keyword))
            .thenReturn(Arrays.asList(testCourses.get(1)));
        
        // When
        List<Course> result = courseService.searchCoursesByName(keyword);
        
        // Then
        assertEquals(1, result.size());
        assertEquals("Java Course", result.get(0).getName());
        verify(courseRepository).findByNameContainingIgnoreCase(keyword);
    }

    @Test
    @DisplayName("Should create course")
    void createCourse() {
        // Given
        String name = "New Course";
        String description = "New Description";
        BigDecimal price = new BigDecimal("79.99");
        
        Course newCourse = new Course(name, description, tutorId, price);
        when(courseRepository.save(any(Course.class))).thenReturn(newCourse);
        
        // When
        Course result = courseService.createCourse(name, description, tutorId, price);
        
        // Then
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(description, result.getDescription());
        assertEquals(tutorId, result.getTutorId());
        assertEquals(price, result.getPrice());
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("Should throw exception when creating course with null name")
    void createCourseWithNullName() {
        // Given
        String name = null;
        String description = "Description";
        BigDecimal price = new BigDecimal("79.99");
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            courseService.createCourse(name, description, tutorId, price);
        });
        
        assertEquals("Course name cannot be empty", exception.getMessage());
        verify(courseRepository, never()).save(any(Course.class));
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
        Optional<Course> result = courseService.updateCourse(courseId, updatedName, updatedDescription, updatedPrice);
        
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
        Optional<Course> result = courseService.updateCourse(nonExistentId, "Name", "Description", new BigDecimal("99.99"));
        
        // Then
        assertFalse(result.isPresent());
        verify(courseRepository).findById(nonExistentId);
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("Should delete course")
    void deleteCourse() {
        // Given
        when(courseRepository.existsById(courseId)).thenReturn(true);
        
        // When
        boolean result = courseService.deleteCourse(courseId);
        
        // Then
        assertTrue(result);
        verify(courseRepository).existsById(courseId);
        verify(courseRepository).deleteById(courseId);
    }

    @Test
    @DisplayName("Should return false when deleting non-existent course")
    void deleteNonExistentCourse() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(courseRepository.existsById(nonExistentId)).thenReturn(false);
        
        // When
        boolean result = courseService.deleteCourse(nonExistentId);
        
        // Then
        assertFalse(result);
        verify(courseRepository).existsById(nonExistentId);
        verify(courseRepository, never()).deleteById(any(UUID.class));
    }
}