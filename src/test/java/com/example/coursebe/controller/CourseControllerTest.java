package com.example.coursebe.controller;

import com.example.coursebe.common.ApiResponse;
import com.example.coursebe.dto.CourseResponse;
import com.example.coursebe.dto.EnrollmentResponse;
import com.example.coursebe.dto.CreateCourseRequest;
import com.example.coursebe.model.Course;
import com.example.coursebe.model.Enrollment;
import com.example.coursebe.model.TutorApplication;
import com.example.coursebe.service.CourseService;
import com.example.coursebe.service.EnrollmentService;
import com.example.coursebe.service.TutorApplicationService;
import com.example.coursebe.exception.UnsupportedSearchTypeException;
import com.example.coursebe.dto.CourseResponse;
import com.example.coursebe.dto.CourseEnrolledResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


import java.math.BigDecimal;
import java.security.Principal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseControllerTest {
    @Mock
    private CourseService courseService;
    @Mock
    private EnrollmentService enrollmentService;
    @Mock
    private TutorApplicationService tutorApplicationService;
    @InjectMocks
    private CourseController courseController;

    private Principal principal;
    private UUID tutorId;

    @BeforeEach
    void setUp() {
        tutorId = UUID.randomUUID();
        principal = mock(Principal.class);
        lenient().when(principal.getName()).thenReturn(tutorId.toString());
    }

    @Test
    @DisplayName("GET /courses?userId={userId} - should return all courses with pagination when no search parameters")
    void getAllCourses_noSearchParameters() {
        // Given
        UUID userId = UUID.randomUUID();
        int page = 0;
        int size = 15;
        Pageable pageable = PageRequest.of(page, size);
        List<Course> mockCourses = Arrays.asList(
                new Course("Java Course", "Learn Java", UUID.randomUUID(), new BigDecimal("99.99")),
                new Course("Python Course", "Learn Python", UUID.randomUUID(), new BigDecimal("89.99"))
        );
        Page<Course> mockCoursePage = new PageImpl<>(mockCourses, pageable, 2);
        when(courseService.getAllCourses(pageable)).thenReturn(mockCoursePage);

        // When
        ResponseEntity<ApiResponse<List<CourseResponse>>> response = courseController.getAllCourses(userId, null, null, page, size);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiResponse<List<CourseResponse>> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(200, responseBody.getCode());
        assertTrue(responseBody.isSuccess());
        assertEquals("Courses retrieved successfully.", responseBody.getMessage());
        assertNotNull(responseBody.getData());
        assertEquals(2, responseBody.getData().size());

        // Check pagination metadata
        assertNotNull(responseBody.getMetadata());
        assertEquals(2L, responseBody.getMetadata().get("totalItems"));
        assertEquals(1, responseBody.getMetadata().get("totalPages"));
        assertEquals(0, responseBody.getMetadata().get("currentPage"));
        assertEquals(15, responseBody.getMetadata().get("pageSize"));

        verify(courseService).getAllCourses(pageable);
        verify(courseService, never()).searchCourses(anyString(), anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /courses?userId={userId}&type=keyword&keyword=Java - should search courses by keyword with pagination")
    void getAllCourses_searchByKeyword() {
        // Given
        UUID userId = UUID.randomUUID();
        String type = "keyword";
        String keyword = "Java";
        int page = 0;
        int size = 15;
        Pageable pageable = PageRequest.of(page, size);

        List<Course> mockCourses = Arrays.asList(
                new Course("Java Course", "Learn Java", UUID.randomUUID(), new BigDecimal("99.99"))
        );
        Page<Course> mockCoursePage = new PageImpl<>(mockCourses, pageable, 1);

        when(courseService.searchCourses(type, keyword, pageable)).thenReturn(mockCoursePage);

        // When
        ResponseEntity<ApiResponse<List<CourseResponse>>> response = courseController.getAllCourses(userId, type, keyword, page, size);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiResponse<List<CourseResponse>> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(200, responseBody.getCode());
        assertTrue(responseBody.isSuccess());
        assertEquals("Courses retrieved successfully.", responseBody.getMessage());
        assertNotNull(responseBody.getData());
        assertEquals(1, responseBody.getData().size());

        // Check pagination metadata
        assertNotNull(responseBody.getMetadata());
        assertEquals(1L, responseBody.getMetadata().get("totalItems"));
        assertEquals(1, responseBody.getMetadata().get("totalPages"));
        assertEquals(0, responseBody.getMetadata().get("currentPage"));
        assertEquals(15, responseBody.getMetadata().get("pageSize"));

        verify(courseService).searchCourses(type, keyword, pageable);
        verify(courseService, never()).getAllCourses(any(Pageable.class));
    }

    @Test
    @DisplayName("GET /courses?userId={userId}&type=name&keyword=Python - should search courses by name with pagination")
    void getAllCourses_searchByName() {
        // Given
        UUID userId = UUID.randomUUID();
        String type = "name";
        String keyword = "Python";
        int page = 0;
        int size = 15;
        Pageable pageable = PageRequest.of(page, size);

        List<Course> mockCourses = Arrays.asList(
                new Course("Python Course", "Learn Python", UUID.randomUUID(), new BigDecimal("89.99"))
        );
        Page<Course> mockCoursePage = new PageImpl<>(mockCourses, pageable, 1);

        when(courseService.searchCourses(type, keyword, pageable)).thenReturn(mockCoursePage);

        // When
        ResponseEntity<ApiResponse<List<CourseResponse>>> response = courseController.getAllCourses(userId, type, keyword, page, size);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiResponse<List<CourseResponse>> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(200, responseBody.getCode());
        assertTrue(responseBody.isSuccess());
        assertNotNull(responseBody.getData());
        assertEquals(1, responseBody.getData().size());

        // Check pagination metadata
        assertNotNull(responseBody.getMetadata());
        assertEquals(1L, responseBody.getMetadata().get("totalItems"));
        assertEquals(1, responseBody.getMetadata().get("totalPages"));
        assertEquals(0, responseBody.getMetadata().get("currentPage"));
        assertEquals(15, responseBody.getMetadata().get("pageSize"));

        verify(courseService).searchCourses(type, keyword, pageable);
        verify(courseService, never()).getAllCourses(any(Pageable.class));
    }

    @Test
    @DisplayName("GET /courses?userId={userId}&type=keyword&keyword=Nonexistent - should handle empty search results with pagination")
    void getAllCourses_emptySearchResults() {
        // Given
        UUID userId = UUID.randomUUID();
        String type = "keyword";
        String keyword = "Nonexistent";
        int page = 0;
        int size = 15;
        Pageable pageable = PageRequest.of(page, size);

        Page<Course> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(courseService.searchCourses(type, keyword, pageable)).thenReturn(emptyPage);

        // When
        ResponseEntity<ApiResponse<List<CourseResponse>>> response = courseController.getAllCourses(userId, type, keyword, page, size);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiResponse<List<CourseResponse>> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(200, responseBody.getCode());
        assertTrue(responseBody.isSuccess());
        assertTrue(responseBody.getData().isEmpty());

        // Check pagination metadata
        assertNotNull(responseBody.getMetadata());
        assertEquals(0L, responseBody.getMetadata().get("totalItems"));
        assertEquals(0, responseBody.getMetadata().get("totalPages"));
        assertEquals(0, responseBody.getMetadata().get("currentPage"));
        assertEquals(15, responseBody.getMetadata().get("pageSize"));

        verify(courseService).searchCourses(type, keyword, pageable);
        verify(courseService, never()).getAllCourses(any(Pageable.class));
    }

    @Test
    @DisplayName("GET /courses?userId={userId}&type=keyword - should handle missing keyword with pagination")
    void getAllCourses_missingKeyword() {
        // Given
        UUID userId = UUID.randomUUID();
        String type = "keyword";
        String keyword = null;
        int page = 0;
        int size = 15;
        Pageable pageable = PageRequest.of(page, size);

        List<Course> mockCourses = Arrays.asList(
                new Course("Java Course", "Learn Java", UUID.randomUUID(), new BigDecimal("99.99")),
                new Course("Python Course", "Learn Python", UUID.randomUUID(), new BigDecimal("89.99"))
        );
        Page<Course> mockCoursePage = new PageImpl<>(mockCourses, pageable, 2);

        when(courseService.getAllCourses(pageable)).thenReturn(mockCoursePage);

        // When
        ResponseEntity<ApiResponse<List<CourseResponse>>> response = courseController.getAllCourses(userId, type, keyword, page, size);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiResponse<List<CourseResponse>> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(200, responseBody.getCode());
        assertTrue(responseBody.isSuccess());
        assertEquals(2, responseBody.getData().size());

        // Check pagination metadata
        assertNotNull(responseBody.getMetadata());
        assertEquals(2L, responseBody.getMetadata().get("totalItems"));
        assertEquals(1, responseBody.getMetadata().get("totalPages"));
        assertEquals(0, responseBody.getMetadata().get("currentPage"));
        assertEquals(15, responseBody.getMetadata().get("pageSize"));

        verify(courseService).getAllCourses(pageable);
        verify(courseService, never()).searchCourses(anyString(), anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /courses?userId={userId} - should use custom pagination parameters")
    void getAllCourses_withCustomPagination() {
        // Given
        UUID userId = UUID.randomUUID();
        int page = 2;
        int size = 5;
        Pageable pageable = PageRequest.of(page, size);

        List<Course> mockCourses = Arrays.asList(
                new Course("Advanced Course", "Advanced topics", UUID.randomUUID(), new BigDecimal("149.99"))
        );
        Page<Course> mockCoursePage = new PageImpl<>(mockCourses, pageable, 11); // 11 total elements

        when(courseService.getAllCourses(pageable)).thenReturn(mockCoursePage);

        // When
        ResponseEntity<ApiResponse<List<CourseResponse>>> response = courseController.getAllCourses(userId, null, null, page, size);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiResponse<List<CourseResponse>> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(1, responseBody.getData().size());

        // Check pagination metadata
        assertNotNull(responseBody.getMetadata());
        assertEquals(11L, responseBody.getMetadata().get("totalItems"));
        assertEquals(3, responseBody.getMetadata().get("totalPages"));
        assertEquals(2, responseBody.getMetadata().get("currentPage"));
        assertEquals(5, responseBody.getMetadata().get("pageSize"));

        verify(courseService).getAllCourses(pageable);
    }

    @Test
    @DisplayName("GET /courses/my-courses?userId={userId} - should return enrolled courses for a user")
    void getMyAllCourses_success() {
        // Given
        UUID userId = UUID.randomUUID();
        int page = 0;
        int size = 15;
        Pageable pageable = PageRequest.of(page, size);

        List<Course> mockCourses = Arrays.asList(
                new Course("Java Course", "Learn Java", UUID.randomUUID(), new BigDecimal("99.99")),
                new Course("Python Course", "Learn Python", UUID.randomUUID(), new BigDecimal("89.99"))
        );
        Page<Course> mockCoursePage = new PageImpl<>(mockCourses, pageable, 2);

        when(courseService.getEnrolledCourses(userId, pageable)).thenReturn(mockCoursePage);

        // When
        ResponseEntity<ApiResponse<List<CourseEnrolledResponse>>> response =
                courseController.getMyAllCourses(userId, null, null, page, size);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiResponse<List<CourseEnrolledResponse>> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(200, responseBody.getCode());
        assertTrue(responseBody.isSuccess());
        assertEquals("User enrolled courses retrieved successfully.", responseBody.getMessage());
        assertNotNull(responseBody.getData());
        assertEquals(2, responseBody.getData().size());

        // Check course data
        assertEquals("Java Course", responseBody.getData().get(0).getName());
        assertEquals("Python Course", responseBody.getData().get(1).getName());

        // Verify price field exists in response
        assertNotNull(responseBody.getData().get(0).getPrice());
        assertEquals(new BigDecimal("99.99"), responseBody.getData().get(0).getPrice());

        // Check pagination metadata
        assertNotNull(responseBody.getMetadata());
        assertEquals(2L, responseBody.getMetadata().get("totalItems"));
        assertEquals(1, responseBody.getMetadata().get("totalPages"));
        assertEquals(0, responseBody.getMetadata().get("currentPage"));
        assertEquals(15, responseBody.getMetadata().get("pageSize"));

        verify(courseService).getEnrolledCourses(userId, pageable);
        verify(courseService, never()).searchEnrolledCourses(any(), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("GET /courses/my-courses?userId={userId}&type=name&keyword=Java - should search enrolled courses by name")
    void getMyAllCourses_searchByName() {
        // Given
        UUID userId = UUID.randomUUID();
        String type = "name";
        String keyword = "Java";
        int page = 0;
        int size = 15;
        Pageable pageable = PageRequest.of(page, size);

        List<Course> mockCourses = Arrays.asList(
                new Course("Java Course", "Learn Java", UUID.randomUUID(), new BigDecimal("99.99"))
        );
        Page<Course> mockCoursePage = new PageImpl<>(mockCourses, pageable, 1);

        when(courseService.searchEnrolledCourses(userId, type, keyword, pageable)).thenReturn(mockCoursePage);

        // When
        ResponseEntity<ApiResponse<List<CourseEnrolledResponse>>> response =
                courseController.getMyAllCourses(userId, type, keyword, page, size);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiResponse<List<CourseEnrolledResponse>> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(200, responseBody.getCode());
        assertTrue(responseBody.isSuccess());
        assertEquals("User enrolled courses retrieved successfully.", responseBody.getMessage());
        assertNotNull(responseBody.getData());
        assertEquals(1, responseBody.getData().size());
        assertEquals("Java Course", responseBody.getData().get(0).getName());

        // Check pagination metadata
        assertNotNull(responseBody.getMetadata());
        assertEquals(1L, responseBody.getMetadata().get("totalItems"));
        assertEquals(1, responseBody.getMetadata().get("totalPages"));
        assertEquals(0, responseBody.getMetadata().get("currentPage"));
        assertEquals(15, responseBody.getMetadata().get("pageSize"));

        verify(courseService).searchEnrolledCourses(userId, type, keyword, pageable);
        verify(courseService, never()).getEnrolledCourses(any(), any());
    }

    @Test
    @DisplayName("GET /courses/{id}?userId={userId} - should return course with enrollment status")
    void getCourseById_withUserId() {
        // Given
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Course mockCourse = new Course("Java Course", "Learn Java", UUID.randomUUID(), new BigDecimal("99.99"));

        // Use reflection to set ID
        try {
            java.lang.reflect.Field field = Course.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(mockCourse, courseId);
        } catch (Exception e) {
            fail("Failed to set course ID");
        }

        when(courseService.getCourseById(courseId)).thenReturn(Optional.of(mockCourse));
        when(enrollmentService.isEnrolled(userId, courseId)).thenReturn(true);

        // When
        ResponseEntity<ApiResponse<CourseResponse>> response = courseController.getCourseById(courseId, userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiResponse<CourseResponse> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(200, responseBody.getCode());
        assertTrue(responseBody.isSuccess());
        assertEquals("Course retrieved successfully.", responseBody.getMessage());

        CourseResponse courseResponse = responseBody.getData();
        assertNotNull(courseResponse);
        assertEquals(courseId, courseResponse.getId());
        assertEquals("Java Course", courseResponse.getName());
        assertEquals("Learn Java", courseResponse.getDescription());
        assertEquals(new BigDecimal("99.99"), courseResponse.getPrice());
        assertTrue(courseResponse.isEnrolled());

        verify(courseService).getCourseById(courseId);
        verify(enrollmentService).isEnrolled(userId, courseId);
    }

    @Test
    @DisplayName("GET /courses/my-courses/{id}?userId={userId} - should return enrolled course details")
    void getMyCourseById_whenEnrolled() {
        // Given
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Course mockCourse = new Course("Java Course", "Learn Java", UUID.randomUUID(), new BigDecimal("99.99"));

        // Use reflection to set ID
        try {
            java.lang.reflect.Field field = Course.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(mockCourse, courseId);
        } catch (Exception e) {
            fail("Failed to set course ID");
        }

        when(courseService.getCourseById(courseId)).thenReturn(Optional.of(mockCourse));
        when(enrollmentService.isEnrolled(userId, courseId)).thenReturn(true);

        // When
        ResponseEntity<ApiResponse<CourseEnrolledResponse>> response = courseController.getMyCourseById(courseId, userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiResponse<CourseEnrolledResponse> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(200, responseBody.getCode());
        assertTrue(responseBody.isSuccess());
        assertEquals("Enrolled course retrieved successfully.", responseBody.getMessage());

        CourseEnrolledResponse enrolledResponse = responseBody.getData();
        assertNotNull(enrolledResponse);
        assertEquals(courseId, enrolledResponse.getId());
        assertEquals("Java Course", enrolledResponse.getName());
        assertEquals("Learn Java", enrolledResponse.getDescription());
        assertEquals(new BigDecimal("99.99"), enrolledResponse.getPrice());

        verify(courseService).getCourseById(courseId);
        verify(enrollmentService).isEnrolled(userId, courseId);
    }

    @Test
    @DisplayName("GET /courses/my-courses/{id}?userId={userId} - should return forbidden when not enrolled")
    void getMyCourseById_whenNotEnrolled() {
        // Given
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Course mockCourse = new Course("Java Course", "Learn Java", UUID.randomUUID(), new BigDecimal("99.99"));

        // Use reflection to set ID
        try {
            java.lang.reflect.Field field = Course.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(mockCourse, courseId);
        } catch (Exception e) {
            fail("Failed to set course ID");
        }

        when(courseService.getCourseById(courseId)).thenReturn(Optional.of(mockCourse));
        when(enrollmentService.isEnrolled(userId, courseId)).thenReturn(false);

        // When
        ResponseEntity<ApiResponse<CourseEnrolledResponse>> response = courseController.getMyCourseById(courseId, userId);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        ApiResponse<CourseEnrolledResponse> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(403, responseBody.getCode());
        assertFalse(responseBody.isSuccess());
        assertEquals("You are not enrolled in this course.", responseBody.getMessage());
        assertNull(responseBody.getData());

        verify(courseService).getCourseById(courseId);
        verify(enrollmentService).isEnrolled(userId, courseId);
    }

    @Test
    @DisplayName("POST /courses/{id}/enroll?userId={userId} - should enroll student in course successfully")
    void enrollCourse_success() throws ExecutionException, InterruptedException {
        // Given
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Course mockCourse = new Course("Java Course", "Learn Java", UUID.randomUUID(), new BigDecimal("99.99"));

        // Set course ID using reflection
        try {
            java.lang.reflect.Field field = Course.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(mockCourse, courseId);
        } catch (Exception e) {
            fail("Failed to set course ID");
        }

        // Now create enrollment with the Course object
        Enrollment mockEnrollment = new Enrollment(userId, mockCourse);

        when(courseService.getCourseById(courseId)).thenReturn(Optional.of(mockCourse));
        when(enrollmentService.enroll(userId, courseId)).thenReturn(
                CompletableFuture.completedFuture(mockEnrollment)
        );

        // When
        CompletableFuture<ResponseEntity<ApiResponse<EnrollmentResponse>>> futureResponse =
                courseController.enrollCourse(courseId, userId);
        ResponseEntity<ApiResponse<EnrollmentResponse>> response = futureResponse.get();  // Wait for the async result

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiResponse<EnrollmentResponse> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(200, responseBody.getCode());
        assertTrue(responseBody.isSuccess());
        assertEquals("Successfully enrolled in the course.", responseBody.getMessage());
        assertNotNull(responseBody.getData());
        assertEquals(courseId, responseBody.getData().getCourseId());

        verify(courseService).getCourseById(courseId);
        verify(enrollmentService).enroll(userId, courseId);
    }

    @Test
    @DisplayName("POST /courses/{id}/enroll?userId={userId} - should return 404 when course not found")
    void enrollCourse_courseNotFound() throws ExecutionException, InterruptedException {
        // Given
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(courseService.getCourseById(courseId)).thenReturn(Optional.empty());

        // When
        CompletableFuture<ResponseEntity<ApiResponse<EnrollmentResponse>>> futureResponse =
                courseController.enrollCourse(courseId, userId);
        ResponseEntity<ApiResponse<EnrollmentResponse>> response = futureResponse.get();  // Wait for the async result

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        ApiResponse<EnrollmentResponse> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(404, responseBody.getCode());
        assertFalse(responseBody.isSuccess());
        assertEquals("Course not found.", responseBody.getMessage());
        assertNull(responseBody.getData());

        verify(courseService).getCourseById(courseId);
        verify(enrollmentService, never()).enroll(any(UUID.class), any(UUID.class));
    }

    @Test
    @DisplayName("POST /courses/{id}/enroll?userId={userId} - should return bad request when enrollment fails")
    void enrollCourse_enrollmentFails() throws ExecutionException, InterruptedException {
        // Given
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Course mockCourse = new Course("Java Course", "Learn Java", UUID.randomUUID(), new BigDecimal("99.99"));

        when(courseService.getCourseById(courseId)).thenReturn(Optional.of(mockCourse));
        when(enrollmentService.enroll(userId, courseId)).thenReturn(
                CompletableFuture.completedFuture(null)
        );

        // When
        CompletableFuture<ResponseEntity<ApiResponse<EnrollmentResponse>>> futureResponse =
                courseController.enrollCourse(courseId, userId);
        ResponseEntity<ApiResponse<EnrollmentResponse>> response = futureResponse.get();  // Wait for the async result

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ApiResponse<EnrollmentResponse> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(400, responseBody.getCode());
        assertFalse(responseBody.isSuccess());
        assertEquals("Failed to enroll in the course.", responseBody.getMessage());
        assertNull(responseBody.getData());

        verify(courseService).getCourseById(courseId);
        verify(enrollmentService).enroll(userId, courseId);
    }

    @Test
    @DisplayName("POST /courses/{id}/enroll?userId={userId} - should handle exceptions properly")
    void enrollCourse_handlesException() throws ExecutionException, InterruptedException {
        // Given
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Course mockCourse = new Course("Java Course", "Learn Java", UUID.randomUUID(), new BigDecimal("99.99"));
        RuntimeException testException = new RuntimeException("Test exception");

        when(courseService.getCourseById(courseId)).thenReturn(Optional.of(mockCourse));
        when(enrollmentService.enroll(userId, courseId)).thenReturn(
                CompletableFuture.failedFuture(testException)
        );

        // When
        CompletableFuture<ResponseEntity<ApiResponse<EnrollmentResponse>>> futureResponse =
                courseController.enrollCourse(courseId, userId);
        ResponseEntity<ApiResponse<EnrollmentResponse>> response = futureResponse.get();  // Wait for the async result

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        ApiResponse<EnrollmentResponse> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(500, responseBody.getCode());
        assertFalse(responseBody.isSuccess());
        assertEquals("Error enrolling in course: Test exception", responseBody.getMessage());
        assertNull(responseBody.getData());

        verify(courseService).getCourseById(courseId);
        verify(enrollmentService).enroll(userId, courseId);
    }

    @Test
    @DisplayName("DELETE /courses/{id}/unenroll?userId={userId} - should unenroll student from course successfully")
    void unenrollCourse_success() throws ExecutionException, InterruptedException {
        // Given
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(enrollmentService.unenroll(userId, courseId)).thenReturn(
                CompletableFuture.completedFuture(true)
        );

        // When
        CompletableFuture<ResponseEntity<ApiResponse<Void>>> futureResponse =
                courseController.unenrollCourse(courseId, userId);
        ResponseEntity<ApiResponse<Void>> response = futureResponse.get();  // Wait for the async result

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiResponse<Void> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(200, responseBody.getCode());
        assertTrue(responseBody.isSuccess());
        assertEquals("Successfully unenrolled from the course.", responseBody.getMessage());
        assertNull(responseBody.getData());

        verify(enrollmentService).unenroll(userId, courseId);
    }

    @Test
    @DisplayName("DELETE /courses/{id}/unenroll?userId={userId} - should return bad request when unenrollment fails")
    void unenrollCourse_unenrollmentFails() throws ExecutionException, InterruptedException {
        // Given
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(enrollmentService.unenroll(userId, courseId)).thenReturn(
                CompletableFuture.completedFuture(false)
        );

        // When
        CompletableFuture<ResponseEntity<ApiResponse<Void>>> futureResponse =
                courseController.unenrollCourse(courseId, userId);
        ResponseEntity<ApiResponse<Void>> response = futureResponse.get();  // Wait for the async result

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ApiResponse<Void> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(400, responseBody.getCode());
        assertFalse(responseBody.isSuccess());
        assertEquals("Failed to unenroll from the course.", responseBody.getMessage());
        assertNull(responseBody.getData());

        verify(enrollmentService).unenroll(userId, courseId);
    }

    @Test
    @DisplayName("DELETE /courses/{id}/unenroll?userId={userId} - should handle exceptions properly")
    void unenrollCourse_handlesException() throws ExecutionException, InterruptedException {
        // Given
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        RuntimeException testException = new RuntimeException("Test exception");

        when(enrollmentService.unenroll(userId, courseId)).thenReturn(
                CompletableFuture.failedFuture(testException)
        );

        // When
        CompletableFuture<ResponseEntity<ApiResponse<Void>>> futureResponse =
                courseController.unenrollCourse(courseId, userId);
        ResponseEntity<ApiResponse<Void>> response = futureResponse.get();  // Wait for the async result

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        ApiResponse<Void> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(500, responseBody.getCode());
        assertFalse(responseBody.isSuccess());
        assertEquals("Error unenrolling from course: Test exception", responseBody.getMessage());
        assertNull(responseBody.getData());

        verify(enrollmentService).unenroll(userId, courseId);
    }

    @Test
    @DisplayName("POST /courses - success (ACCEPTED tutor)")
    void createCourse_success() {
        CreateCourseRequest req = new CreateCourseRequest();
        req.name = "Test Course";
        req.description = "Desc";
        req.price = new BigDecimal("100000");
        TutorApplication app = new TutorApplication(tutorId);
        app.setStatus(TutorApplication.Status.ACCEPTED);
        when(tutorApplicationService.getMostRecentApplicationByStudentId(tutorId)).thenReturn(Optional.of(app));
        Course course = new Course(req.name, req.description, tutorId, req.price);
        when(courseService.createCourse(req.name, req.description, tutorId, req.price)).thenReturn(course);

        ResponseEntity<?> response = courseController.createCourse(req, principal);
        assertEquals(201, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("Course created successfully"));
        verify(courseService).createCourse(req.name, req.description, tutorId, req.price);
    }

    @Test
    @DisplayName("POST /courses - forbidden (not ACCEPTED)")
    void createCourse_forbidden() {
        CreateCourseRequest req = new CreateCourseRequest();
        req.name = "Test Course";
        req.description = "Desc";
        req.price = new BigDecimal("100000");
        TutorApplication app = new TutorApplication(tutorId);
        app.setStatus(TutorApplication.Status.PENDING);
        when(tutorApplicationService.getMostRecentApplicationByStudentId(tutorId)).thenReturn(Optional.of(app));

        ResponseEntity<?> response = courseController.createCourse(req, principal);
        assertEquals(403, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("must be ACCEPTED"));
        verify(courseService, never()).createCourse(any(), any(), any(), any());
    }

    @Test
    @DisplayName("POST /courses - forbidden (no tutor application)")
    void createCourse_noTutorApplication() {
        CreateCourseRequest req = new CreateCourseRequest();
        req.name = "Test Course";
        req.description = "Desc";
        req.price = new BigDecimal("100000");
        when(tutorApplicationService.getMostRecentApplicationByStudentId(tutorId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = courseController.createCourse(req, principal);
        assertEquals(403, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("must be ACCEPTED"));
        verify(courseService, never()).createCourse(any(), any(), any(), any());
    }

    @Test
    @DisplayName("GET /courses/mine - success (ACCEPTED tutor)")
    void getMyCourses_success() {
        TutorApplication app = new TutorApplication(tutorId);
        app.setStatus(TutorApplication.Status.ACCEPTED);
        when(tutorApplicationService.getMostRecentApplicationByStudentId(tutorId)).thenReturn(Optional.of(app));
        List<Course> courses = List.of(
            new Course("Course 1", "Desc 1", tutorId, new BigDecimal("10000")),
            new Course("Course 2", "Desc 2", tutorId, new BigDecimal("20000"))
        );
        when(courseService.getCoursesByTutorId(tutorId)).thenReturn(courses);

        ResponseEntity<?> response = courseController.getMyCourses(principal);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof java.util.Map);
        var respMap = (java.util.Map<?,?>) response.getBody();
        assertTrue((Boolean) respMap.get("success"));
        assertTrue(respMap.get("courses") instanceof List);
        List<?> respCourses = (List<?>) respMap.get("courses");
        assertEquals(2, respCourses.size());
        // Cek nama course baik jika elemen Course maupun Map hasil serialisasi
        assertTrue(respCourses.stream().anyMatch(c -> {
            if (c instanceof java.util.Map<?,?> map) {
                return "Course 1".equals(map.get("name"));
            } else if (c instanceof Course courseObj) {
                return "Course 1".equals(courseObj.getName());
            }
            return false;
        }));
        verify(courseService).getCoursesByTutorId(tutorId);
    }

    @Test
    @DisplayName("GET /courses/mine - forbidden (not ACCEPTED)")
    void getMyCourses_forbidden() {
        TutorApplication app = new TutorApplication(tutorId);
        app.setStatus(TutorApplication.Status.PENDING);
        when(tutorApplicationService.getMostRecentApplicationByStudentId(tutorId)).thenReturn(Optional.of(app));

        ResponseEntity<?> response = courseController.getMyCourses(principal);
        assertEquals(403, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("must be ACCEPTED"));
        verify(courseService, never()).getCoursesByTutorId(any());
    }

    @Test
    @DisplayName("GET /courses/mine - forbidden (no tutor application)")
    void getMyCourses_noTutorApplication() {
        when(tutorApplicationService.getMostRecentApplicationByStudentId(tutorId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = courseController.getMyCourses(principal);
        assertEquals(403, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("must be ACCEPTED"));
        verify(courseService, never()).getCoursesByTutorId(any());
    }

    @Test
    @DisplayName("DELETE /courses/{courseId} - success (owner & ACCEPTED)")
    void deleteCourse_success() {
        UUID courseId = UUID.randomUUID();
        TutorApplication app = new TutorApplication(tutorId);
        app.setStatus(TutorApplication.Status.ACCEPTED);
        Course course = new Course("Course 1", "Desc", tutorId, new BigDecimal("10000"));
        when(tutorApplicationService.getMostRecentApplicationByStudentId(tutorId)).thenReturn(Optional.of(app));
        when(courseService.getCourseById(courseId)).thenReturn(Optional.of(course));
        when(courseService.deleteCourse(courseId)).thenReturn(true);

        ResponseEntity<?> response = courseController.deleteCourse(courseId, principal);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("deleted successfully"));
        verify(courseService).deleteCourse(courseId);
    }

    @Test
    @DisplayName("DELETE /courses/{courseId} - forbidden (not ACCEPTED)")
    void deleteCourse_forbidden_notAccepted() {
        UUID courseId = UUID.randomUUID();
        TutorApplication app = new TutorApplication(tutorId);
        app.setStatus(TutorApplication.Status.PENDING);
        when(tutorApplicationService.getMostRecentApplicationByStudentId(tutorId)).thenReturn(Optional.of(app));

        ResponseEntity<?> response = courseController.deleteCourse(courseId, principal);
        assertEquals(403, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("must be ACCEPTED"));
        verify(courseService, never()).deleteCourse(any());
    }

    @Test
    @DisplayName("DELETE /courses/{courseId} - forbidden (not owner)")
    void deleteCourse_forbidden_notOwner() {
        UUID courseId = UUID.randomUUID();
        TutorApplication app = new TutorApplication(tutorId);
        app.setStatus(TutorApplication.Status.ACCEPTED);
        Course course = new Course("Course 1", "Desc", UUID.randomUUID(), new BigDecimal("10000")); // different tutorId
        when(tutorApplicationService.getMostRecentApplicationByStudentId(tutorId)).thenReturn(Optional.of(app));
        when(courseService.getCourseById(courseId)).thenReturn(Optional.of(course));

        ResponseEntity<?> response = courseController.deleteCourse(courseId, principal);
        assertEquals(403, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("Only the owner can delete"));
        verify(courseService, never()).deleteCourse(any());
    }

    @Test
    @DisplayName("DELETE /courses/{courseId} - not found")
    void deleteCourse_notFound() {
        UUID courseId = UUID.randomUUID();
        TutorApplication app = new TutorApplication(tutorId);
        app.setStatus(TutorApplication.Status.ACCEPTED);
        when(tutorApplicationService.getMostRecentApplicationByStudentId(tutorId)).thenReturn(Optional.of(app));
        when(courseService.getCourseById(courseId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = courseController.deleteCourse(courseId, principal);
        assertEquals(403, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("Only the owner can delete"));
        verify(courseService, never()).deleteCourse(any());
    }

    @Test
    @DisplayName("GET /courses/{courseId}/students - success (owner & ACCEPTED)")
    void getEnrolledStudents_success() {
        UUID courseId = UUID.randomUUID();
        TutorApplication app = new TutorApplication(tutorId);
        app.setStatus(TutorApplication.Status.ACCEPTED);
        Course course = new Course("Course 1", "Desc", tutorId, new BigDecimal("10000"));
        when(tutorApplicationService.getMostRecentApplicationByStudentId(tutorId)).thenReturn(Optional.of(app));
        when(courseService.getCourseById(courseId)).thenReturn(Optional.of(course));
        var students = List.of("student1@example.com", "student2@example.com");
        when(courseService.getEnrolledStudents(courseId)).thenReturn(students);

        ResponseEntity<?> response = courseController.getEnrolledStudents(courseId, principal);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("students"));
        assertTrue(response.getBody().toString().contains("student1@example.com"));
        verify(courseService).getEnrolledStudents(courseId);
    }

    @Test
    @DisplayName("GET /courses/{courseId}/students - forbidden (not ACCEPTED)")
    void getEnrolledStudents_forbidden_notAccepted() {
        UUID courseId = UUID.randomUUID();
        TutorApplication app = new TutorApplication(tutorId);
        app.setStatus(TutorApplication.Status.PENDING);
        when(tutorApplicationService.getMostRecentApplicationByStudentId(tutorId)).thenReturn(Optional.of(app));

        ResponseEntity<?> response = courseController.getEnrolledStudents(courseId, principal);
        assertEquals(403, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("must be ACCEPTED"));
        verify(courseService, never()).getEnrolledStudents(any());
    }

    @Test
    @DisplayName("GET /courses/{courseId}/students - forbidden (not owner)")
    void getEnrolledStudents_forbidden_notOwner() {
        UUID courseId = UUID.randomUUID();
        TutorApplication app = new TutorApplication(tutorId);
        app.setStatus(TutorApplication.Status.ACCEPTED);
        Course course = new Course("Course 1", "Desc", UUID.randomUUID(), new BigDecimal("10000")); // different tutorId
        when(tutorApplicationService.getMostRecentApplicationByStudentId(tutorId)).thenReturn(Optional.of(app));
        when(courseService.getCourseById(courseId)).thenReturn(Optional.of(course));

        ResponseEntity<?> response = courseController.getEnrolledStudents(courseId, principal);
        assertEquals(403, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("Only the owner can view"));
        verify(courseService, never()).getEnrolledStudents(any());
    }

    @Test
    @DisplayName("GET /courses/{courseId}/students - not found")
    void getEnrolledStudents_notFound() {
        UUID courseId = UUID.randomUUID();
        TutorApplication app = new TutorApplication(tutorId);
        app.setStatus(TutorApplication.Status.ACCEPTED);
        when(tutorApplicationService.getMostRecentApplicationByStudentId(tutorId)).thenReturn(Optional.of(app));
        when(courseService.getCourseById(courseId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = courseController.getEnrolledStudents(courseId, principal);
        assertEquals(403, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("Only the owner can view"));
        verify(courseService, never()).getEnrolledStudents(any());
    }
}
