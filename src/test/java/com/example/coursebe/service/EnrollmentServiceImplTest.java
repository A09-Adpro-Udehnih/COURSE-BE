package com.example.coursebe.service;

import com.example.coursebe.model.Course;
import com.example.coursebe.model.Enrollment;
import com.example.coursebe.repository.CourseRepository;
import com.example.coursebe.repository.EnrollmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.AsyncResult;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EnrollmentServiceImplTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private EnrollmentServiceImpl enrollmentService;

    private UUID courseId;
    private UUID studentId;
    private UUID enrollmentId;
    private Course testCourse;
    private Enrollment testEnrollment;
    private List<Enrollment> testEnrollments;

    @BeforeEach
    void setUp() {
        courseId = UUID.randomUUID();
        studentId = UUID.randomUUID();
        enrollmentId = UUID.randomUUID();

        testCourse = new Course("Test Course", "Test Description", UUID.randomUUID(), new BigDecimal("99.99"));
        // Set course ID using reflection
        try {
            java.lang.reflect.Field field = Course.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(testCourse, courseId);
        } catch (Exception e) {
            fail("Failed to set course ID");
        }

        testEnrollment = new Enrollment(studentId, testCourse);
        // Set enrollment ID using reflection
        try {
            java.lang.reflect.Field field = Enrollment.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(testEnrollment, enrollmentId);
        } catch (Exception e) {
            fail("Failed to set enrollment ID");
        }

        UUID otherStudentId = UUID.randomUUID();
        Enrollment enrollment2 = new Enrollment(otherStudentId, testCourse);
        try {
            java.lang.reflect.Field field = Enrollment.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(enrollment2, UUID.randomUUID());
        } catch (Exception e) {
            fail("Failed to set enrollment ID");
        }

        testEnrollments = Arrays.asList(testEnrollment, enrollment2);
    }

    @Test
    @DisplayName("Should get enrollments by student ID")
    void getEnrollmentsByStudentId() {
        // Given
        when(enrollmentRepository.findByStudentId(studentId)).thenReturn(Collections.singletonList(testEnrollment));

        // When
        List<Enrollment> result = enrollmentService.getEnrollmentsByStudentId(studentId);

        // Then
        assertEquals(1, result.size());
        assertEquals(testEnrollment, result.get(0));
        verify(enrollmentRepository).findByStudentId(studentId);
    }

    @Test
    @DisplayName("Should get enrollments by course ID")
    void getEnrollmentsByCourseId() {
        // Given
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));
        when(enrollmentRepository.findByCourse(testCourse)).thenReturn(testEnrollments);

        // When
        List<Enrollment> result = enrollmentService.getEnrollmentsByCourseId(courseId);

        // Then
        assertEquals(2, result.size());
        assertEquals(testEnrollments, result);
        verify(courseRepository).findById(courseId);
        verify(enrollmentRepository).findByCourse(testCourse);
    }

    @Test
    @DisplayName("Should return empty list when course not found")
    void getEnrollmentsByCourseIdNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(courseRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        List<Enrollment> result = enrollmentService.getEnrollmentsByCourseId(nonExistentId);

        // Then
        assertTrue(result.isEmpty());
        verify(courseRepository).findById(nonExistentId);
        verify(enrollmentRepository, never()).findByCourse(any(Course.class));
    }

    @Test
    @DisplayName("Should get enrollment by student ID and course")
    void getEnrollment() {
        // Given
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));
        when(enrollmentRepository.findByStudentIdAndCourse(studentId, testCourse))
                .thenReturn(Optional.of(testEnrollment));

        // When
        Optional<Enrollment> result = enrollmentService.getEnrollment(studentId, courseId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testEnrollment, result.get());
        verify(courseRepository).findById(courseId);
        verify(enrollmentRepository).findByStudentIdAndCourse(studentId, testCourse);
    }

    @Test
    @DisplayName("Should return empty optional when course not found for enrollment")
    void getEnrollmentCourseNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(courseRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<Enrollment> result = enrollmentService.getEnrollment(studentId, nonExistentId);

        // Then
        assertFalse(result.isPresent());
        verify(courseRepository).findById(nonExistentId);
        verify(enrollmentRepository, never()).findByStudentIdAndCourse(any(UUID.class), any(Course.class));
    }

    @Test
    @DisplayName("Should check if student is enrolled")
    void isEnrolled() {
        // Given
        when(enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)).thenReturn(true);

        // When
        boolean result = enrollmentService.isEnrolled(studentId, courseId);

        // Then
        assertTrue(result);
        verify(enrollmentRepository).existsByStudentIdAndCourseId(studentId, courseId);
    }

    @Test
    @DisplayName("Should enroll student in course")
    void enroll() throws ExecutionException, InterruptedException {
        // Given
        when(enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)).thenReturn(false);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(i -> {
            Enrollment enrollment = (Enrollment) i.getArguments()[0];
            // Set enrollment ID using reflection
            try {
                java.lang.reflect.Field field = Enrollment.class.getDeclaredField("id");
                field.setAccessible(true);
                field.set(enrollment, UUID.randomUUID());
            } catch (Exception e) {
                fail("Failed to set enrollment ID");
            }
            return enrollment;
        });

        // When
        Enrollment result = enrollmentService.enroll(studentId, courseId).get();

        // Then
        assertNotNull(result);
        assertEquals(studentId, result.getStudentId());
        assertEquals(courseId, result.getCourse().getId());
        verify(enrollmentRepository).existsByStudentIdAndCourseId(studentId, courseId);
        verify(courseRepository).findById(courseId);
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("Should return null when enrolling in non-existent course")
    void enrollNonExistentCourse() throws ExecutionException, InterruptedException {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(enrollmentRepository.existsByStudentIdAndCourseId(studentId, nonExistentId)).thenReturn(false);
        when(courseRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Enrollment result = enrollmentService.enroll(studentId, nonExistentId).get();

        // Then
        assertNull(result);
        verify(enrollmentRepository).existsByStudentIdAndCourseId(studentId, nonExistentId);
        verify(courseRepository).findById(nonExistentId);
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("Should return null when student is already enrolled")
    void enrollAlreadyEnrolled() throws ExecutionException, InterruptedException {
        // Given
        when(enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)).thenReturn(true);

        // When
        Enrollment result = enrollmentService.enroll(studentId, courseId).get();

        // Then
        assertNull(result);
        verify(enrollmentRepository).existsByStudentIdAndCourseId(studentId, courseId);
        verify(courseRepository, never()).findById(any(UUID.class));
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("Should handle exceptions in enroll method")
    void enrollException() {
        // Given
        RuntimeException testException = new RuntimeException("Test exception");
        when(enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)).thenThrow(testException);

        // When & Then
        CompletableFuture<Enrollment> future = enrollmentService.enroll(studentId, courseId);
        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertEquals(testException, exception.getCause());
    }

    @Test
    @DisplayName("Should unenroll student from course")
    void unenroll() throws ExecutionException, InterruptedException {
        // Given
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));
        when(enrollmentRepository.findByStudentIdAndCourse(studentId, testCourse))
                .thenReturn(Optional.of(testEnrollment));

        // When
        boolean result = enrollmentService.unenroll(studentId, courseId).get();

        // Then
        assertTrue(result);
        verify(courseRepository).findById(courseId);
        verify(enrollmentRepository).findByStudentIdAndCourse(studentId, testCourse);
        verify(enrollmentRepository).delete(testEnrollment);
    }

    @Test
    @DisplayName("Should return false when unenrolling from non-existent course")
    void unenrollNonExistentCourse() throws ExecutionException, InterruptedException {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(courseRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        boolean result = enrollmentService.unenroll(studentId, nonExistentId).get();

        // Then
        assertFalse(result);
        verify(courseRepository).findById(nonExistentId);
        verify(enrollmentRepository, never()).findByStudentIdAndCourse(any(UUID.class), any(Course.class));
        verify(enrollmentRepository, never()).delete(any(Enrollment.class));
    }

    @Test
    @DisplayName("Should return false when enrollment not found")
    void unenrollNotEnrolled() throws ExecutionException, InterruptedException {
        // Given
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));
        when(enrollmentRepository.findByStudentIdAndCourse(studentId, testCourse))
                .thenReturn(Optional.empty());

        // When
        boolean result = enrollmentService.unenroll(studentId, courseId).get();

        // Then
        assertFalse(result);
        verify(courseRepository).findById(courseId);
        verify(enrollmentRepository).findByStudentIdAndCourse(studentId, testCourse);
        verify(enrollmentRepository, never()).delete(any(Enrollment.class));
    }

    @Test
    @DisplayName("Should handle exceptions in unenroll method")
    void unenrollException() {
        // Given
        RuntimeException testException = new RuntimeException("Test exception");
        when(courseRepository.findById(courseId)).thenThrow(testException);

        // When & Then
        CompletableFuture<Boolean> future = enrollmentService.unenroll(studentId, courseId);
        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertEquals(testException, exception.getCause());
    }
}