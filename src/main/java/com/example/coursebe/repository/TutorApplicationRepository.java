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

@Repository
public interface TutorApplicationRepository extends JpaRepository<TutorApplication, UUID> {
    List<TutorApplication> findByStudentId(UUID studentId);
    Optional<TutorApplication> findTopByStudentIdOrderByCreatedAtDesc(UUID studentId);    List<TutorApplication> findByStatus(TutorApplication.Status status);
    boolean existsByStudentId(UUID studentId);
    boolean existsByStudentIdAndStatus(UUID studentId, TutorApplication.Status status);
    
    @Modifying
    @Query(value = "DELETE FROM tutor_application WHERE id IN " +
                   "(SELECT id FROM (SELECT id FROM tutor_application " +
                   "WHERE student_id = :studentId ORDER BY created_at DESC LIMIT 1) AS temp)", 
           nativeQuery = true)
    int deleteTopByStudentIdOrderByCreatedAtDesc(@Param("studentId") UUID studentId);

    @Modifying
    int deleteByStudentId(UUID studentId);

    @Modifying
    int deleteByStatus(TutorApplication.Status status);
    
    @Query("SELECT t FROM TutorApplication t WHERE t.createdAt < CURRENT_TIMESTAMP - :daysOld DAY")
    List<TutorApplication> findApplicationsOlderThan(@Param("daysOld") int daysOld);
}