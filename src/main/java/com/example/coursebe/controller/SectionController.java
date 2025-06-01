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
        System.out.println("=== checkTutorAndCourseOwnership ENTRY ===");
        System.out.println("CourseId: " + courseId);
        System.out.println("Principal: " + (principal != null ? principal.getName() : "null"));
        
        try {
            UUID tutorId = UUID.fromString(principal.getName());
            System.out.println("Tutor ID: " + tutorId);
            
            System.out.println("Getting most recent tutor application...");
            Optional<TutorApplication> appOpt = tutorApplicationService.getMostRecentApplicationByStudentId(tutorId);
            System.out.println("Tutor application found: " + appOpt.isPresent());
            
            if (appOpt.isEmpty() || appOpt.get().getStatus() != TutorApplication.Status.ACCEPTED) {
                System.out.println("Tutor application not found or not ACCEPTED. Status: " + 
                    (appOpt.isPresent() ? appOpt.get().getStatus() : "NOT_FOUND"));
                Map<String, Object> response = new HashMap<>();
                response.put("code", HttpStatus.FORBIDDEN.value());
                response.put("success", false);
                response.put("message", "You are not allowed to modify this course. Tutor application must be ACCEPTED.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            System.out.println("Tutor application is ACCEPTED. Checking course ownership...");
            Optional<Course> courseOpt = courseService.getCourseById(courseId);
            System.out.println("Course found: " + courseOpt.isPresent());
            
            if (courseOpt.isEmpty() || !courseOpt.get().getTutorId().equals(tutorId)) {
                System.out.println("Course not found or tutor is not the owner. Course tutor ID: " + 
                    (courseOpt.isPresent() ? courseOpt.get().getTutorId() : "N/A"));
                Map<String, Object> response = new HashMap<>();
                response.put("code", HttpStatus.FORBIDDEN.value());
                response.put("success", false);
                response.put("message", "You are not the owner of this course.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            System.out.println("Ownership check passed successfully");
            return null;
            
        } catch (Exception e) {
            System.out.println("Exception in ownership check: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("code", HttpStatus.FORBIDDEN.value());
            response.put("success", false);
            response.put("message", "Error checking ownership: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } finally {
            System.out.println("=== checkTutorAndCourseOwnership EXIT ===");
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createSection(@PathVariable UUID courseId, @RequestBody SectionRequest req, Principal principal) {
        System.out.println("=== SectionController.createSection ENTRY ===");
        System.out.println("CourseId: " + courseId);
        System.out.println("Request title: " + req.title);
        System.out.println("Request position: " + req.position);
        System.out.println("Principal: " + (principal != null ? principal.getName() : "null"));
        
        try {
            System.out.println("Checking tutor and course ownership...");
            ResponseEntity<?> ownershipCheck = checkTutorAndCourseOwnership(courseId, principal);
            if (ownershipCheck != null) {
                System.out.println("Ownership check failed, returning: " + ownershipCheck.getStatusCode());
                return ownershipCheck;
            }
            System.out.println("Ownership check passed");
            
            System.out.println("Getting course by ID...");
            Optional<Course> courseOpt = courseService.getCourseById(courseId);
            if (courseOpt.isEmpty()) {
                System.out.println("Course not found");
                Map<String, Object> response = new HashMap<>();
                response.put("code", HttpStatus.NOT_FOUND.value());
                response.put("success", false);
                response.put("message", "Course not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            Course course = courseOpt.get();
            System.out.println("Course found: " + course.getId() + ", Status: " + course.getStatus());
            
            // Check if course is approved before allowing section creation
            if (course.getStatus() == com.example.coursebe.enums.Status.PENDING) {
                System.out.println("Course has PENDING status, rejecting");
                Map<String, Object> response = new HashMap<>();
                response.put("code", HttpStatus.FORBIDDEN.value());
                response.put("success", false);
                response.put("message", "Cannot create sections for courses with PENDING status. Please wait for course approval.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            if (course.getStatus() == com.example.coursebe.enums.Status.DENIED) {
                System.out.println("Course has DENIED status, rejecting");
                Map<String, Object> response = new HashMap<>();
                response.put("code", HttpStatus.FORBIDDEN.value());
                response.put("success", false);
                response.put("message", "Cannot create sections for DENIED courses.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            System.out.println("Course status is acceptable: " + course.getStatus());
            System.out.println("Calling sectionService.createSection...");
            
            Section section = sectionService.createSection(courseId, req.title, req.position);
            System.out.println("Section created successfully: " + (section != null ? section.getId() : "null"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", HttpStatus.CREATED.value());
            response.put("success", true);
            response.put("message", "Section created successfully.");
            response.put("sectionId", section.getId());
            
            System.out.println("Returning response: " + response);
            ResponseEntity<?> result = ResponseEntity.status(HttpStatus.CREATED).body(response);
            System.out.println("Response entity status: " + result.getStatusCode());
            return result;
            
        } catch (IllegalArgumentException e) {
            System.out.println("IllegalArgumentException: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("code", HttpStatus.BAD_REQUEST.value());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            System.out.println("Unexpected exception: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } finally {
            System.out.println("=== SectionController.createSection EXIT ===");
        }
    }

    @GetMapping
    public ResponseEntity<?> getSections(@PathVariable UUID courseId, Principal principal) {
        Optional<Course> courseOpt = courseService.getCourseById(courseId);
        if (courseOpt.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", HttpStatus.NOT_FOUND.value());
            response.put("success", false);
            response.put("message", "Course not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        List<Section> sections = sectionService.getSectionsByCourseId(courseId);
        Map<String, Object> response = new HashMap<>();
        response.put("code", HttpStatus.OK.value());
        response.put("success", true);
        response.put("sections", sections);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{sectionId}")
    public ResponseEntity<?> getSectionById(@PathVariable UUID courseId, @PathVariable UUID sectionId, Principal principal) {
        Optional<Section> sectionOpt = sectionService.getSectionById(sectionId);
        if (sectionOpt.isEmpty() || !sectionOpt.get().getCourse().getId().equals(courseId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", HttpStatus.NOT_FOUND.value());
            response.put("success", false);
            response.put("message", "Section not found in this course.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("code", HttpStatus.OK.value());
        response.put("success", true);
        response.put("section", sectionOpt.get());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{sectionId}")
    public ResponseEntity<?> updateSection(@PathVariable UUID courseId, @PathVariable UUID sectionId, @RequestBody SectionRequest req, Principal principal) {
        ResponseEntity<?> ownershipCheck = checkTutorAndCourseOwnership(courseId, principal);
        if (ownershipCheck != null) {
            return ownershipCheck;
        }

        Optional<Section> sectionOpt = sectionService.getSectionById(sectionId);
        if (sectionOpt.isEmpty() || !sectionOpt.get().getCourse().getId().equals(courseId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", HttpStatus.NOT_FOUND.value());
            response.put("success", false);
            response.put("message", "Section not found in this course.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }        
        // Check if course is approved before allowing section modification
        Course course = sectionOpt.get().getCourse();
        if (course.getStatus() == com.example.coursebe.enums.Status.PENDING) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", HttpStatus.FORBIDDEN.value());
            response.put("success", false);
            response.put("message", "Cannot modify sections for courses with PENDING status. Please wait for course approval.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        if (course.getStatus() == com.example.coursebe.enums.Status.DENIED) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", HttpStatus.FORBIDDEN.value());
            response.put("success", false);
            response.put("message", "Cannot modify sections for DENIED courses.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        try {
            Optional<Section> updatedSectionOpt = sectionService.updateSection(sectionId, req.title, req.position);
            if (updatedSectionOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", HttpStatus.NOT_FOUND.value());
                response.put("success", false);
                response.put("message", "Section not found for update.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            Map<String, Object> response = new HashMap<>();
            response.put("code", HttpStatus.OK.value());
            response.put("success", true);
            response.put("message", "Section updated successfully.");
            response.put("section", updatedSectionOpt.get());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", HttpStatus.BAD_REQUEST.value());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
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
            Map<String, Object> response = new HashMap<>();
            response.put("code", HttpStatus.NOT_FOUND.value());
            response.put("success", false);
            response.put("message", "Section not found in this course.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // Check if course is approved before allowing section deletion
        Course course = sectionOpt.get().getCourse();
        if (course.getStatus() != com.example.coursebe.enums.Status.ACCEPTED) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", HttpStatus.FORBIDDEN.value());
            response.put("success", false);
            response.put("message", "Cannot delete sections for courses that are not approved. Current status: " + course.getStatus());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        boolean deleted = sectionService.deleteSection(sectionId);
        Map<String, Object> response = new HashMap<>();
        if (deleted) {
            response.put("code", HttpStatus.OK.value());
            response.put("success", true);
            response.put("message", "Section deleted successfully.");
            return ResponseEntity.ok(response);
        } else {
            response.put("code", HttpStatus.NOT_FOUND.value());
            response.put("success", false);
            response.put("message", "Section not found for deletion.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
