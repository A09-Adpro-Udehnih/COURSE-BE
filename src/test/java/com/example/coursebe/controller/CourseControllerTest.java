package com.example.coursebe.controller;

import com.example.coursebe.model.Course;
import com.example.coursebe.model.TutorApplication;
import com.example.coursebe.service.CourseService;
import com.example.coursebe.service.TutorApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseControllerTest {
    @Mock
    private CourseService courseService;
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
        when(principal.getName()).thenReturn(tutorId.toString());
    }

    @Test
    @DisplayName("POST /courses - success (ACCEPTED tutor)")
    void createCourse_success() {
        CourseController.CreateCourseRequest req = new CourseController.CreateCourseRequest();
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
        CourseController.CreateCourseRequest req = new CourseController.CreateCourseRequest();
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
        CourseController.CreateCourseRequest req = new CourseController.CreateCourseRequest();
        req.name = "Test Course";
        req.description = "Desc";
        req.price = new BigDecimal("100000");
        when(tutorApplicationService.getMostRecentApplicationByStudentId(tutorId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = courseController.createCourse(req, principal);
        assertEquals(403, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("must be ACCEPTED"));
        verify(courseService, never()).createCourse(any(), any(), any(), any());
    }
}
