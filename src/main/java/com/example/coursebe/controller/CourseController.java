package com.example.coursebe.controller;

import com.example.coursebe.model.Course;
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
}
