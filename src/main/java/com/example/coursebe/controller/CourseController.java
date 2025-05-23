package com.example.coursebe.controller;

import com.example.coursebe.exception.UnsupportedSearchTypeException;
import com.example.coursebe.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.coursebe.model.Course;
import com.example.coursebe.service.CourseService;
import com.example.coursebe.service.TutorApplicationService;
import com.example.coursebe.dto.CreateCourseRequest;

import java.security.Principal;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/courses")
public class CourseController {
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final TutorApplicationService tutorApplicationService;

    @Autowired
    public CourseController(CourseService courseService, EnrollmentService enrollmentService, TutorApplicationService tutorApplicationService) {
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
        this.tutorApplicationService = tutorApplicationService;
    }

    @GetMapping
    public ResponseEntity<?> getAllCourses(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword
    ) {
        try {
            List<Course> courses;
            if (type != null && keyword != null) {
                courses = courseService.searchCourses(type, keyword);
            } else {
                courses = courseService.getAllCourses();
            }
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.OK.value());
            resp.put("success", true);
            resp.put("courses", courses);
            return ResponseEntity.status(HttpStatus.OK).body(resp);
        } catch (UnsupportedSearchTypeException e) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.BAD_REQUEST.value());
            resp.put("success", false);
            resp.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCourseById(@PathVariable UUID id) {
        Optional<Course> courseOpt = courseService.getCourseById(id);

        if (courseOpt.isEmpty()) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.NOT_FOUND.value());
            resp.put("success", false);
            resp.put("message", "Course not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }

        Course course = courseOpt.get();
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", HttpStatus.OK.value());
        resp.put("success", true);
        resp.put("course", course);
        return ResponseEntity.status(HttpStatus.OK).body(resp);
    }

    @PostMapping("{id}/enroll")
    public CompletableFuture<ResponseEntity<?>> enrollCourse(@PathVariable UUID id, Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        var courseOpt = courseService.getCourseById(id);

        if (courseOpt.isEmpty()) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.NOT_FOUND.value());
            resp.put("success", false);
            resp.put("message", "Course not found.");
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp));
        }

        return enrollmentService.enroll(userId, id)
                .thenApply(enrollment -> {
                    if (enrollment != null) {
                        Map<String, Object> resp = new HashMap<>();
                        resp.put("code", HttpStatus.OK.value());
                        resp.put("success", true);
                        resp.put("message", "Successfully enrolled in the course.");
                        return (ResponseEntity<?>) ResponseEntity.status(HttpStatus.OK).body(resp);
                    } else {
                        Map<String, Object> resp = new HashMap<>();
                        resp.put("code", HttpStatus.BAD_REQUEST.value());
                        resp.put("success", false);
                        resp.put("message", "Failed to enroll in the course.");
                        return (ResponseEntity<?>) ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
                    }
                })
                .exceptionally(ex -> {
                    Map<String, Object> resp = new HashMap<>();
                    resp.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
                    resp.put("success", false);
                    resp.put("message", "Error enrolling in course: " +
                            (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()));
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
                });
    }

    // POST /courses
    @PostMapping
    public ResponseEntity<?> createCourse(@RequestBody CreateCourseRequest req, Principal principal) {
        UUID tutorId = UUID.fromString(principal.getName());

        // Validasi: hanya tutor dengan status ACCEPTED yang boleh membuat kursus
        var appOpt = tutorApplicationService.getMostRecentApplicationByStudentId(tutorId);
        if (appOpt.isEmpty() || appOpt.get().getStatus() != com.example.coursebe.model.TutorApplication.Status.ACCEPTED) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.FORBIDDEN.value());
            resp.put("success", false);
            resp.put("message", "You are not allowed to create a course. Tutor application must be ACCEPTED.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        }

        Course course = courseService.createCourse(req.name, req.description, tutorId, req.price);
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", HttpStatus.CREATED.value());
        resp.put("success", true);
        resp.put("message", "Course created successfully.");
        resp.put("courseId", course.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    // GET /courses/mine
    @GetMapping("/mine")
    public ResponseEntity<?> getMyCourses(Principal principal) {
        UUID tutorId = UUID.fromString(principal.getName());

        var appOpt = tutorApplicationService.getMostRecentApplicationByStudentId(tutorId);
        if (appOpt.isEmpty() || appOpt.get().getStatus() != com.example.coursebe.model.TutorApplication.Status.ACCEPTED) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.FORBIDDEN.value());
            resp.put("success", false);
            resp.put("message", "You are not allowed to view courses. Tutor application must be ACCEPTED.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        }

        var courses = courseService.getCoursesByTutorId(tutorId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", HttpStatus.OK.value());
        resp.put("success", true);
        resp.put("courses", courses);
        return ResponseEntity.ok(resp);
    }

    // DELETE /courses/{courseId}
    @DeleteMapping("/{courseId}")
    public ResponseEntity<?> deleteCourse(@PathVariable UUID courseId, Principal principal) {
        UUID tutorId = UUID.fromString(principal.getName());

        // Validasi: hanya tutor yang memiliki kursus dan status ACCEPTED yang bisa hapus
        var appOpt = tutorApplicationService.getMostRecentApplicationByStudentId(tutorId);
        if (appOpt.isEmpty() || appOpt.get().getStatus() != com.example.coursebe.model.TutorApplication.Status.ACCEPTED) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.FORBIDDEN.value());
            resp.put("success", false);
            resp.put("message", "You are not allowed to delete this course. Tutor application must be ACCEPTED.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        }

        var courseOpt = courseService.getCourseById(courseId);
        if (courseOpt.isEmpty() || !courseOpt.get().getTutorId().equals(tutorId)) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.FORBIDDEN.value());
            resp.put("success", false);
            resp.put("message", "You are not allowed to delete this course. Only the owner can delete.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        }
        boolean deleted = courseService.deleteCourse(courseId);
        Map<String, Object> resp = new HashMap<>();
        if (deleted) {
            resp.put("code", HttpStatus.OK.value());
            resp.put("success", true);
            resp.put("message", "Course deleted successfully.");
            return ResponseEntity.ok(resp);
        } else {
            resp.put("code", HttpStatus.NOT_FOUND.value());
            resp.put("success", false);
            resp.put("message", "Course not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }
    }

    // GET /courses/{courseId}/students
    @GetMapping("/{courseId}/students")
    public ResponseEntity<?> getEnrolledStudents(@PathVariable UUID courseId, Principal principal) {
        UUID tutorId = UUID.fromString(principal.getName());
        // Validasi: hanya tutor owner & status ACCEPTED yang bisa akses
        var appOpt = tutorApplicationService.getMostRecentApplicationByStudentId(tutorId);
        if (appOpt.isEmpty() || appOpt.get().getStatus() != com.example.coursebe.model.TutorApplication.Status.ACCEPTED) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.FORBIDDEN.value());
            resp.put("success", false);
            resp.put("message", "You are not allowed to view students. Tutor application must be ACCEPTED.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        }
        var courseOpt = courseService.getCourseById(courseId);
        if (courseOpt.isEmpty() || !courseOpt.get().getTutorId().equals(tutorId)) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.FORBIDDEN.value());
            resp.put("success", false);
            resp.put("message", "You are not allowed to view students. Only the owner can view.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        }
        var students = courseService.getEnrolledStudents(courseId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", HttpStatus.OK.value());
        resp.put("success", true);
        resp.put("students", students);
        return ResponseEntity.ok(resp);
    }
}
