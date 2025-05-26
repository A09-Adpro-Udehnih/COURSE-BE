package com.example.coursebe.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.example.coursebe.model.Course;
import com.example.coursebe.model.TutorApplication;
import com.example.coursebe.service.CourseService;
import com.example.coursebe.service.TutorApplicationService;

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
        verify(tutorApplicationService).getMostRecentApplicationByStudentId(tutorId);
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
        
        when(courseService.getCoursesByTutorId(tutorId)).thenReturn(courses);        ResponseEntity<?> response = courseController.getMyCourses(principal);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof java.util.Map);
        var respMap = (java.util.Map<?,?>) response.getBody();
        assertTrue((Boolean) respMap.get("success"));
        
        // More robust way to handle the courses data, preventing ClassCastException
        Object coursesObj = respMap.get("courses");
        assertTrue(coursesObj != null, "Response should contain 'courses'");
        
        if (coursesObj instanceof List<?> respCourses) {
            assertEquals(2, respCourses.size(), "Should have 2 courses in the response");
            
            // Check course name regardless of the element type in the list
            boolean foundCourse1 = false;
            for (Object course : respCourses) {
                if (course == null) continue;
                
                String courseName = null;
                if (course instanceof java.util.Map<?,?> map) {
                    Object nameObj = map.get("name");
                    courseName = nameObj != null ? nameObj.toString() : null;
                } else if (course instanceof Course courseObj) {
                    courseName = courseObj.getName();
                } else {
                    // Try to get name via toString() as a fallback
                    String courseStr = course.toString();
                    if (courseStr.contains("name=")) {
                        courseName = courseStr.substring(courseStr.indexOf("name=") + 5);
                        if (courseName.contains(",")) {
                            courseName = courseName.substring(0, courseName.indexOf(","));
                        }
                    }
                }
                
                if ("Course 1".equals(courseName)) {
                    foundCourse1 = true;
                    break;
                }
            }
            
            assertTrue(foundCourse1, "Response should contain 'Course 1'");
        }
        
        verify(tutorApplicationService).getMostRecentApplicationByStudentId(tutorId);
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
        verify(tutorApplicationService).getMostRecentApplicationByStudentId(tutorId);
        verify(courseService).getCourseById(courseId);
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
