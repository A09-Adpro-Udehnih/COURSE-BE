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

@Service
public class TutorApplicationServiceImpl implements TutorApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(TutorApplicationServiceImpl.class);
    private final TutorApplicationRepository tutorApplicationRepository;

    public TutorApplicationServiceImpl(TutorApplicationRepository tutorApplicationRepository) {
        this.tutorApplicationRepository = tutorApplicationRepository;
    }

    @Override
    public Optional<TutorApplication> getMostRecentApplicationByStudentId(UUID studentId) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        return tutorApplicationRepository.findTopByStudentIdOrderByCreatedAtDesc(studentId);
    }
    
    @Override
    public TutorApplication findByTutorId(UUID tutorId) {
        Optional<TutorApplication> optionalApplication = getMostRecentApplicationByStudentId(tutorId);
        return optionalApplication.orElse(null);
    }

    @Override
    public boolean hasPendingApplication(UUID studentId) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        return tutorApplicationRepository.existsByStudentIdAndStatus(
            studentId, TutorApplication.Status.PENDING);
    }
    
    @Override
    public boolean hasAnyApplication(UUID studentId) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        return !tutorApplicationRepository.findByStudentId(studentId).isEmpty();
    }    
    
    @Override
    @Transactional
    public TutorApplication submitApplication(UUID studentId) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        if (hasAnyApplication(studentId)) {
            return null;
        }
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
        
        // Apply state transition
        if (!isValidStateTransition(application.getStatus(), status)) {
            throw new IllegalStateException(
                "Invalid state transition from " + application.getStatus() + " to " + status);
        }
        
        // Update status
        application.setStatus(status);
        TutorApplication updatedApplication = tutorApplicationRepository.save(application);
        return Optional.of(updatedApplication);
    }

    private boolean isValidStateTransition(TutorApplication.Status currentStatus, TutorApplication.Status newStatus) {
        if (currentStatus == null || newStatus == null) {
            return false;
        }

        switch (currentStatus) {
            case PENDING:
                return newStatus == TutorApplication.Status.ACCEPTED
                    || newStatus == TutorApplication.Status.DENIED;
            case ACCEPTED:
            case DENIED:
                return false;
                
            default:
                return false;
        }
    }
    
    @Override
    @Transactional
    public boolean deleteApplicationByStudentId(UUID studentId) {
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
}