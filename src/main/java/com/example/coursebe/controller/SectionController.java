package com.example.coursebe.controller;

import com.example.coursebe.model.Course;
import com.example.coursebe.model.Section;
import com.example.coursebe.model.TutorApplication;
import com.example.coursebe.service.CourseService;
import com.example.coursebe.service.SectionService;
import com.example.coursebe.service.TutorApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/courses/{courseId}/sections")
public class SectionController {

    private final SectionService sectionService;
    private final CourseService courseService;
    private final TutorApplicationService tutorApplicationService;

    public SectionController(SectionService sectionService, CourseService courseService, TutorApplicationService tutorApplicationService) {
        this.sectionService = sectionService;
        this.courseService = courseService;
        this.tutorApplicationService = tutorApplicationService;
    }

    // DTO for Section creation and update
    public static class SectionRequest {
        public String title;
        public Integer position;
    }

    private ResponseEntity<?> checkTutorAndCourseOwnership(UUID courseId, Principal principal) {
        UUID tutorId = UUID.fromString(principal.getName());
        Optional<TutorApplication> appOpt = tutorApplicationService.getMostRecentApplicationByStudentId(tutorId);
        if (appOpt.isEmpty() || appOpt.get().getStatus() != TutorApplication.Status.ACCEPTED) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.FORBIDDEN.value());
            resp.put("success", false);
            resp.put("message", "You are not allowed to modify this course. Tutor application must be ACCEPTED.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        }

        Optional<Course> courseOpt = courseService.getCourseById(courseId);
        if (courseOpt.isEmpty() || !courseOpt.get().getTutorId().equals(tutorId)) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.FORBIDDEN.value());
            resp.put("success", false);
            resp.put("message", "You are not the owner of this course.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        }
        return null;
    }

    @PostMapping
    public ResponseEntity<?> createSection(@PathVariable UUID courseId, @RequestBody SectionRequest req, Principal principal) {
        ResponseEntity<?> ownershipCheck = checkTutorAndCourseOwnership(courseId, principal);
        if (ownershipCheck != null) {
            return ownershipCheck;
        }

        Optional<Course> courseOpt = courseService.getCourseById(courseId);
        if (courseOpt.isEmpty()) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.NOT_FOUND.value());
            resp.put("success", false);
            resp.put("message", "Course not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }

        try {
            Section section = sectionService.createSection(courseId, req.title, req.position);
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.CREATED.value());
            resp.put("success", true);
            resp.put("message", "Section created successfully.");
            resp.put("sectionId", section.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (IllegalArgumentException e) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.BAD_REQUEST.value());
            resp.put("success", false);
            resp.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
        }
    }

    @GetMapping
    public ResponseEntity<?> getSections(@PathVariable UUID courseId, Principal principal) {
        Optional<Course> courseOpt = courseService.getCourseById(courseId);
        if (courseOpt.isEmpty()) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.NOT_FOUND.value());
            resp.put("success", false);
            resp.put("message", "Course not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }

        List<Section> sections = sectionService.getSectionsByCourseId(courseId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", HttpStatus.OK.value());
        resp.put("success", true);
        resp.put("sections", sections);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{sectionId}")
    public ResponseEntity<?> getSectionById(@PathVariable UUID courseId, @PathVariable UUID sectionId, Principal principal) {
        Optional<Section> sectionOpt = sectionService.getSectionById(sectionId);
        if (sectionOpt.isEmpty() || !sectionOpt.get().getCourse().getId().equals(courseId)) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.NOT_FOUND.value());
            resp.put("success", false);
            resp.put("message", "Section not found in this course.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("code", HttpStatus.OK.value());
        resp.put("success", true);
        resp.put("section", sectionOpt.get());
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/{sectionId}")
    public ResponseEntity<?> updateSection(@PathVariable UUID courseId, @PathVariable UUID sectionId, @RequestBody SectionRequest req, Principal principal) {
        ResponseEntity<?> ownershipCheck = checkTutorAndCourseOwnership(courseId, principal);
        if (ownershipCheck != null) {
            return ownershipCheck;
        }

        Optional<Section> sectionOpt = sectionService.getSectionById(sectionId);
        if (sectionOpt.isEmpty() || !sectionOpt.get().getCourse().getId().equals(courseId)) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.NOT_FOUND.value());
            resp.put("success", false);
            resp.put("message", "Section not found in this course.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }
        
        try {
            Optional<Section> updatedSectionOpt = sectionService.updateSection(sectionId, req.title, req.position);
            if (updatedSectionOpt.isEmpty()) {
                Map<String, Object> resp = new HashMap<>();
                resp.put("code", HttpStatus.NOT_FOUND.value());
                resp.put("success", false);
                resp.put("message", "Section not found for update.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
            }
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.OK.value());
            resp.put("success", true);
            resp.put("message", "Section updated successfully.");
            resp.put("section", updatedSectionOpt.get());
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.BAD_REQUEST.value());
            resp.put("success", false);
            resp.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
        }
    }

    @DeleteMapping("/{sectionId}")
    public ResponseEntity<?> deleteSection(@PathVariable UUID courseId, @PathVariable UUID sectionId, Principal principal) {
        ResponseEntity<?> ownershipCheck = checkTutorAndCourseOwnership(courseId, principal);
        if (ownershipCheck != null) {
            return ownershipCheck;
        }

        Optional<Section> sectionOpt = sectionService.getSectionById(sectionId);
        if (sectionOpt.isEmpty() || !sectionOpt.get().getCourse().getId().equals(courseId)) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.NOT_FOUND.value());
            resp.put("success", false);
            resp.put("message", "Section not found in this course.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }

        boolean deleted = sectionService.deleteSection(sectionId);
        Map<String, Object> resp = new HashMap<>();
        if (deleted) {
            resp.put("code", HttpStatus.OK.value());
            resp.put("success", true);
            resp.put("message", "Section deleted successfully.");
            return ResponseEntity.ok(resp);
        } else {
            resp.put("code", HttpStatus.NOT_FOUND.value());
            resp.put("success", false);
            resp.put("message", "Section not found for deletion.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }
    }
}
