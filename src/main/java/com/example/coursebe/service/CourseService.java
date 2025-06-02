package com.example.coursebe.service;

import com.example.coursebe.dto.SectionDto;
import com.example.coursebe.dto.builder.CourseRequest;
import com.example.coursebe.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseService {
    Page<Course> getAllCourses(Pageable pageable);
    Optional<Course> getCourseById(UUID id);
    List<Course> getCoursesByTutorId(UUID tutorId);
    Page<Course> searchCourses(String type, String keyword, Pageable pageable);
    Page<Course> getEnrolledCourses(UUID userId, Pageable pageable);
    Page<Course> searchEnrolledCourses(UUID userId, String type, String keyword, Pageable pageable);    Course createCourse(String name, String description, UUID tutorId, BigDecimal price);
    Course createCourseWithBuilder(CourseRequest courseRequest);    void deleteCourseWithValidation(UUID courseId, UUID tutorId);
    List<String> getEnrolledStudents(UUID courseId);
    List<String> getEnrolledStudentsWithValidation(UUID courseId, UUID tutorId);
    boolean deleteCourse(UUID id);
    public Optional<Course> updateCourse(UUID id, String name, String description, BigDecimal price, List<SectionDto> sectionDtos);
    void validateTutorAccess(UUID tutorId);
    void validateCourseOwnership(UUID courseId, UUID tutorId);
}