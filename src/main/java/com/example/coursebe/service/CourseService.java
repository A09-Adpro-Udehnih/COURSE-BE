package com.example.coursebe.service;

import com.example.coursebe.model.Course;
import com.example.coursebe.controller.CourseController; // Added for SectionDto
import com.example.coursebe.dto.builder.CourseRequest;
import com.example.coursebe.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface CourseService {
    Page<Course> getAllCourses(Pageable pageable);
    Optional<Course> getCourseById(UUID id);
    List<Course> getCoursesByTutorId(UUID tutorId);
    Page<Course> searchCourses(String type, String keyword, Pageable pageable);
    Page<Course> getEnrolledCourses(UUID userId, Pageable pageable);
    Page<Course> searchEnrolledCourses(UUID userId, String type, String keyword, Pageable pageable);
    Course createCourse(String name, String description, UUID tutorId, BigDecimal price);
    CompletableFuture<Course> createCourseAsync(String name, String description, UUID tutorId, BigDecimal price);
    Optional<Course> updateCourse(UUID id, String name, String description, BigDecimal price, List<CourseController.SectionDto> sections);
    boolean deleteCourse(UUID id);
    List<String> getEnrolledStudents(UUID courseId);
    Course createCourseWithBuilder(CourseRequest courseRequest);
    Optional<Course> updateCourseStatus(UUID courseId, Status status);
    Optional<Status> getCourseStatus(UUID courseId);
}