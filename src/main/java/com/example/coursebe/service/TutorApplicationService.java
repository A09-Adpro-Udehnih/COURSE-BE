package com.example.coursebe.service;

import com.example.coursebe.model.TutorApplication;
import jakarta.validation.constraints.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface TutorApplicationService {
    Optional<TutorApplication> getMostRecentApplicationByStudentId(@NotNull UUID studentId);
    TutorApplication getMostRecentApplicationByStudentIdOrThrow(@NotNull UUID studentId);
    Optional<TutorApplication> findByTutorId(@NotNull UUID tutorId);
    boolean hasPendingApplication(@NotNull UUID studentId);
    boolean hasAnyApplication(@NotNull UUID studentId);
    TutorApplication submitApplication(@NotNull UUID studentId);
    CompletableFuture<TutorApplication> submitApplicationAsync(@NotNull UUID studentId);
    Optional<TutorApplication> updateApplicationStatus(@NotNull UUID id, @NotNull TutorApplication.Status status);
    boolean deleteApplicationByStudentId(@NotNull UUID studentId);
}