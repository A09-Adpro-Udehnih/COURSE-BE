package com.example.coursebe.controller;

import com.example.coursebe.dto.GlobalResponse;
import com.example.coursebe.dto.tutorapplication.TutorApplicationResponse;
import com.example.coursebe.dto.tutorapplication.TutorApplicationStatusResponse;
import com.example.coursebe.model.TutorApplication;
import com.example.coursebe.service.TutorApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.coursebe.util.AuthenticationUtil;


import java.security.Principal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/tutors/registration")
public class TutorApplicationController {
    private final TutorApplicationService tutorApplicationService;    public TutorApplicationController(TutorApplicationService tutorApplicationService) {
        this.tutorApplicationService = tutorApplicationService;
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<GlobalResponse<TutorApplicationResponse>>> registerAsTutorAsync(Principal principal) {
        UUID studentId = AuthenticationUtil.parseUserId(principal);
        return tutorApplicationService.submitApplicationAsync(studentId)
                .thenApply(app -> ResponseEntity.ok(GlobalResponse.success("Tutor application submitted asynchronously.", toResponse(app))))
                .exceptionally(ex -> ResponseEntity.badRequest().body(GlobalResponse.badRequest(ex.getMessage())));
    }

    @GetMapping
    public ResponseEntity<GlobalResponse<TutorApplicationStatusResponse>> getTutorRegistrationStatus(Principal principal) {
        UUID studentId = AuthenticationUtil.parseUserId(principal);
        TutorApplication app = tutorApplicationService.getMostRecentApplicationByStudentIdOrThrow(studentId);

        return ResponseEntity.ok(GlobalResponse.success("Tutor application status retrieved successfully.",
                TutorApplicationStatusResponse.builder()
                        .status(app.getStatus().name())
                        .tutorApplicationId(app.getId())
                        .build()));
    }

    @DeleteMapping
    public ResponseEntity<GlobalResponse<Void>> deleteTutorRegistration(Principal principal) {
        UUID studentId = AuthenticationUtil.parseUserId(principal);
        boolean deleted = tutorApplicationService.deleteApplicationByStudentId(studentId);
        
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(GlobalResponse.success("Tutor application deleted successfully.", null));
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
