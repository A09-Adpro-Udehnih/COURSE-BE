package com.example.coursebe.repository;

import com.example.coursebe.model.TutorApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for TutorApplication entity
 * Provides CRUD operations and custom query methods for TutorApplication
 */
@Repository
public interface TutorApplicationRepository extends JpaRepository<TutorApplication, UUID> {
    
    /**
     * Find all applications submitted by a specific student
     * @param studentId the ID of the student
     * @return list of applications submitted by the student
     */
    List<TutorApplication> findByStudentId(UUID studentId);
    
    /**
     * Find the most recent application submitted by a specific student
     * @param studentId the ID of the student
     * @return optional containing the most recent application if it exists
     */
    Optional<TutorApplication> findTopByStudentIdOrderByCreatedAtDesc(UUID studentId);
    
    /**
     * Find all applications with a specific status
     * @param status the application status
     * @return list of applications with the specified status
     */
    List<TutorApplication> findByStatus(TutorApplication.Status status);
    
    /**
     * Check if a student has any pending applications
     * @param studentId the ID of the student
     * @param status the PENDING status
     * @return true if the student has a pending application, false otherwise
     */
    boolean existsByStudentIdAndStatus(UUID studentId, TutorApplication.Status status);
}