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
    @DisplayName("GET /courses - should return all courses with pagination when no search parameters")
    void getAllCourses_noSearchParameters() {
        // Given
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
        ResponseEntity<ApiResponse<List<CourseResponse>>> response = courseController.getAllCourses(null, null, page, size);

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
    @DisplayName("GET /courses?keyword=Java - should search courses by keyword with pagination")
    void getAllCourses_searchByKeyword() {
        // Given
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
        ResponseEntity<ApiResponse<List<CourseResponse>>> response = courseController.getAllCourses(type, keyword, page, size);

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
    @DisplayName("GET /courses - should search courses by name with pagination")
    void getAllCourses_searchByName() {
        // Given
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
        ResponseEntity<ApiResponse<List<CourseResponse>>> response = courseController.getAllCourses(type, keyword, page, size);

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
    @DisplayName("GET /courses - should handle empty search results with pagination")
    void getAllCourses_emptySearchResults() {
        // Given
        String type = "keyword";
        String keyword = "Nonexistent";
        int page = 0;
        int size = 15;
        Pageable pageable = PageRequest.of(page, size);

        Page<Course> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(courseService.searchCourses(type, keyword, pageable)).thenReturn(emptyPage);

        // When
        ResponseEntity<ApiResponse<List<CourseResponse>>> response = courseController.getAllCourses(type, keyword, page, size);

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
    @DisplayName("GET /courses - should handle missing keyword with pagination")
    void getAllCourses_missingKeyword() {
        // Given
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
        ResponseEntity<ApiResponse<List<CourseResponse>>> response = courseController.getAllCourses(type, keyword, page, size);

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
    @DisplayName("GET /courses - should use custom pagination parameters")
    void getAllCourses_withCustomPagination() {
        // Given
        int page = 2;
        int size = 5;
        Pageable pageable = PageRequest.of(page, size);

        List<Course> mockCourses = Arrays.asList(
                new Course("Advanced Course", "Advanced topics", UUID.randomUUID(), new BigDecimal("149.99"))
        );
        Page<Course> mockCoursePage = new PageImpl<>(mockCourses, pageable, 11); // 11 total elements

        when(courseService.getAllCourses(pageable)).thenReturn(mockCoursePage);

        // When
        ResponseEntity<ApiResponse<List<CourseResponse>>> response = courseController.getAllCourses(null, null, page, size);

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
    @DisplayName("GET /courses/{id} - should return course by ID")
    void getCourseById_successful() {
        // Given
        UUID courseId = UUID.randomUUID();
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

        // When
        ResponseEntity<ApiResponse<CourseResponse>> response = courseController.getCourseById(courseId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiResponse<CourseResponse> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(200, responseBody.getCode());
        assertTrue(responseBody.isSuccess());
        assertEquals("Course retrieved successfully.", responseBody.getMessage());
        assertNotNull(responseBody.getData());
        assertEquals(courseId, responseBody.getData().getId());

        verify(courseService).getCourseById(courseId);
    }

    @Test
    @DisplayName("GET /courses/{id} - should return 404 when course not found")
    void getCourseById_notFound() {
        // Given
        UUID courseId = UUID.randomUUID();
        when(courseService.getCourseById(courseId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<ApiResponse<CourseResponse>> response = courseController.getCourseById(courseId);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        ApiResponse<CourseResponse> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(404, responseBody.getCode());
        assertFalse(responseBody.isSuccess());
        assertEquals("Course not found.", responseBody.getMessage());
        assertNull(responseBody.getData());

        verify(courseService).getCourseById(courseId);
    }

    @Test
    @DisplayName("POST /courses/{id}/enroll - should enroll student in course successfully")
    void enrollCourse_success() throws ExecutionException, InterruptedException {
        // Given
        UUID courseId = UUID.randomUUID();
        Course mockCourse = new Course("Java Course", "Learn Java", UUID.randomUUID(), new BigDecimal("99.99"));
        Enrollment mockEnrollment = new Enrollment(tutorId, mockCourse);

        when(courseService.getCourseById(courseId)).thenReturn(Optional.of(mockCourse));
        when(enrollmentService.enroll(tutorId, courseId)).thenReturn(
                CompletableFuture.completedFuture(mockEnrollment)
        );

        // When
        CompletableFuture<ResponseEntity<ApiResponse<EnrollmentResponse>>> futureResponse =
                courseController.enrollCourse(courseId, principal);
        ResponseEntity<ApiResponse<EnrollmentResponse>> response = futureResponse.get();  // Wait for the async result

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiResponse<EnrollmentResponse> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(200, responseBody.getCode());
        assertTrue(responseBody.isSuccess());
        assertEquals("Successfully enrolled in the course.", responseBody.getMessage());
        assertNotNull(responseBody.getData());

        verify(courseService).getCourseById(courseId);
        verify(enrollmentService).enroll(tutorId, courseId);
    }

    @Test
    @DisplayName("POST /courses/{id}/enroll - should return 404 when course not found")
    void enrollCourse_courseNotFound() throws ExecutionException, InterruptedException {
        // Given
        UUID courseId = UUID.randomUUID();
        when(courseService.getCourseById(courseId)).thenReturn(Optional.empty());

        // When
        CompletableFuture<ResponseEntity<ApiResponse<EnrollmentResponse>>> futureResponse =
                courseController.enrollCourse(courseId, principal);
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
    @DisplayName("POST /courses/{id}/enroll - should return bad request when enrollment fails")
    void enrollCourse_enrollmentFails() throws ExecutionException, InterruptedException {
        // Given
        UUID courseId = UUID.randomUUID();
        Course mockCourse = new Course("Java Course", "Learn Java", UUID.randomUUID(), new BigDecimal("99.99"));

        when(courseService.getCourseById(courseId)).thenReturn(Optional.of(mockCourse));
        when(enrollmentService.enroll(tutorId, courseId)).thenReturn(
                CompletableFuture.completedFuture(null)
        );

        // When
        CompletableFuture<ResponseEntity<ApiResponse<EnrollmentResponse>>> futureResponse =
                courseController.enrollCourse(courseId, principal);
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
        verify(enrollmentService).enroll(tutorId, courseId);
    }

    @Test
    @DisplayName("POST /courses/{id}/enroll - should handle exceptions properly")
    void enrollCourse_handlesException() throws ExecutionException, InterruptedException {
        // Given
        UUID courseId = UUID.randomUUID();
        Course mockCourse = new Course("Java Course", "Learn Java", UUID.randomUUID(), new BigDecimal("99.99"));
        RuntimeException testException = new RuntimeException("Test exception");

        when(courseService.getCourseById(courseId)).thenReturn(Optional.of(mockCourse));
        when(enrollmentService.enroll(tutorId, courseId)).thenReturn(
                CompletableFuture.failedFuture(testException)
        );

        // When
        CompletableFuture<ResponseEntity<ApiResponse<EnrollmentResponse>>> futureResponse =
                courseController.enrollCourse(courseId, principal);
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
        verify(enrollmentService).enroll(tutorId, courseId);
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
        assertEquals(201, response.getStatusCode().value());
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
        assertEquals(403, response.getStatusCode().value());
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
        assertEquals(403, response.getStatusCode().value());
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
        assertEquals(200, response.getStatusCode().value());
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
        assertEquals(403, response.getStatusCode().value());
        assertTrue(response.getBody().toString().contains("must be ACCEPTED"));
        verify(courseService, never()).getCoursesByTutorId(any());
    }

    @Test
    @DisplayName("GET /courses/mine - forbidden (no tutor application)")
    void getMyCourses_noTutorApplication() {
        when(tutorApplicationService.getMostRecentApplicationByStudentId(tutorId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = courseController.getMyCourses(principal);
        assertEquals(403, response.getStatusCode().value());
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
        assertEquals(200, response.getStatusCode().value());
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
        assertEquals(403, response.getStatusCode().value());
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
        assertEquals(403, response.getStatusCode().value());
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
        assertEquals(403, response.getStatusCode().value());
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
        assertEquals(200, response.getStatusCode().value());
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
        assertEquals(403, response.getStatusCode().value());
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
        assertEquals(403, response.getStatusCode().value());
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
        assertEquals(403, response.getStatusCode().value());
        assertTrue(response.getBody().toString().contains("Only the owner can view"));
        verify(courseService, never()).getEnrolledStudents(any());
    }
}