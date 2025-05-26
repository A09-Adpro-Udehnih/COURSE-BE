package com.example.coursebe.repository;

import com.example.coursebe.model.TutorApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
      /**
     * Delete the most recent application by student ID in a single query
     * This is optimized to reduce database round trips
     * Uses a more efficient approach with proper indexing support
     * @param studentId the ID of the student
     * @return number of deleted records (0 or 1)
     */
    @Modifying
    @Query(value = "DELETE FROM tutor_application WHERE id IN " +
                   "(SELECT id FROM (SELECT id FROM tutor_application " +
                   "WHERE student_id = :studentId ORDER BY created_at DESC LIMIT 1) AS temp)", 
           nativeQuery = true)
    long deleteTopByStudentIdOrderByCreatedAtDesc(@Param("studentId") UUID studentId);
    
    /**
     * Delete all applications by student ID
     * Useful for cleanup operations
     * @param studentId the ID of the student
     * @return number of deleted records
     */
    @Modifying
    int deleteByStudentId(UUID studentId);
    
    /**
     * Batch delete applications by status
     * Useful for administrative cleanup
     * @param status the status to delete
     * @return number of deleted records
     */
    @Modifying
    int deleteByStatus(TutorApplication.Status status);
    
    /**
     * Find applications older than specified days for cleanup
     * @param daysOld number of days to look back
     * @return list of old applications
     */
    @Query("SELECT t FROM TutorApplication t WHERE t.createdAt < CURRENT_TIMESTAMP - :daysOld DAY")
    List<TutorApplication> findApplicationsOlderThan(@Param("daysOld") int daysOld);
}