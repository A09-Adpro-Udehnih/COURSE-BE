package com.example.coursebe.service;

import com.example.coursebe.model.TutorApplication;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
     * Get all tutor applications with a specific status
     * @param status Application status
     * @return List of applications with the specified status
     */
    List<TutorApplication> getApplicationsByStatus(TutorApplication.Status status);
    
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
     * Update the status of an application
     * @param id Application ID
     * @param status New status
     * @return Updated application or empty optional if not found
     */
    Optional<TutorApplication> updateApplicationStatus(UUID id, TutorApplication.Status status);
    
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
}