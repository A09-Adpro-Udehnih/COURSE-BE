package com.example.coursebe.service;

import com.example.coursebe.model.TutorApplication;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for managing TutorApplication entities
 */
public interface TutorApplicationService {
    
    /**
     * Get all tutor applications
     * @return List of all tutor applications
     */
    List<TutorApplication> getAllApplications();
    
    /**
     * Get all tutor applications asynchronously
     * @return CompletableFuture of list of all tutor applications
     */
    CompletableFuture<List<TutorApplication>> getAllApplicationsAsync();
    
    /**
     * Get all tutor applications with a specific status
     * @param status Application status
     * @return List of applications with the specified status
     */
    List<TutorApplication> getApplicationsByStatus(TutorApplication.Status status);
    
    /**
     * Get all tutor applications with a specific status asynchronously
     * @param status Application status
     * @return CompletableFuture of list of applications with the specified status
     */
    CompletableFuture<List<TutorApplication>> getApplicationsByStatusAsync(TutorApplication.Status status);
    
    /**
     * Get all applications for a student
     * @param studentId Student ID
     * @return List of applications submitted by the student
     */
    List<TutorApplication> getApplicationsByStudentId(UUID studentId);
    
    /**
     * Get the most recent application for a student
     * @param studentId Student ID
     * @return Optional containing the most recent application if it exists
     */
    Optional<TutorApplication> getMostRecentApplicationByStudentId(UUID studentId);
    
    /**
     * Get the most recent application for a student asynchronously
     * @param studentId Student ID
     * @return CompletableFuture of optional containing the most recent application if it exists
     */
    CompletableFuture<Optional<TutorApplication>> getMostRecentApplicationByStudentIdAsync(UUID studentId);
    
    /**
     * Check if a student has a pending application
     * @param studentId Student ID
     * @return true if the student has a pending application, false otherwise
     */
    boolean hasPendingApplication(UUID studentId);
    
    /**
     * Submit a new tutor application
     * @param studentId Student ID
     * @return Created application or null if the student already has a pending application
     */
    TutorApplication submitApplication(UUID studentId);
    
    /**
     * Submit a new tutor application asynchronously
     * @param studentId Student ID
     * @return CompletableFuture of created application or null if the student already has a pending application
     */
    CompletableFuture<TutorApplication> submitApplicationAsync(UUID studentId);
    
    /**
     * Update the status of an application
     * @param id Application ID
     * @param status New status
     * @return Updated application or empty optional if not found
     */
    Optional<TutorApplication> updateApplicationStatus(UUID id, TutorApplication.Status status);
    
    /**
     * Update the status of an application asynchronously
     * @param id Application ID
     * @param status New status
     * @return CompletableFuture of updated application or empty optional if not found
     */
    CompletableFuture<Optional<TutorApplication>> updateApplicationStatusAsync(UUID id, TutorApplication.Status status);
    
    /**
     * Delete an application
     * @param id Application ID
     * @return true if deleted, false if not found
     */
    boolean deleteApplication(UUID id);
      /**
     * Delete the most recent application by studentId
     * @param studentId Student ID
     * @return true if deleted, false if not found
     */
    boolean deleteApplicationByStudentId(UUID studentId);
    
    /**
     * Delete the most recent application by studentId asynchronously
     * @param studentId Student ID
     * @return CompletableFuture of true if deleted, false if not found
     */
    CompletableFuture<Boolean> deleteApplicationByStudentIdAsync(UUID studentId);
    
    /**
     * Delete all applications by studentId (for cleanup operations)
     * @param studentId Student ID
     * @return number of deleted applications
     */
    int deleteAllApplicationsByStudentId(UUID studentId);
}