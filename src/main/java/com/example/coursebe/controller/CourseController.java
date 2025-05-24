package com.example.coursebe.controller;

import com.example.coursebe.model.Course;
import com.example.coursebe.model.TutorApplication;
import com.example.coursebe.service.CourseService;
import com.example.coursebe.service.TutorApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/courses")
public class CourseController {
    private final CourseService courseService;
    private final TutorApplicationService tutorApplicationService;

    @Autowired
    public CourseController(CourseService courseService, TutorApplicationService tutorApplicationService) {
        this.courseService = courseService;
        this.tutorApplicationService = tutorApplicationService;
    }

    // DTO for course creation
    public static class CreateCourseRequest {
        public String name;
        public String description;
        public BigDecimal price;
    }

    // POST /courses - Async implementation
    @PostMapping
    public CompletableFuture<ResponseEntity<?>> createCourse(@RequestBody CreateCourseRequest req, Principal principal) {
        UUID tutorId = UUID.fromString(principal.getName());
        
        // Validasi: hanya tutor dengan status ACCEPTED yang boleh membuat kursus
        return tutorApplicationService.getMostRecentApplicationByStudentIdAsync(tutorId)
            .thenCompose(appOpt -> {
                if (appOpt.isEmpty() || appOpt.get().getStatus() != TutorApplication.Status.ACCEPTED) {
                    Map<String, Object> resp = new HashMap<>();
                    resp.put("code", HttpStatus.FORBIDDEN.value());
                    resp.put("success", false);
                    resp.put("message", "You are not allowed to create a course. Tutor application must be ACCEPTED.");
                    return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp));
                }
                
                return courseService.createCourseAsync(req.name, req.description, tutorId, req.price)
                    .thenApply(course -> {
                        Map<String, Object> resp = new HashMap<>();
                        resp.put("code", HttpStatus.CREATED.value());
                        resp.put("success", true);
                        resp.put("message", "Course created successfully.");
                        resp.put("courseId", course.getId());
                        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
                    });
            });
    }

    // GET /courses/mine - Async implementation
    @GetMapping("/mine")
    public CompletableFuture<ResponseEntity<?>> getMyCourses(Principal principal) {
        UUID tutorId = UUID.fromString(principal.getName());
        
        return tutorApplicationService.getMostRecentApplicationByStudentIdAsync(tutorId)
            .thenCompose(appOpt -> {
                if (appOpt.isEmpty() || appOpt.get().getStatus() != TutorApplication.Status.ACCEPTED) {
                    Map<String, Object> resp = new HashMap<>();
                    resp.put("code", HttpStatus.FORBIDDEN.value());
                    resp.put("success", false);
                    resp.put("message", "You are not allowed to view courses. Tutor application must be ACCEPTED.");
                    return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp));
                }
                
                return courseService.getCoursesByTutorIdAsync(tutorId)
                    .thenApply(courses -> {
                        Map<String, Object> resp = new HashMap<>();
                        resp.put("code", HttpStatus.OK.value());
                        resp.put("success", true);
                        resp.put("courses", courses);
                        return ResponseEntity.ok(resp);
                    });
            });
    }

    // DELETE /courses/{courseId}
    @DeleteMapping("/{courseId}")
    public ResponseEntity<?> deleteCourse(@PathVariable UUID courseId, Principal principal) {
        UUID tutorId = UUID.fromString(principal.getName());
        // Validasi: hanya tutor yang memiliki kursus dan status ACCEPTED yang bisa hapus
        var appOpt = tutorApplicationService.getMostRecentApplicationByStudentId(tutorId);
        if (appOpt.isEmpty() || appOpt.get().getStatus() != TutorApplication.Status.ACCEPTED) {
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

    // GET /courses/{courseId}/students - Async implementation
    @GetMapping("/{courseId}/students")
    public CompletableFuture<ResponseEntity<?>> getEnrolledStudents(@PathVariable UUID courseId, Principal principal) {
        UUID tutorId = UUID.fromString(principal.getName());
        
        // Validasi: hanya tutor owner & status ACCEPTED yang bisa akses
        return tutorApplicationService.getMostRecentApplicationByStudentIdAsync(tutorId)
            .thenCompose(appOpt -> {
                if (appOpt.isEmpty() || appOpt.get().getStatus() != TutorApplication.Status.ACCEPTED) {
                    Map<String, Object> resp = new HashMap<>();
                    resp.put("code", HttpStatus.FORBIDDEN.value());
                    resp.put("success", false);
                    resp.put("message", "You are not allowed to view students. Tutor application must be ACCEPTED.");
                    return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp));
                }
                
                return courseService.getCourseByIdAsync(courseId)
                    .thenCompose(courseOpt -> {
                        if (courseOpt.isEmpty() || !courseOpt.get().getTutorId().equals(tutorId)) {
                            Map<String, Object> resp = new HashMap<>();
                            resp.put("code", HttpStatus.FORBIDDEN.value());
                            resp.put("success", false);
                            resp.put("message", "You are not allowed to view students. Only the owner can view.");
                            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp));
                        }
                        
                        return courseService.getEnrolledStudentsAsync(courseId)
                            .thenApply(students -> {
                                Map<String, Object> resp = new HashMap<>();
                                resp.put("code", HttpStatus.OK.value());
                                resp.put("success", true);
                                resp.put("students", students);
                                return ResponseEntity.ok(resp);
                            });
                    });
            });
    }
}
