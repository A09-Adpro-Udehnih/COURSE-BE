package com.example.coursebe.controller;

import com.example.coursebe.dto.GlobalResponse;
import com.example.coursebe.dto.tutorapplication.TutorApplicationResponse;
import com.example.coursebe.dto.tutorapplication.TutorApplicationStatusResponse;
import com.example.coursebe.model.TutorApplication;
import com.example.coursebe.service.TutorApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/tutors/registration")
public class TutorApplicationController {
    private final TutorApplicationService tutorApplicationService;

    public TutorApplicationController(TutorApplicationService tutorApplicationService) {
        this.tutorApplicationService = tutorApplicationService;
    }
      
    @PostMapping
    public ResponseEntity<GlobalResponse<TutorApplicationResponse>> registerAsTutor(Principal principal) {
        try {
            UUID studentId = UUID.fromString(principal.getName());
            
            if (tutorApplicationService.hasAnyApplication(studentId)) {
                return ResponseEntity.badRequest().body(GlobalResponse.<TutorApplicationResponse>builder()
                        .code(HttpStatus.BAD_REQUEST)
                        .success(false)
                        .message("You already have a tutor application.")
                        .data(null)
                        .build());
            }
            
            TutorApplication app = tutorApplicationService.submitApplication(studentId);
            return ResponseEntity.ok(GlobalResponse.<TutorApplicationResponse>builder()
                    .code(HttpStatus.OK)
                    .success(true)
                    .message("Tutor application submitted.")
                    .data(toResponse(app))
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(GlobalResponse.<TutorApplicationResponse>builder()
                    .code(HttpStatus.BAD_REQUEST)
                    .success(false)
                    .message("Invalid student ID format.")
                    .data(null)
                    .build());
        }
    }    
    
    @GetMapping
    public ResponseEntity<GlobalResponse<TutorApplicationStatusResponse>> getTutorRegistrationStatus(Principal principal) {
        try {
            UUID studentId = UUID.fromString(principal.getName());
            
            Optional<TutorApplication> appOpt = tutorApplicationService.getMostRecentApplicationByStudentId(studentId);
              if (appOpt.isPresent()) {
                TutorApplicationStatusResponse statusResponse = TutorApplicationStatusResponse.builder()
                        .status(appOpt.get().getStatus().name())
                        .tutorApplicationId(appOpt.get().getId())
                        .message(null)
                        .build();
                
                return ResponseEntity.ok(GlobalResponse.<TutorApplicationStatusResponse>builder()
                        .code(HttpStatus.OK)
                        .success(true)
                        .message("Tutor application status retrieved successfully.")
                        .data(statusResponse)
                        .build());
            } else {
                TutorApplicationStatusResponse statusResponse = TutorApplicationStatusResponse.builder()
                        .status(null)
                        .tutorApplicationId(null)
                        .message("No tutor application found.")
                        .build();
                
                return ResponseEntity.ok(GlobalResponse.<TutorApplicationStatusResponse>builder()
                        .code(HttpStatus.OK)
                        .success(true)
                        .message("No tutor application found.")
                        .data(statusResponse)
                        .build());
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(GlobalResponse.<TutorApplicationStatusResponse>builder()
                    .code(HttpStatus.BAD_REQUEST)
                    .success(false)
                    .message("Invalid student ID format.")
                    .data(null)
                    .build());
        }
    }    
    
    @DeleteMapping
    public ResponseEntity<GlobalResponse<Void>> deleteTutorRegistration(Principal principal) {
        try {
            UUID studentId = UUID.fromString(principal.getName());
            boolean deleted = tutorApplicationService.deleteApplicationByStudentId(studentId);
            
            if (deleted) {
                return ResponseEntity.ok(GlobalResponse.<Void>builder()
                        .code(HttpStatus.OK)
                        .success(true)
                        .message("Tutor application deleted successfully.")
                        .data(null)
                        .build());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(GlobalResponse.<Void>builder()
                                .code(HttpStatus.NOT_FOUND)
                                .success(false)
                                .message("No tutor application found to delete.")
                                .data(null)
                                .build());
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(GlobalResponse.<Void>builder()
                    .code(HttpStatus.BAD_REQUEST)
                    .success(false)
                    .message("Invalid student ID format.")
                    .data(null)
                    .build());
        }
    }    
    
    private TutorApplicationResponse toResponse(TutorApplication application) {
        return TutorApplicationResponse.builder()
                .tutorApplicationId(application.getId())
                .studentId(application.getStudentId())
                .status(application.getStatus().name())
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }
}
