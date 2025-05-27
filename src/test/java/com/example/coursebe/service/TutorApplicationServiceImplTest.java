package com.example.coursebe.service;

import com.example.coursebe.model.TutorApplication;
import com.example.coursebe.repository.TutorApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TutorApplicationServiceImplTest {

    @Mock
    private TutorApplicationRepository tutorApplicationRepository;

    @InjectMocks
    private TutorApplicationServiceImpl tutorApplicationService;

    private UUID applicationId;
    private UUID studentId;
    private TutorApplication testApplication;
    private List<TutorApplication> testApplications;

    @BeforeEach
    void setUp() {
        applicationId = UUID.randomUUID();
        studentId = UUID.randomUUID();
        
        testApplication = new TutorApplication(studentId);
        // Set application ID and other fields using reflection
        try {
            java.lang.reflect.Field idField = TutorApplication.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testApplication, applicationId);
            
            java.lang.reflect.Field createdAtField = TutorApplication.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(testApplication, LocalDateTime.now().minusDays(1));
        } catch (Exception e) {
            fail("Failed to set application fields");
        }
        
        UUID otherStudentId = UUID.randomUUID();
        TutorApplication application2 = new TutorApplication(otherStudentId);
        application2.setStatus(TutorApplication.Status.DENIED);
        try {
            java.lang.reflect.Field idField = TutorApplication.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(application2, UUID.randomUUID());
            
            java.lang.reflect.Field createdAtField = TutorApplication.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(application2, LocalDateTime.now().minusDays(2));
        } catch (Exception e) {
            fail("Failed to set application fields");
        }
        
        testApplications = Arrays.asList(testApplication, application2);
    }

    @Test
    @DisplayName("Should get all applications")
    void getAllApplications() {
        // Given
        when(tutorApplicationRepository.findAll()).thenReturn(testApplications);
        
        // When
        List<TutorApplication> result = tutorApplicationService.getAllApplications();
        
        // Then
        assertEquals(2, result.size());
        assertEquals(testApplications, result);
        verify(tutorApplicationRepository).findAll();
    }

    @Test
    @DisplayName("Should get applications by status")
    void getApplicationsByStatus() {
        // Given
        TutorApplication.Status status = TutorApplication.Status.PENDING;
        when(tutorApplicationRepository.findByStatus(status))
            .thenReturn(Collections.singletonList(testApplication));
        
        // When
        List<TutorApplication> result = tutorApplicationService.getApplicationsByStatus(status);
        
        // Then
        assertEquals(1, result.size());
        assertEquals(testApplication, result.get(0));
        verify(tutorApplicationRepository).findByStatus(status);
    }

    @Test
    @DisplayName("Should get applications by student ID")
    void getApplicationsByStudentId() {
        // Given
        when(tutorApplicationRepository.findByStudentId(studentId))
            .thenReturn(Collections.singletonList(testApplication));
        
        // When
        List<TutorApplication> result = tutorApplicationService.getApplicationsByStudentId(studentId);
        
        // Then
        assertEquals(1, result.size());
        assertEquals(testApplication, result.get(0));
        verify(tutorApplicationRepository).findByStudentId(studentId);
    }

    @Test
    @DisplayName("Should get most recent application by student ID")
    void getMostRecentApplicationByStudentId() {
        // Given
        when(tutorApplicationRepository.findTopByStudentIdOrderByCreatedAtDesc(studentId))
            .thenReturn(Optional.of(testApplication));
        
        // When
        Optional<TutorApplication> result = tutorApplicationService.getMostRecentApplicationByStudentId(studentId);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(testApplication, result.get());
        verify(tutorApplicationRepository).findTopByStudentIdOrderByCreatedAtDesc(studentId);
    }

    @Test
    @DisplayName("Should check if student has pending application")
    void hasPendingApplication() {
        // Given
        when(tutorApplicationRepository.existsByStudentIdAndStatus(studentId, TutorApplication.Status.PENDING))
            .thenReturn(true);
        
        // When
        boolean result = tutorApplicationService.hasPendingApplication(studentId);
        
        // Then
        assertTrue(result);
        verify(tutorApplicationRepository).existsByStudentIdAndStatus(studentId, TutorApplication.Status.PENDING);
    }

    @Test
    @DisplayName("Should submit application")
    void submitApplication() {
        // Given
        when(tutorApplicationRepository.existsByStudentIdAndStatus(studentId, TutorApplication.Status.PENDING))
            .thenReturn(false);
        when(tutorApplicationRepository.save(any(TutorApplication.class))).thenAnswer(i -> {
            TutorApplication application = (TutorApplication) i.getArguments()[0];
            // Set application ID using reflection
            try {
                java.lang.reflect.Field field = TutorApplication.class.getDeclaredField("id");
                field.setAccessible(true);
                field.set(application, UUID.randomUUID());
            } catch (Exception e) {
                fail("Failed to set application ID");
            }
            return application;
        });
        
        // When
        TutorApplication result = tutorApplicationService.submitApplication(studentId);
        
        // Then
        assertNotNull(result);
        assertEquals(studentId, result.getStudentId());
        assertEquals(TutorApplication.Status.PENDING, result.getStatus());
        verify(tutorApplicationRepository).existsByStudentIdAndStatus(studentId, TutorApplication.Status.PENDING);
        verify(tutorApplicationRepository).save(any(TutorApplication.class));
    }

    @Test
    @DisplayName("Should return null when student already has pending application")
    void submitApplicationAlreadyPending() {
        // Given
        when(tutorApplicationRepository.existsByStudentIdAndStatus(studentId, TutorApplication.Status.PENDING))
            .thenReturn(true);
        
        // When
        TutorApplication result = tutorApplicationService.submitApplication(studentId);
        
        // Then
        assertNull(result);
        verify(tutorApplicationRepository).existsByStudentIdAndStatus(studentId, TutorApplication.Status.PENDING);
        verify(tutorApplicationRepository, never()).save(any(TutorApplication.class));
    }

    @Test
    @DisplayName("Should update application status")
    void updateApplicationStatus() {
        // Given
        TutorApplication.Status newStatus = TutorApplication.Status.ACCEPTED;
        when(tutorApplicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));
        when(tutorApplicationRepository.save(any(TutorApplication.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // When
        Optional<TutorApplication> result = tutorApplicationService.updateApplicationStatus(applicationId, newStatus);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(newStatus, result.get().getStatus());
        verify(tutorApplicationRepository).findById(applicationId);
        verify(tutorApplicationRepository).save(any(TutorApplication.class));
    }

    @Test
    @DisplayName("Should return empty optional when updating status for non-existent application")
    void updateApplicationStatusNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        TutorApplication.Status newStatus = TutorApplication.Status.ACCEPTED;
        when(tutorApplicationRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        
        // When
        Optional<TutorApplication> result = tutorApplicationService.updateApplicationStatus(nonExistentId, newStatus);
        
        // Then
        assertFalse(result.isPresent());
        verify(tutorApplicationRepository).findById(nonExistentId);
        verify(tutorApplicationRepository, never()).save(any(TutorApplication.class));
    }

    @Test
    @DisplayName("Should throw exception for invalid state transition")
    void updateApplicationStatusInvalidTransition() {
        // Given
        // Create an application that's already accepted
        TutorApplication acceptedApplication = new TutorApplication(studentId);
        acceptedApplication.setStatus(TutorApplication.Status.ACCEPTED);
        try {
            java.lang.reflect.Field field = TutorApplication.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(acceptedApplication, applicationId);
        } catch (Exception e) {
            fail("Failed to set application ID");
        }
        
        when(tutorApplicationRepository.findById(applicationId)).thenReturn(Optional.of(acceptedApplication));
        
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            tutorApplicationService.updateApplicationStatus(applicationId, TutorApplication.Status.DENIED);
        });
        
        assertTrue(exception.getMessage().contains("Invalid state transition"));
        verify(tutorApplicationRepository).findById(applicationId);
        verify(tutorApplicationRepository, never()).save(any(TutorApplication.class));
    }

    @Test
    @DisplayName("Should delete application")
    void deleteApplication() {
        // Given
        when(tutorApplicationRepository.existsById(applicationId)).thenReturn(true);
        
        // When
        boolean result = tutorApplicationService.deleteApplication(applicationId);
        
        // Then
        assertTrue(result);
        verify(tutorApplicationRepository).existsById(applicationId);
        verify(tutorApplicationRepository).deleteById(applicationId);
    }

    @Test
    @DisplayName("Should return false when deleting non-existent application")
    void deleteNonExistentApplication() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(tutorApplicationRepository.existsById(nonExistentId)).thenReturn(false);
        
        // When
        boolean result = tutorApplicationService.deleteApplication(nonExistentId);
        
        // Then
        assertFalse(result);
        verify(tutorApplicationRepository).existsById(nonExistentId);
        verify(tutorApplicationRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    @DisplayName("Should delete most recent application by student ID")
    void deleteApplicationByStudentId_success() {
        // Given
        when(tutorApplicationRepository.deleteTopByStudentIdOrderByCreatedAtDesc(studentId)).thenReturn(1); // Simulate 1 row deleted

        // When
        boolean result = tutorApplicationService.deleteApplicationByStudentId(studentId);

        // Then
        assertTrue(result);
        verify(tutorApplicationRepository).deleteTopByStudentIdOrderByCreatedAtDesc(studentId); // Verify the correct method is called
    }

    @Test
    @DisplayName("Should return false when deleting application by student ID if not found")
    void deleteApplicationByStudentId_notFound() {
        // Given
        when(tutorApplicationRepository.deleteTopByStudentIdOrderByCreatedAtDesc(studentId)).thenReturn(0); // Simulate 0 rows deleted

        // When
        boolean result = tutorApplicationService.deleteApplicationByStudentId(studentId);

        // Then
        assertFalse(result);
        verify(tutorApplicationRepository).deleteTopByStudentIdOrderByCreatedAtDesc(studentId);
    }

    @Test
    @DisplayName("Should get all applications asynchronously")
    void getAllApplicationsAsync() throws Exception {
        // Given
        when(tutorApplicationRepository.findAll()).thenReturn(testApplications);
        
        // When
        CompletableFuture<List<TutorApplication>> future = tutorApplicationService.getAllApplicationsAsync();
        List<TutorApplication> result = future.get();
        
        // Then
        assertEquals(2, result.size());
        assertEquals(testApplications, result);
        verify(tutorApplicationRepository).findAll();
    }

    @Test
    @DisplayName("Should get applications by status asynchronously")
    void getApplicationsByStatusAsync() throws Exception {
        // Given
        TutorApplication.Status status = TutorApplication.Status.PENDING;
        when(tutorApplicationRepository.findByStatus(status))
            .thenReturn(Collections.singletonList(testApplication));
        
        // When
        CompletableFuture<List<TutorApplication>> future = tutorApplicationService.getApplicationsByStatusAsync(status);
        List<TutorApplication> result = future.get();
        
        // Then
        assertEquals(1, result.size());
        assertEquals(testApplication, result.get(0));
        verify(tutorApplicationRepository).findByStatus(status);
    }

    @Test
    @DisplayName("Should throw exception when getting applications by null status asynchronously")
    void getApplicationsByStatusAsync_nullStatus() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            tutorApplicationService.getApplicationsByStatusAsync(null);
        });
    }

    @Test
    @DisplayName("Should get most recent application by student ID asynchronously")
    void getMostRecentApplicationByStudentIdAsync() throws Exception {
        // Given
        when(tutorApplicationRepository.findTopByStudentIdOrderByCreatedAtDesc(studentId))
            .thenReturn(Optional.of(testApplication));
        
        // When
        CompletableFuture<Optional<TutorApplication>> future = tutorApplicationService.getMostRecentApplicationByStudentIdAsync(studentId);
        Optional<TutorApplication> result = future.get();
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(testApplication, result.get());
        verify(tutorApplicationRepository).findTopByStudentIdOrderByCreatedAtDesc(studentId);
    }

    @Test
    @DisplayName("Should submit application asynchronously")
    void submitApplicationAsync() throws Exception {
        // Given
        when(tutorApplicationRepository.existsByStudentIdAndStatus(studentId, TutorApplication.Status.PENDING))
            .thenReturn(false);
        when(tutorApplicationRepository.save(any(TutorApplication.class))).thenAnswer(i -> {
            TutorApplication application = (TutorApplication) i.getArguments()[0];
            try {
                java.lang.reflect.Field field = TutorApplication.class.getDeclaredField("id");
                field.setAccessible(true);
                field.set(application, UUID.randomUUID());
            } catch (Exception e) {
                fail("Failed to set application ID");
            }
            return application;
        });
        
        // When
        CompletableFuture<TutorApplication> future = tutorApplicationService.submitApplicationAsync(studentId);
        TutorApplication result = future.get();
        
        // Then
        assertNotNull(result);
        assertEquals(studentId, result.getStudentId());
        assertEquals(TutorApplication.Status.PENDING, result.getStatus());
        verify(tutorApplicationRepository).existsByStudentIdAndStatus(studentId, TutorApplication.Status.PENDING);
        verify(tutorApplicationRepository).save(any(TutorApplication.class));
    }

    @Test
    @DisplayName("Should update application status asynchronously")
    void updateApplicationStatusAsync() throws Exception {
        // Given
        TutorApplication.Status newStatus = TutorApplication.Status.ACCEPTED;
        when(tutorApplicationRepository.findById(applicationId)).thenReturn(Optional.of(testApplication));
        when(tutorApplicationRepository.save(any(TutorApplication.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // When
        CompletableFuture<Optional<TutorApplication>> future = tutorApplicationService.updateApplicationStatusAsync(applicationId, newStatus);
        Optional<TutorApplication> result = future.get();
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(newStatus, result.get().getStatus());
        verify(tutorApplicationRepository).findById(applicationId);
        verify(tutorApplicationRepository).save(any(TutorApplication.class));
    }

    @Test
    @DisplayName("Should delete application by student ID asynchronously")
    void deleteApplicationByStudentIdAsync() throws Exception {
        // Given
        when(tutorApplicationRepository.deleteTopByStudentIdOrderByCreatedAtDesc(studentId))
            .thenReturn(1L);

        // When
        CompletableFuture<Boolean> future = tutorApplicationService.deleteApplicationByStudentIdAsync(studentId);
        boolean result = future.get();

        // Then
        assertTrue(result);
        verify(tutorApplicationRepository).deleteTopByStudentIdOrderByCreatedAtDesc(studentId);
    }

    @Test
    @DisplayName("Should handle exception in delete application by student ID asynchronously")
    void deleteApplicationByStudentIdAsync_exception() throws Exception {
        // Given
        when(tutorApplicationRepository.deleteTopByStudentIdOrderByCreatedAtDesc(studentId))
            .thenThrow(new RuntimeException("Database error"));

        // When
        CompletableFuture<Boolean> future = tutorApplicationService.deleteApplicationByStudentIdAsync(studentId);

        // Then
        assertThrows(Exception.class, () -> future.get());
        verify(tutorApplicationRepository).deleteTopByStudentIdOrderByCreatedAtDesc(studentId);
    }

    @Test
    @DisplayName("Should delete all applications by student ID")
    void deleteAllApplicationsByStudentId() {
        // Given
        when(tutorApplicationRepository.deleteByStudentId(studentId)).thenReturn(2);

        // When
        int result = tutorApplicationService.deleteAllApplicationsByStudentId(studentId);

        // Then
        assertEquals(2, result);
        verify(tutorApplicationRepository).deleteByStudentId(studentId);
    }

    @Test
    @DisplayName("Should handle exception in delete all applications by student ID")
    void deleteAllApplicationsByStudentId_exception() {
        // Given
        when(tutorApplicationRepository.deleteByStudentId(studentId))
            .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            tutorApplicationService.deleteAllApplicationsByStudentId(studentId);
        });
        verify(tutorApplicationRepository).deleteByStudentId(studentId);
    }

    @Test
    @DisplayName("Should throw exception when getting applications by null status")
    void getApplicationsByStatus_nullStatus() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            tutorApplicationService.getApplicationsByStatus(null);
        });
    }

    @Test
    @DisplayName("Should throw exception when getting applications by null student ID")
    void getApplicationsByStudentId_nullStudentId() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            tutorApplicationService.getApplicationsByStudentId(null);
        });
    }

    @Test
    @DisplayName("Should throw exception when getting most recent application by null student ID")
    void getMostRecentApplicationByStudentId_nullStudentId() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            tutorApplicationService.getMostRecentApplicationByStudentId(null);
        });
    }

    @Test
    @DisplayName("Should throw exception when getting most recent application by null student ID asynchronously")
    void getMostRecentApplicationByStudentIdAsync_nullStudentId() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            tutorApplicationService.getMostRecentApplicationByStudentIdAsync(null);
        });
    }

    @Test
    @DisplayName("Should throw exception when checking pending application with null student ID")
    void hasPendingApplication_nullStudentId() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            tutorApplicationService.hasPendingApplication(null);
        });
    }

    @Test
    @DisplayName("Should throw exception when submitting application with null student ID")
    void submitApplication_nullStudentId() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            tutorApplicationService.submitApplication(null);
        });
    }

    @Test
    @DisplayName("Should throw exception when updating application status with null ID")
    void updateApplicationStatus_nullId() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            tutorApplicationService.updateApplicationStatus(null, TutorApplication.Status.ACCEPTED);
        });
    }

    @Test
    @DisplayName("Should throw exception when updating application status with null status")
    void updateApplicationStatus_nullStatus() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            tutorApplicationService.updateApplicationStatus(applicationId, null);
        });
    }

    @Test
    @DisplayName("Should throw exception when deleting application with null ID")
    void deleteApplication_nullId() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            tutorApplicationService.deleteApplication(null);
        });
    }

    @Test
    @DisplayName("Should throw exception when deleting application by null student ID")
    void deleteApplicationByStudentId_nullStudentId() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            tutorApplicationService.deleteApplicationByStudentId(null);
        });
    }

    @Test
    @DisplayName("Should throw exception when deleting all applications by null student ID")
    void deleteAllApplicationsByStudentId_nullStudentId() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            tutorApplicationService.deleteAllApplicationsByStudentId(null);
        });
    }

    @Test
    @DisplayName("Should validate state transition from PENDING to ACCEPTED")
    void isValidStateTransition_pendingToAccepted() {
        // Given
        TutorApplication application = new TutorApplication(studentId);
        application.setStatus(TutorApplication.Status.PENDING);
        when(tutorApplicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        when(tutorApplicationRepository.save(any(TutorApplication.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // When
        Optional<TutorApplication> result = tutorApplicationService.updateApplicationStatus(
            application.getId(), TutorApplication.Status.ACCEPTED);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(TutorApplication.Status.ACCEPTED, result.get().getStatus());
        verify(tutorApplicationRepository).findById(application.getId());
        verify(tutorApplicationRepository).save(any(TutorApplication.class));
    }

    @Test
    @DisplayName("Should validate state transition from PENDING to DENIED")
    void isValidStateTransition_pendingToDenied() {
        // Given
        TutorApplication application = new TutorApplication(studentId);
        application.setStatus(TutorApplication.Status.PENDING);
        when(tutorApplicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        when(tutorApplicationRepository.save(any(TutorApplication.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // When
        Optional<TutorApplication> result = tutorApplicationService.updateApplicationStatus(
            application.getId(), TutorApplication.Status.DENIED);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(TutorApplication.Status.DENIED, result.get().getStatus());
        verify(tutorApplicationRepository).findById(application.getId());
        verify(tutorApplicationRepository).save(any(TutorApplication.class));
    }
    
    @Test
    @DisplayName("Should reject state transition from ACCEPTED to PENDING")
    void isValidStateTransition_acceptedToPending() {
        // Given
        TutorApplication application = new TutorApplication(studentId);
        application.setStatus(TutorApplication.Status.ACCEPTED);
        when(tutorApplicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        
        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            tutorApplicationService.updateApplicationStatus(application.getId(), TutorApplication.Status.PENDING);
        });
    }

    @Test
    @DisplayName("Should reject state transition from DENIED to ACCEPTED")
    void isValidStateTransition_deniedToAccepted() {
        // Given
        TutorApplication application = new TutorApplication(studentId);
        application.setStatus(TutorApplication.Status.DENIED);
        when(tutorApplicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        
        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            tutorApplicationService.updateApplicationStatus(application.getId(), TutorApplication.Status.ACCEPTED);
        });
    }

    @Test
    @DisplayName("Should handle exception in delete application by student ID")
    void deleteApplicationByStudentId_exception() {
        // Given
        when(tutorApplicationRepository.deleteTopByStudentIdOrderByCreatedAtDesc(studentId))
            .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            tutorApplicationService.deleteApplicationByStudentId(studentId);
        });
    }
}