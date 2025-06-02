package com.example.coursebe.controller;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

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
    @DisplayName("GET /tutors/registration - has application")
    void getTutorRegistrationStatus_hasApplication() {
        TutorApplication app = new TutorApplication(studentId);
        
        when(tutorApplicationService.getMostRecentApplicationByStudentId(studentId)).thenReturn(Optional.of(app));

        ResponseEntity<?> response = controller.getTutorRegistrationStatus(principal);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().toString().contains("PENDING"));
        assertTrue(response.getBody().toString().contains("tutorApplicationId"));
        verify(tutorApplicationService).getMostRecentApplicationByStudentId(studentId);
    }

    @Test
    @DisplayName("GET /tutors/registration - no application")
    void getTutorRegistrationStatus_noApplication() {
        when(tutorApplicationService.getMostRecentApplicationByStudentId(studentId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getTutorRegistrationStatus(principal);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().toString().contains("No tutor application found"));
        verify(tutorApplicationService).getMostRecentApplicationByStudentId(studentId);
    }

    @Test
    @DisplayName("DELETE /tutors/registration - success")
    void deleteTutorRegistration_success() {
        when(tutorApplicationService.deleteApplicationByStudentId(studentId)).thenReturn(true);

        ResponseEntity<?> response = controller.deleteTutorRegistration(principal);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().toString().contains("deleted successfully"));
        verify(tutorApplicationService).deleteApplicationByStudentId(studentId);
    }

    @Test
    @DisplayName("DELETE /tutors/registration - not found")
    void deleteTutorRegistration_notFound() {
        when(tutorApplicationService.deleteApplicationByStudentId(studentId)).thenReturn(false);

        ResponseEntity<?> response = controller.deleteTutorRegistration(principal);
        assertEquals(404, response.getStatusCode().value());
        assertTrue(response.getBody().toString().contains("No tutor application found"));
        verify(tutorApplicationService).deleteApplicationByStudentId(studentId);
    }
}
