package com.example.coursebe.repository;

import com.example.coursebe.model.Course;
import com.example.coursebe.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    List<Enrollment> findByStudentId(UUID studentId);
    List<Enrollment> findByCourse(Course course);
    Optional<Enrollment> findByStudentIdAndCourse(UUID studentId, Course course);
    boolean existsByStudentIdAndCourseId(UUID studentId, UUID courseId);
}