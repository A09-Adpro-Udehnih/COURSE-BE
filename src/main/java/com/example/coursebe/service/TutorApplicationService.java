package com.example.coursebe.service;

import com.example.coursebe.model.TutorApplication;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface TutorApplicationService {
    Optional<TutorApplication> getMostRecentApplicationByStudentId(UUID studentId);
    TutorApplication getMostRecentApplicationByStudentIdOrThrow(UUID studentId);
    TutorApplication findByTutorId(UUID tutorId);
    boolean hasPendingApplication(UUID studentId);
    boolean hasAnyApplication(UUID studentId);
    TutorApplication submitApplication(UUID studentId);
    CompletableFuture<TutorApplication> submitApplicationAsync(UUID studentId);
    Optional<TutorApplication> updateApplicationStatus(UUID id, TutorApplication.Status status);
    boolean deleteApplicationByStudentId(UUID studentId);
    void deleteApplicationByStudentIdOrThrow(UUID studentId);
}