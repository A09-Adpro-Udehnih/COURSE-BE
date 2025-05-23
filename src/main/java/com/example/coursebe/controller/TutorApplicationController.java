package com.example.coursebe.controller;

import com.example.coursebe.model.TutorApplication;
import com.example.coursebe.service.TutorApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/tutors/registration")
public class TutorApplicationController {
    private final TutorApplicationService tutorApplicationService;

    @Autowired
    public TutorApplicationController(TutorApplicationService tutorApplicationService) {
        this.tutorApplicationService = tutorApplicationService;
    }

    // POST /tutors/registration - Async implementation
    @PostMapping
    public CompletableFuture<ResponseEntity<?>> registerAsTutor(Principal principal) {
        // Assume studentId is fetched from JWT principal (implementasi bisa disesuaikan)
        UUID studentId = UUID.fromString(principal.getName());
        
        if (tutorApplicationService.hasPendingApplication(studentId)) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.BAD_REQUEST.value());
            resp.put("success", false);
            resp.put("message", "You already have a pending tutor application.");
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(resp));
        }
        
        return tutorApplicationService.submitApplicationAsync(studentId)
            .thenApply(app -> {
                Map<String, Object> resp = new HashMap<>();
                resp.put("code", HttpStatus.OK.value());
                resp.put("success", true);
                resp.put("message", "Tutor application submitted.");
                resp.put("tutorApplicationId", app.getId());
                return ResponseEntity.ok(resp);
            });
    }

    // GET /tutors/registration - Async implementation
    @GetMapping
    public CompletableFuture<ResponseEntity<?>> getTutorRegistrationStatus(Principal principal) {
        UUID studentId = UUID.fromString(principal.getName());
        
        return tutorApplicationService.getMostRecentApplicationByStudentIdAsync(studentId)
            .thenApply(appOpt -> {
                Map<String, Object> resp = new HashMap<>();
                resp.put("code", HttpStatus.OK.value());
                resp.put("success", true);
                
                if (appOpt.isPresent()) {
                    resp.put("status", appOpt.get().getStatus());
                    resp.put("tutorApplicationId", appOpt.get().getId());
                } else {
                    resp.put("status", null);
                    resp.put("message", "No tutor application found.");
                }
                
                return ResponseEntity.ok(resp);
            });
    }

    // DELETE /tutors/registration
    @DeleteMapping
    public ResponseEntity<?> deleteTutorRegistration(Principal principal) {
        UUID studentId = UUID.fromString(principal.getName());
        boolean deleted = tutorApplicationService.deleteApplicationByStudentId(studentId);
        Map<String, Object> resp = new HashMap<>();
        if (deleted) {
            resp.put("code", HttpStatus.OK.value());
            resp.put("success", true);
            resp.put("message", "Tutor application deleted successfully.");
            return ResponseEntity.ok(resp);
        } else {
            resp.put("code", HttpStatus.NOT_FOUND.value());
            resp.put("success", false);
            resp.put("message", "No tutor application found to delete.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }
    }
}
