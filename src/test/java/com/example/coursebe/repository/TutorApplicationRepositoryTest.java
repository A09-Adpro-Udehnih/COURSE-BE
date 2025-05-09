package com.example.coursebe.repository;

import com.example.coursebe.model.TutorApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for TutorApplicationRepository
 */
@DataJpaTest
public class TutorApplicationRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private TutorApplicationRepository tutorApplicationRepository;
    
    private UUID studentId1;
    private UUID studentId2;
    private TutorApplication application1;
    private TutorApplication application2;
    private TutorApplication application3;
    
    @BeforeEach
    void setUp() {
        studentId1 = UUID.randomUUID();
        studentId2 = UUID.randomUUID();
        
        // Create test applications
        application1 = new TutorApplication(studentId1);
        application1.setStatus(TutorApplication.Status.PENDING);
        
        application2 = new TutorApplication(studentId1);
        application2.setStatus(TutorApplication.Status.DENIED);
        
        application3 = new TutorApplication(studentId2);
        application3.setStatus(TutorApplication.Status.ACCEPTED);
        
        // Ensure different creation times for testing order
        entityManager.persist(application1);
        // Simulate delay between application submissions
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        entityManager.persist(application2);
        entityManager.persist(application3);
        entityManager.flush();
    }
    
    @Test
    @DisplayName("Should find all applications")
    void findAllApplications() {
        // when
        List<TutorApplication> applications = tutorApplicationRepository.findAll();
        
        // then
        assertNotNull(applications);
        assertEquals(3, applications.size());
    }
    
    @Test
    @DisplayName("Should find application by ID")
    void findApplicationById() {
        // when
        Optional<TutorApplication> found = tutorApplicationRepository.findById(application1.getId());
        
        // then
        assertTrue(found.isPresent());
        assertEquals(studentId1, found.get().getStudentId());
        assertEquals(TutorApplication.Status.PENDING, found.get().getStatus());
    }
    
    @Test
    @DisplayName("Should find applications by student ID")
    void findApplicationsByStudentId() {
        // when
        List<TutorApplication> studentApplications = tutorApplicationRepository.findByStudentId(studentId1);
        
        // then
        assertNotNull(studentApplications);
        assertEquals(2, studentApplications.size());
        assertTrue(studentApplications.stream().allMatch(app -> app.getStudentId().equals(studentId1)));
    }
    
    @Test
    @DisplayName("Should find most recent application by student ID")
    void findMostRecentApplicationByStudentId() {
        // when
        Optional<TutorApplication> mostRecent = tutorApplicationRepository.findTopByStudentIdOrderByCreatedAtDesc(studentId1);
        
        // then
        assertTrue(mostRecent.isPresent());
        assertEquals(studentId1, mostRecent.get().getStudentId());
        // The most recent should be application2 since it was persisted later
        assertEquals(application2.getId(), mostRecent.get().getId());
    }
    
    @Test
    @DisplayName("Should find applications by status")
    void findApplicationsByStatus() {
        // when - pending status
        List<TutorApplication> pendingApplications = tutorApplicationRepository.findByStatus(TutorApplication.Status.PENDING);
        
        // then
        assertEquals(1, pendingApplications.size());
        assertEquals(TutorApplication.Status.PENDING, pendingApplications.get(0).getStatus());
        
        // when - accepted status
        List<TutorApplication> acceptedApplications = tutorApplicationRepository.findByStatus(TutorApplication.Status.ACCEPTED);
        
        // then
        assertEquals(1, acceptedApplications.size());
        assertEquals(TutorApplication.Status.ACCEPTED, acceptedApplications.get(0).getStatus());
        assertEquals(studentId2, acceptedApplications.get(0).getStudentId());
    }
    
    @Test
    @DisplayName("Should check if student has pending application")
    void existsByStudentIdAndStatus() {
        // when - student has pending application
        boolean hasPending = tutorApplicationRepository.existsByStudentIdAndStatus(studentId1, TutorApplication.Status.PENDING);
        
        // then
        assertTrue(hasPending);
        
        // when - student does not have accepted application
        boolean hasAccepted = tutorApplicationRepository.existsByStudentIdAndStatus(studentId1, TutorApplication.Status.ACCEPTED);
        
        // then
        assertFalse(hasAccepted);
    }
    
    @Test
    @DisplayName("Should save application")
    void saveApplication() {
        // given
        UUID newStudentId = UUID.randomUUID();
        TutorApplication newApplication = new TutorApplication(newStudentId);
        
        // when
        TutorApplication saved = tutorApplicationRepository.save(newApplication);
        
        // then
        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals(newStudentId, saved.getStudentId());
        assertEquals(TutorApplication.Status.PENDING, saved.getStatus());
        
        // when
        Optional<TutorApplication> found = tutorApplicationRepository.findById(saved.getId());
        
        // then
        assertTrue(found.isPresent());
    }
    
    @Test
    @DisplayName("Should update application status")
    void updateApplicationStatus() {
        // given
        TutorApplication application = application1;
        application.setStatus(TutorApplication.Status.ACCEPTED);
        
        // when
        TutorApplication updated = tutorApplicationRepository.save(application);
        
        // then
        assertNotNull(updated);
        assertEquals(TutorApplication.Status.ACCEPTED, updated.getStatus());
        
        // when
        Optional<TutorApplication> found = tutorApplicationRepository.findById(updated.getId());
        
        // then
        assertTrue(found.isPresent());
        assertEquals(TutorApplication.Status.ACCEPTED, found.get().getStatus());
    }
    
    @Test
    @DisplayName("Should delete application")
    void deleteApplication() {
        // given
        TutorApplication applicationToDelete = application3;
        
        // when
        tutorApplicationRepository.delete(applicationToDelete);
        Optional<TutorApplication> found = tutorApplicationRepository.findById(applicationToDelete.getId());
        
        // then
        assertFalse(found.isPresent());
    }
}