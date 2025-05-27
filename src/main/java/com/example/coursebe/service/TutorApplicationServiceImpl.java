package com.example.coursebe.service;

import com.example.coursebe.model.TutorApplication;
import com.example.coursebe.repository.TutorApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of TutorApplicationService
 * Uses the State pattern for managing application status transitions
 * Implements asynchronous programming for enhanced performance
 * Optimized for database operations with single-query deletions
 */
@Service
public class TutorApplicationServiceImpl implements TutorApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(TutorApplicationServiceImpl.class);
    private final TutorApplicationRepository tutorApplicationRepository;

    public TutorApplicationServiceImpl(TutorApplicationRepository tutorApplicationRepository) {
        this.tutorApplicationRepository = tutorApplicationRepository;
    }

    @Override
    public List<TutorApplication> getAllApplications() {
        return tutorApplicationRepository.findAll();
    }
    
    @Override
    @Async
    public CompletableFuture<List<TutorApplication>> getAllApplicationsAsync() {
        return CompletableFuture.completedFuture(tutorApplicationRepository.findAll());
    }

    @Override
    public List<TutorApplication> getApplicationsByStatus(TutorApplication.Status status) {
        // Validate input
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        return tutorApplicationRepository.findByStatus(status);
    }
    
    @Override
    @Async
    public CompletableFuture<List<TutorApplication>> getApplicationsByStatusAsync(TutorApplication.Status status) {
        // Validate input
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        return CompletableFuture.completedFuture(tutorApplicationRepository.findByStatus(status));
    }

    @Override
    public List<TutorApplication> getApplicationsByStudentId(UUID studentId) {
        // Validate input
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        
        return tutorApplicationRepository.findByStudentId(studentId);
    }

    @Override
    public Optional<TutorApplication> getMostRecentApplicationByStudentId(UUID studentId) {
        // Validate input
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        
        return tutorApplicationRepository.findTopByStudentIdOrderByCreatedAtDesc(studentId);
    }
    
    @Override
    @Async
    public CompletableFuture<Optional<TutorApplication>> getMostRecentApplicationByStudentIdAsync(UUID studentId) {
        // Validate input
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        
        return CompletableFuture.completedFuture(
            tutorApplicationRepository.findTopByStudentIdOrderByCreatedAtDesc(studentId)
        );
    }

    @Override
    public boolean hasPendingApplication(UUID studentId) {
        // Validate input
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        
        return tutorApplicationRepository.existsByStudentIdAndStatus(
            studentId, TutorApplication.Status.PENDING);
    }

    @Override
    @Transactional
    public TutorApplication submitApplication(UUID studentId) {
        // Validate input
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        
        // Check if student already has a pending application
        if (hasPendingApplication(studentId)) {
            return null;
        }
        
        // Create and save new application using the State pattern
        // The initial state is PENDING (set in the constructor)
        TutorApplication application = new TutorApplication(studentId);
        return tutorApplicationRepository.save(application);
    }
    
    @Override
    @Async
    @Transactional
    public CompletableFuture<TutorApplication> submitApplicationAsync(UUID studentId) {
        return CompletableFuture.completedFuture(submitApplication(studentId));
    }

    @Override
    @Transactional
    public Optional<TutorApplication> updateApplicationStatus(UUID id, TutorApplication.Status status) {
        // Validate inputs
        if (id == null) {
            throw new IllegalArgumentException("Application ID cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        // Find application
        Optional<TutorApplication> optionalApplication = tutorApplicationRepository.findById(id);
        if (optionalApplication.isEmpty()) {
            return Optional.empty();
        }
        
        TutorApplication application = optionalApplication.get();
        
        // Apply state transition based on State pattern rules
        if (!isValidStateTransition(application.getStatus(), status)) {
            throw new IllegalStateException(
                "Invalid state transition from " + application.getStatus() + " to " + status);
        }
        
        // Update status
        application.setStatus(status);
        TutorApplication updatedApplication = tutorApplicationRepository.save(application);
        return Optional.of(updatedApplication);
    }
    
    @Override
    @Async
    @Transactional
    public CompletableFuture<Optional<TutorApplication>> updateApplicationStatusAsync(UUID id, TutorApplication.Status status) {
        return CompletableFuture.completedFuture(updateApplicationStatus(id, status));
    }

    /**
     * Implements the State pattern to determine valid transitions between application statuses
     */
    private boolean isValidStateTransition(TutorApplication.Status currentStatus, TutorApplication.Status newStatus) {
        if (currentStatus == null || newStatus == null) {
            return false;
        }
        
        // Define valid transitions using State pattern:
        switch (currentStatus) {
            case PENDING:
                // From PENDING, we can move to ACCEPTED or DENIED
                return newStatus == TutorApplication.Status.ACCEPTED 
                    || newStatus == TutorApplication.Status.DENIED;
            
            case ACCEPTED:
            case DENIED:
                // Once ACCEPTED or DENIED, no further transitions allowed
                return false;
                
            default:
                return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteApplication(UUID id) {
        // Validate input
        if (id == null) {
            throw new IllegalArgumentException("Application ID cannot be null");
        }
        
        // Check if application exists
        if (tutorApplicationRepository.existsById(id)) {
            tutorApplicationRepository.deleteById(id);
            return true;
        }
        
        return false;
    }
    
    @Override
    @Transactional
    public boolean deleteApplicationByStudentId(UUID studentId) {
        // Input validation
        if (studentId == null) {
            logger.warn("Attempted to delete application with null studentId");
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        
        logger.info("Attempting to delete most recent application for studentId: {}", studentId);
        
        try {
            // Optimized approach: Use custom repository method to delete in single query
            // This reduces database round trips from 2 operations to 1
            int deletedCount = tutorApplicationRepository.deleteTopByStudentIdOrderByCreatedAtDesc(studentId);
            
            // Log the operation for monitoring and debugging
            if (deletedCount > 0) {
                logger.info("Successfully deleted application for studentId: {}, deletedCount: {}", studentId, deletedCount);
                // Could add audit logging here for compliance
                return true;
            } else {
                logger.info("No application found to delete for studentId: {}", studentId);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error deleting application for studentId: {}", studentId, e);
            throw new RuntimeException("Failed to delete application", e);
        }
    }
    
    @Override
    @Transactional
    @Async
    public CompletableFuture<Boolean> deleteApplicationByStudentIdAsync(UUID studentId) {
        logger.info("Starting async deletion for studentId: {}", studentId);
        
        try {
            // Asynchronous version for non-blocking operations
            boolean result = deleteApplicationByStudentId(studentId);
            logger.info("Async deletion completed for studentId: {}, result: {}", studentId, result);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            logger.error("Async deletion failed for studentId: {}", studentId, e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    @Override
    @Transactional
    public int deleteAllApplicationsByStudentId(UUID studentId) {
        // Batch deletion method for cleanup operations
        if (studentId == null) {
            logger.warn("Attempted to delete all applications with null studentId");
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        
        logger.info("Attempting to delete all applications for studentId: {}", studentId);
        
        try {
            int deletedCount = tutorApplicationRepository.deleteByStudentId(studentId);
            logger.info("Successfully deleted all applications for studentId: {}, deletedCount: {}", studentId, deletedCount);
            return deletedCount;
        } catch (Exception e) {
            logger.error("Error deleting all applications for studentId: {}", studentId, e);
            throw new RuntimeException("Failed to delete all applications", e);
        }
    }
}