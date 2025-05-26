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
import java.util.Optional;
import java.util.UUID;
import java.util.List;

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

    // DTOs for course content update
    public static class ArticleDto {
        public UUID id; // Null for new articles, existing ID for updates
        public String title;
        public String content;
        public Integer position;
    }

    public static class SectionDto {
        public UUID id; // Null for new sections, existing ID for updates
        public String title;
        public Integer position;
        public List<ArticleDto> articles;
    }

    // DTO for course update (now includes sections)
    public static class UpdateCourseRequest {
        public String name;
        public String description;
        public BigDecimal price;
        public List<SectionDto> sections; // To manage course content
    }

    // POST /courses - Sync implementation for JWT compatibility
    @PostMapping
    public ResponseEntity<?> createCourse(@RequestBody CreateCourseRequest req, Principal principal) {
        UUID tutorId = UUID.fromString(principal.getName());
        
        Optional<TutorApplication> appOpt = tutorApplicationService.getMostRecentApplicationByStudentId(tutorId);
        if (appOpt.isEmpty() || appOpt.get().getStatus() != TutorApplication.Status.ACCEPTED) {
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

    // GET /courses/mine - Sync implementation for JWT compatibility
    @GetMapping("/mine")
    public ResponseEntity<?> getMyCourses(Principal principal) {
        UUID tutorId = UUID.fromString(principal.getName());
        
        Optional<TutorApplication> appOpt = tutorApplicationService.getMostRecentApplicationByStudentId(tutorId);
        if (appOpt.isEmpty() || appOpt.get().getStatus() != TutorApplication.Status.ACCEPTED) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.FORBIDDEN.value());
            resp.put("success", false);
            resp.put("message", "You are not allowed to view courses. Tutor application must be ACCEPTED.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        }
        
        List<Course> courses = courseService.getCoursesByTutorId(tutorId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", HttpStatus.OK.value());
        resp.put("success", true);
        resp.put("courses", courses);
        return ResponseEntity.ok(resp);
    }

    // PUT /courses/{courseId} - Sync implementation for JWT compatibility
    @PutMapping("/{courseId}")
    public ResponseEntity<?> updateCourse(@PathVariable UUID courseId, @RequestBody UpdateCourseRequest req, Principal principal) {
        UUID tutorId = UUID.fromString(principal.getName());

        // Validasi: hanya tutor dengan status ACCEPTED yang boleh mengedit kursus
        Optional<TutorApplication> appOpt = tutorApplicationService.getMostRecentApplicationByStudentId(tutorId);
        if (appOpt.isEmpty() || appOpt.get().getStatus() != TutorApplication.Status.ACCEPTED) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.FORBIDDEN.value());
            resp.put("success", false);
            resp.put("message", "You are not allowed to update courses. Tutor application must be ACCEPTED.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        }

        // Validasi: hanya pemilik kursus yang boleh mengedit
        Optional<Course> courseOpt = courseService.getCourseById(courseId);
        if (courseOpt.isEmpty()) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.NOT_FOUND.value());
            resp.put("success", false);
            resp.put("message", "Course not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }

        if (!courseOpt.get().getTutorId().equals(tutorId)) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.FORBIDDEN.value());
            resp.put("success", false);
            resp.put("message", "You are not allowed to update this course. Only the owner can update.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        }

        // Pass the sections to the service layer - This will require CourseService to be updated
        Optional<Course> updatedCourseOpt = courseService.updateCourse(courseId, req.name, req.description, req.price, req.sections);

        if (updatedCourseOpt.isPresent()) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.OK.value());
            resp.put("success", true);
            resp.put("message", "Course updated successfully.");
            resp.put("course", updatedCourseOpt.get());
            return ResponseEntity.ok(resp);
        } else {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value()); // Or NOT_FOUND if appropriate
            resp.put("success", false);
            resp.put("message", "Failed to update course.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    // DELETE /courses/{courseId}
    @DeleteMapping("/{courseId}")
    public ResponseEntity<?> deleteCourse(@PathVariable UUID courseId, Principal principal) {
        UUID tutorId = UUID.fromString(principal.getName());
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

    // GET /courses/{courseId}/students - Sync implementation for JWT compatibility
    @GetMapping("/{courseId}/students")
    public ResponseEntity<?> getEnrolledStudents(@PathVariable UUID courseId, Principal principal) {
        UUID tutorId = UUID.fromString(principal.getName());
        
        Optional<TutorApplication> appOpt = tutorApplicationService.getMostRecentApplicationByStudentId(tutorId);
        if (appOpt.isEmpty() || appOpt.get().getStatus() != TutorApplication.Status.ACCEPTED) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.FORBIDDEN.value());
            resp.put("success", false);
            resp.put("message", "You are not allowed to view students. Tutor application must be ACCEPTED.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        }
        
        Optional<Course> courseOpt = courseService.getCourseById(courseId);
        if (courseOpt.isEmpty() || !courseOpt.get().getTutorId().equals(tutorId)) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.FORBIDDEN.value());
            resp.put("success", false);
            resp.put("message", "You are not allowed to view students. Only the owner can view.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        }
        
        List<String> students = courseService.getEnrolledStudents(courseId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", HttpStatus.OK.value());
        resp.put("success", true);
        resp.put("students", students);
        return ResponseEntity.ok(resp);
    }
}
