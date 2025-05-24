package com.example.coursebe.controller;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import com.example.coursebe.model.TutorApplication;
import com.example.coursebe.service.TutorApplicationService;

class TutorApplicationControllerTest {
    @Mock
    private TutorApplicationService tutorApplicationService;

    @InjectMocks
    private TutorApplicationController controller;

    private Principal principal;
    private UUID studentId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        studentId = UUID.randomUUID();
        principal = mock(Principal.class);
        when(principal.getName()).thenReturn(studentId.toString());
    }

    @Test
    @DisplayName("POST /tutors/registration - success")
    void registerAsTutor_success() throws ExecutionException, InterruptedException {
        when(tutorApplicationService.hasPendingApplication(studentId)).thenReturn(false);
        
        TutorApplication app = new TutorApplication(studentId);
        when(tutorApplicationService.submitApplicationAsync(studentId))
            .thenReturn(CompletableFuture.completedFuture(app));

        ResponseEntity<?> response = controller.registerAsTutor(principal).get();
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("success=true"));
        verify(tutorApplicationService).submitApplicationAsync(studentId);
    }

    @Test
    @DisplayName("POST /tutors/registration - already pending")
    void registerAsTutor_alreadyPending() throws ExecutionException, InterruptedException {
        when(tutorApplicationService.hasPendingApplication(studentId)).thenReturn(true);

        ResponseEntity<?> response = controller.registerAsTutor(principal).get();
        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("pending tutor application"));
        verify(tutorApplicationService, never()).submitApplicationAsync(any());
    }

    @Test
    @DisplayName("GET /tutors/registration - has application")
    void getTutorRegistrationStatus_hasApplication() throws ExecutionException, InterruptedException {
        TutorApplication app = new TutorApplication(studentId);
        
        when(tutorApplicationService.getMostRecentApplicationByStudentIdAsync(studentId))
            .thenReturn(CompletableFuture.completedFuture(Optional.of(app)));

        ResponseEntity<?> response = controller.getTutorRegistrationStatus(principal).get();
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("PENDING"));
        assertTrue(response.getBody().toString().contains("tutorApplicationId"));
        verify(tutorApplicationService).getMostRecentApplicationByStudentIdAsync(studentId);
    }

    @Test
    @DisplayName("GET /tutors/registration - no application")
    void getTutorRegistrationStatus_noApplication() throws ExecutionException, InterruptedException {
        when(tutorApplicationService.getMostRecentApplicationByStudentIdAsync(studentId))
            .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        ResponseEntity<?> response = controller.getTutorRegistrationStatus(principal).get();
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("No tutor application found"));
        verify(tutorApplicationService).getMostRecentApplicationByStudentIdAsync(studentId);
    }

    @Test
    @DisplayName("DELETE /tutors/registration - success")
    void deleteTutorRegistration_success() {
        when(tutorApplicationService.deleteApplicationByStudentId(studentId)).thenReturn(true);

        ResponseEntity<?> response = controller.deleteTutorRegistration(principal);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("deleted successfully"));
        verify(tutorApplicationService).deleteApplicationByStudentId(studentId);
    }

    @Test
    @DisplayName("DELETE /tutors/registration - not found")
    void deleteTutorRegistration_notFound() {
        when(tutorApplicationService.deleteApplicationByStudentId(studentId)).thenReturn(false);

        ResponseEntity<?> response = controller.deleteTutorRegistration(principal);
        assertEquals(404, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("No tutor application found"));
        verify(tutorApplicationService).deleteApplicationByStudentId(studentId);
    }
}
