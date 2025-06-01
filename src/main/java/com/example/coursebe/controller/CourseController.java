package com.example.coursebe.controller;

import com.example.coursebe.common.ApiResponse;
import com.example.coursebe.dto.CourseEnrolledResponse;
import com.example.coursebe.dto.CourseResponse;
import com.example.coursebe.dto.EnrollmentResponse;
import com.example.coursebe.dto.UpdateCourseRequest;
import com.example.coursebe.exception.UnsupportedSearchTypeException;
import com.example.coursebe.model.Enrollment;
import com.example.coursebe.service.EnrollmentService;
import com.example.coursebe.util.AuthenticationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.example.coursebe.model.Course;
import com.example.coursebe.service.CourseService;
import com.example.coursebe.dto.CreateCourseRequest;
import com.example.coursebe.dto.builder.CourseRequest;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/courses")
public class CourseController {
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

    public CourseController(CourseService courseService, EnrollmentService enrollmentService) {
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getAllCourses(
            @RequestParam() UUID userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Course> courses = (type != null && keyword != null)
                ? courseService.searchCourses(type, keyword, pageable)
                : courseService.getAllCourses(pageable);

            Map<String, Object> courseMetadata = new HashMap<>();
            courseMetadata.put("totalItems", courses.getTotalElements());
            courseMetadata.put("totalPages", courses.getTotalPages());
            courseMetadata.put("currentPage", courses.getNumber());
            courseMetadata.put("pageSize", courses.getSize());

            List<CourseResponse> courseResponse = courses.getContent().stream()
                    .map(course -> toCourseResponse(course, userId))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Courses retrieved successfully.",
                courseMetadata,
                courseResponse
            ));
        } catch (UnsupportedSearchTypeException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                    HttpStatus.BAD_REQUEST.value(),
                    e.getMessage()
                ));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "An error occurred while fetching courses."
                ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseById(
            @PathVariable UUID id,
            @RequestParam UUID userId
    ) {
        try {
            Optional<Course> courseOpt = courseService.getCourseById(id);

            if (courseOpt.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(
                                HttpStatus.NOT_FOUND.value(),
                                "Course not found."
                        ));
            }

            Course course = courseOpt.get();

            CourseResponse response = this.toCourseResponse(course, userId);
            return ResponseEntity.ok(ApiResponse.success(
                    HttpStatus.OK.value(),
                    "Course retrieved successfully.",
                    response
            ));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "An error occurred while retrieving the course: " + e.getMessage()
                    ));
        }
    }

    @GetMapping("/my-courses")
    public ResponseEntity<ApiResponse<List<CourseEnrolledResponse>>> getMyAllCourses(
            @RequestParam() UUID userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Course> enrolledCourses = (type != null && keyword != null)
                    ? courseService.searchEnrolledCourses(userId, type, keyword, pageable)
                    : courseService.getEnrolledCourses(userId, pageable);

            Map<String, Object> courseMetadata = new HashMap<>();
            courseMetadata.put("totalItems", enrolledCourses.getTotalElements());
            courseMetadata.put("totalPages", enrolledCourses.getTotalPages());
            courseMetadata.put("currentPage", enrolledCourses.getNumber());
            courseMetadata.put("pageSize", enrolledCourses.getSize());

            List<CourseEnrolledResponse> courseResponse = enrolledCourses.getContent().stream()
                    .map(course -> toCourseEnrolledResponse(course, userId))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(
                    HttpStatus.OK.value(),
                    "User enrolled courses retrieved successfully.",
                    courseMetadata,
                    courseResponse
            ));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "An error occurred while fetching user enrolled courses: " + e.getMessage()
                    ));
        }
    }

    @GetMapping("/my-courses/{id}")
    public ResponseEntity<ApiResponse<CourseEnrolledResponse>> getMyCourseById(
            @PathVariable UUID id,
            @RequestParam UUID userId
    ) {
        try {
            Optional<Course> courseOpt = courseService.getCourseById(id);

            if (courseOpt.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(
                                HttpStatus.NOT_FOUND.value(),
                                "Course not found."
                        ));
            }

            Course course = courseOpt.get();
            if (enrollmentService.isEnrolled(userId, id)) {
                CourseEnrolledResponse response = this.toCourseEnrolledResponse(course, userId);
                return ResponseEntity.ok(ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Enrolled course retrieved successfully.",
                        response
                ));
            } else {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error(
                                HttpStatus.FORBIDDEN.value(),
                                "You are not enrolled in this course."
                        ));
            }
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "An error occurred while retrieving the course: " + e.getMessage()
                    ));
        }
    }

    @PostMapping("{id}/enroll")
    public CompletableFuture<ResponseEntity<ApiResponse<EnrollmentResponse>>> enrollCourse(
            @PathVariable UUID id,
            @RequestParam UUID userId
    ) {
        Optional<Course> courseOpt = courseService.getCourseById(id);

        if (courseOpt.isEmpty()) {
            return CompletableFuture.completedFuture(
                ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(
                        HttpStatus.NOT_FOUND.value(),
                        "Course not found."
                    ))
            );
        }

        return enrollmentService.enroll(userId, id)
            .thenApply(enrollment -> {
                if (enrollment != null) {
                    EnrollmentResponse enrollmentResponse = this.toEnrollmentResponse(enrollment);
                    return ResponseEntity.ok(
                        ApiResponse.success(
                            HttpStatus.OK.value(),
                            "Successfully enrolled in the course.",
                            enrollmentResponse
                        )
                    );
                } else {
                    return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.<EnrollmentResponse>error(
                            HttpStatus.BAD_REQUEST.value(),
                            "Failed to enroll in the course."
                        ));
                }
            })
            .exceptionally(ex -> {
                String errorMessage = "Error enrolling in course: " +
                        (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
                return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<EnrollmentResponse>error(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        errorMessage
                    ));
            });
    }

    @DeleteMapping("{id}/unenroll")
    public CompletableFuture<ResponseEntity<ApiResponse<Void>>> unenrollCourse(
            @PathVariable UUID id,
            @RequestParam UUID userId
    ) {
        return enrollmentService.unenroll(userId, id)
            .<ResponseEntity<ApiResponse<Void>>>thenApply(success -> {
                if (success) {
                    return ResponseEntity.ok(
                        ApiResponse.success(
                            HttpStatus.OK.value(),
                            "Successfully unenrolled from the course.",
                            null
                        )
                    );
                } else {
                    return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.<Void>error(
                            HttpStatus.BAD_REQUEST.value(),
                            "Failed to unenroll from the course."
                        ));
                }
            })
            .exceptionally(ex -> {
                String errorMessage = "Error unenrolling from course: " +
                        (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
                return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Void>error(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        errorMessage
                    ));            });
    }

    // POST /courses - Clean implementation with proper validation
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createCourse(@RequestBody CreateCourseRequest req, Principal principal) {
        try {
            UUID tutorId = AuthenticationUtil.parseUserId(principal);
            courseService.validateTutorAccess(tutorId);
            
            Course course = courseService.createCourse(req.name, req.description, tutorId, req.price);
            
            Map<String, Object> data = new HashMap<>();
            data.put("courseId", course.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Course created successfully.", data));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                .body(ApiResponse.error(e.getStatusCode().value(), e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred while creating the course: " + e.getMessage()));
        }
    }

    // POST /courses/with-builder - Create course using builder pattern with validation
    @PostMapping("/with-builder")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createCourseWithBuilder(@RequestBody CourseRequest courseRequest, Principal principal) {
        try {
            UUID tutorId = AuthenticationUtil.parseUserId(principal);
            courseRequest.setTutorId(tutorId);
            
            Course course = courseService.createCourseWithBuilder(courseRequest);
            
            Map<String, Object> data = new HashMap<>();
            data.put("courseId", course.getId());
            data.put("course", course);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Course created successfully using builder pattern.", data));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                .body(ApiResponse.error(e.getStatusCode().value(), e.getReason()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred while creating the course: " + e.getMessage()));
        }
    }

    // GET /courses/mine
    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<List<Course>>> getMyCourses(Principal principal) {
        try {
            UUID tutorId = AuthenticationUtil.parseUserId(principal);
            courseService.validateTutorAccess(tutorId);
            
            List<Course> courses = courseService.getCoursesByTutorId(tutorId);
            
            return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Courses retrieved successfully.", courses));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                .body(ApiResponse.error(e.getStatusCode().value(), e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred while retrieving courses: " + e.getMessage()));
        }
    }

    // PUT /courses/{courseId} - Clean implementation with proper validation
    @PutMapping("/{courseId}")
    public ResponseEntity<ApiResponse<Course>> updateCourse(@PathVariable UUID courseId, @RequestBody UpdateCourseRequest req, Principal principal) {
        try {
            UUID tutorId = AuthenticationUtil.parseUserId(principal);
            courseService.validateTutorAccess(tutorId);
            courseService.validateCourseOwnership(courseId, tutorId);
            
            Optional<Course> updatedCourseOpt = courseService.updateCourse(courseId, req.getName(), req.getDescription(), req.getPrice(), req.getSections());
            
            if (updatedCourseOpt.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Course updated successfully.", updatedCourseOpt.get()));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to update course."));
            }
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                .body(ApiResponse.error(e.getStatusCode().value(), e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred while updating the course: " + e.getMessage()));
        }
    }

    // DELETE /courses/{courseId}
    @DeleteMapping("/{courseId}")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable UUID courseId, Principal principal) {
        try {
            UUID tutorId = AuthenticationUtil.parseUserId(principal);
            // courseService.validateTutorAccess(tutorId); // Already handled in deleteCourseWithValidation
            // courseService.validateCourseOwnership(courseId, tutorId); // Already handled in deleteCourseWithValidation
            
            // boolean deleted = courseService.deleteCourse(courseId); // Old call
            courseService.deleteCourseWithValidation(courseId, tutorId); // New call

            // if (deleted) {
            return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Course deleted successfully.", null));
            // } else {
            //     return ResponseEntity.status(HttpStatus.NOT_FOUND)
            //         .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "Course not found or could not be deleted."));
            // }
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                .body(ApiResponse.error(e.getStatusCode().value(), e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred while deleting the course: " + e.getMessage()));
        }
    }

    // GET /courses/{courseId}/students
    @GetMapping("/{courseId}/students")
    public ResponseEntity<ApiResponse<List<String>>> getEnrolledStudents(@PathVariable UUID courseId, Principal principal) {
        try {
            UUID tutorId = AuthenticationUtil.parseUserId(principal);
            courseService.validateTutorAccess(tutorId);
            courseService.validateCourseOwnership(courseId, tutorId);
            
            List<String> students = courseService.getEnrolledStudents(courseId);
            
            return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Students retrieved successfully.", students));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                .body(ApiResponse.error(e.getStatusCode().value(), e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred while retrieving students: " + e.getMessage()));
        }
    }

    /* DTO Mapper helper */
    private CourseResponse toCourseResponse(Course course, UUID userId) {
        List<CourseResponse.Section> sectionResponses = course.getSections().stream()
            .map(section -> new CourseResponse.Section(
                section.getId(),
                section.getTitle()
            ))
            .collect(Collectors.toList());

        String tutorName = "Unknown"; // TODO: Fetch tutor name from user service or similar
        boolean isEnrolled = userId != null && enrollmentService.isEnrolled(userId, course.getId());

        return new CourseResponse(
                course.getId(),
                course.getName(),
                course.getDescription(),
                tutorName,
                course.getPrice(),
                isEnrolled,
                sectionResponses
        );
    };

    private CourseEnrolledResponse toCourseEnrolledResponse(Course course, UUID userId) {
        List<CourseEnrolledResponse.Section> sectionResponses = course.getSections().stream()
                .map(section -> {
                    List<CourseEnrolledResponse.Article> articleResponses = section.getArticles().stream()
                            .map(article -> new CourseEnrolledResponse.Article(
                                    article.getId(),
                                    article.getTitle()
                            ))
                            .collect(Collectors.toList());

                    return new CourseEnrolledResponse.Section(
                            section.getId(),
                            section.getTitle(),
                            articleResponses
                    );
                })
                .collect(Collectors.toList());

        // Get enrollment date
        LocalDateTime enrollmentDate = course.getEnrollments().stream()
                .filter(enrollment -> enrollment.getStudentId().equals(userId))
                .findFirst()
                .map(Enrollment::getEnrollmentDate)
                .orElse(null);

        String tutorName = "Unknown"; // TODO: Fetch tutor name from user service or similar

        return new CourseEnrolledResponse(
                course.getId(),
                course.getName(),
                course.getDescription(),
                tutorName,
                course.getPrice(),
                enrollmentDate,
                sectionResponses
        );
    }

    private EnrollmentResponse toEnrollmentResponse(Enrollment enrollment) {
        Course course = enrollment.getCourse();
        return new EnrollmentResponse(
                course.getId(),
                course.getName(),
                course.getDescription(),
                null,
                // TODO: tutor name //
                // course.getTutorId(),
                enrollment.getId(),
                enrollment.getEnrollmentDate() // Using LocalDateTime directly
        );
    }
}