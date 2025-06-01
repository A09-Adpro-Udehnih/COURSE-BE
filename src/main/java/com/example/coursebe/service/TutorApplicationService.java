package com.example.coursebe.service;

import com.example.coursebe.model.TutorApplication;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface TutorApplicationService {
    List<TutorApplication> getAllApplications();
    CompletableFuture<List<TutorApplication>> getAllApplicationsAsync();
    List<TutorApplication> getApplicationsByStatus(TutorApplication.Status status);
    CompletableFuture<List<TutorApplication>> getApplicationsByStatusAsync(TutorApplication.Status status);
    List<TutorApplication> getApplicationsByStudentId(UUID studentId);
    Optional<TutorApplication> getMostRecentApplicationByStudentId(UUID studentId);
    CompletableFuture<Optional<TutorApplication>> getMostRecentApplicationByStudentIdAsync(UUID studentId);
    TutorApplication findByTutorId(UUID tutorId);
    boolean hasPendingApplication(UUID studentId);
    TutorApplication submitApplication(UUID studentId);
    CompletableFuture<TutorApplication> submitApplicationAsync(UUID studentId);
    Optional<TutorApplication> updateApplicationStatus(UUID id, TutorApplication.Status status);
    CompletableFuture<Optional<TutorApplication>> updateApplicationStatusAsync(UUID id, TutorApplication.Status status);
    boolean deleteApplication(UUID id);
    boolean deleteApplicationByStudentId(UUID studentId);
    CompletableFuture<Boolean> deleteApplicationByStudentIdAsync(UUID studentId);
    int deleteAllApplicationsByStudentId(UUID studentId);
}