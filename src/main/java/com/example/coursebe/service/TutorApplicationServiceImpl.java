package com.example.coursebe.service;

import com.example.coursebe.model.TutorApplication;
import com.example.coursebe.repository.TutorApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of TutorApplicationService
 * Uses the State pattern for managing application status transitions
 */
@Service
public class TutorApplicationServiceImpl implements TutorApplicationService {

    private final TutorApplicationRepository tutorApplicationRepository;

    @Autowired
    public TutorApplicationServiceImpl(TutorApplicationRepository tutorApplicationRepository) {
        this.tutorApplicationRepository = tutorApplicationRepository;
    }

    @Override
    public List<TutorApplication> getAllApplications() {
        return tutorApplicationRepository.findAll();
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
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        Optional<TutorApplication> appOpt = tutorApplicationRepository.findTopByStudentIdOrderByCreatedAtDesc(studentId);
        if (appOpt.isPresent()) {
            tutorApplicationRepository.deleteById(appOpt.get().getId());
            return true;
        }
        return false;
    }
}