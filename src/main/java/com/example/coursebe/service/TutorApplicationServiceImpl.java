package com.example.coursebe.service;

import com.example.coursebe.exception.ApplicationExistsException;
import com.example.coursebe.exception.ApplicationNotFoundException;
import com.example.coursebe.model.TutorApplication;
import com.example.coursebe.repository.TutorApplicationRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Validated
public class TutorApplicationServiceImpl implements TutorApplicationService {

    private final TutorApplicationRepository tutorApplicationRepository;

    public TutorApplicationServiceImpl(TutorApplicationRepository tutorApplicationRepository) {
        this.tutorApplicationRepository = tutorApplicationRepository;
    }

    @Override
    public Optional<TutorApplication> getMostRecentApplicationByStudentId(@NotNull UUID studentId) {
        return tutorApplicationRepository.findTopByStudentIdOrderByCreatedAtDesc(studentId);
    }

    @Override
    public TutorApplication getMostRecentApplicationByStudentIdOrThrow(@NotNull UUID studentId) {
        return tutorApplicationRepository.findTopByStudentIdOrderByCreatedAtDesc(studentId)
                .orElseThrow(() -> new ApplicationNotFoundException("No tutor application found."));
    }    
    
    @Override
    public Optional<TutorApplication> findByTutorId(@NotNull UUID tutorId) {
        return getMostRecentApplicationByStudentId(tutorId);
    }

    @Override
    public boolean hasPendingApplication(@NotNull UUID studentId) {
        return tutorApplicationRepository.existsByStudentIdAndStatus(studentId, TutorApplication.Status.PENDING);
    }

    @Override
    public boolean hasAnyApplication(@NotNull UUID studentId) {
        return tutorApplicationRepository.existsByStudentId(studentId);
    }

    @Override
    @Transactional
    public TutorApplication submitApplication(@NotNull UUID studentId) {
        if (hasAnyApplication(studentId)) {
            throw new ApplicationExistsException("You already have a tutor application.");
        }
        TutorApplication application = new TutorApplication(studentId);
        return tutorApplicationRepository.save(application);
    }    
    
    @Override
    @Async("tutorApplicationExecutor")
    public CompletableFuture<TutorApplication> submitApplicationAsync(@NotNull UUID studentId) {
        if (tutorApplicationRepository.existsByStudentId(studentId)) {
            return CompletableFuture.failedFuture(new ApplicationExistsException("You already have a tutor application."));
        }
        TutorApplication application = new TutorApplication(studentId);
        TutorApplication savedApplication = tutorApplicationRepository.save(application);
        return CompletableFuture.completedFuture(savedApplication);
    }

    @Override
    @Transactional
    public Optional<TutorApplication> updateApplicationStatus(@NotNull UUID id, @NotNull TutorApplication.Status status) {
        Optional<TutorApplication> optionalApplication = tutorApplicationRepository.findById(id);
        if (optionalApplication.isEmpty()) {
            return Optional.empty();
        }

        TutorApplication application = optionalApplication.get();

        if (!isValidStateTransition(application.getStatus(), status)) {
            throw new IllegalStateException(
                "Invalid state transition from " + application.getStatus() + " to " + status);
        }

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
    public boolean deleteApplicationByStudentId(@NotNull UUID studentId) {
        int deletedCount = tutorApplicationRepository.deleteTopByStudentIdOrderByCreatedAtDesc(studentId);
        return deletedCount > 0;
    }
}