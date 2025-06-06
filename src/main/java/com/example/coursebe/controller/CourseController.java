package com.example.coursebe.controller;

import com.example.coursebe.common.ApiResponse;
import com.example.coursebe.dto.CourseEnrolledResponse;
import com.example.coursebe.dto.CourseResponse;
import com.example.coursebe.dto.EnrollmentResponse;
import com.example.coursebe.exception.UnsupportedSearchTypeException;
import com.example.coursebe.model.Enrollment;
import com.example.coursebe.service.EnrollmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.coursebe.model.Course;
import com.example.coursebe.model.TutorApplication;
import com.example.coursebe.service.CourseService;
import com.example.coursebe.service.TutorApplicationService;
import com.example.coursebe.dto.CreateCourseRequest;

import java.math.BigDecimal;
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
    private final TutorApplicationService tutorApplicationService;

    public CourseController(CourseService courseService, EnrollmentService enrollmentService, TutorApplicationService tutorApplicationService) {
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
        this.tutorApplicationService = tutorApplicationService;
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
                    ));
            });
    }

    // DTOs for course content update
    public static class ArticleDto {
        public UUID id; // Null for new articles, existing ID for updates
        public String title;
        public String content;
        public Integer position;
    }

    public static class SectionDto {
        public UUID id; // Null for new sections, existing ID for updates
        public String title;
        public Integer position;
        public List<ArticleDto> articles;
    }

    // DTO for course update (now includes sections)
    public static class UpdateCourseRequest {
        public String name;
        public String description;
        public BigDecimal price;
        public List<SectionDto> sections; // To manage course content
    }

    // POST /courses - Sync implementation for JWT compatibility
    @PostMapping
    public ResponseEntity<?> createCourse(@RequestBody CreateCourseRequest req, Principal principal) {
        UUID tutorId = UUID.fromString(principal.getName());
        // Validasi: hanya tutor dengan status ACCEPTED yang boleh membuat kursus
        var appOpt = tutorApplicationService.getMostRecentApplicationByStudentId(tutorId);
        if (appOpt.isEmpty() || appOpt.get().getStatus() != com.example.coursebe.model.TutorApplication.Status.ACCEPTED) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.FORBIDDEN.value());
            resp.put("success", false);
            resp.put("message", "You are not allowed to create a course. Tutor application must be ACCEPTED.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        }

        Course course = courseService.createCourse(req.name, req.description, tutorId, req.price);
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", HttpStatus.CREATED.value());
        resp.put("success", true);
        resp.put("message", "Course created successfully.");
        resp.put("courseId", course.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    // GET /courses/mine
    @GetMapping("/mine")
    public ResponseEntity<?> getMyCourses(Principal principal) {
        UUID tutorId = UUID.fromString(principal.getName());

        var appOpt = tutorApplicationService.getMostRecentApplicationByStudentId(tutorId);
        if (appOpt.isEmpty() || appOpt.get().getStatus() != com.example.coursebe.model.TutorApplication.Status.ACCEPTED) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.FORBIDDEN.value());
            resp.put("success", false);
            resp.put("message", "You are not allowed to view courses. Tutor application must be ACCEPTED.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        }

        var courses = courseService.getCoursesByTutorId(tutorId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", HttpStatus.OK.value());
        resp.put("success", true);
        resp.put("courses", courses);
        return ResponseEntity.ok(resp);
    }

    // PUT /courses/{courseId} - Sync implementation for JWT compatibility
    @PutMapping("/{courseId}")
    public ResponseEntity<?> updateCourse(@PathVariable UUID courseId, @RequestBody UpdateCourseRequest req, Principal principal) {
        UUID tutorId = UUID.fromString(principal.getName());

        // Validasi: hanya tutor dengan status ACCEPTED yang boleh mengedit kursus
        Optional<TutorApplication> appOpt = tutorApplicationService.getMostRecentApplicationByStudentId(tutorId);
        if (appOpt.isEmpty() || appOpt.get().getStatus() != TutorApplication.Status.ACCEPTED) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.FORBIDDEN.value());
            resp.put("success", false);
            resp.put("message", "You are not allowed to update courses. Tutor application must be ACCEPTED.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        }

        // Validasi: hanya pemilik kursus yang boleh mengedit
        Optional<Course> courseOpt = courseService.getCourseById(courseId);
        if (courseOpt.isEmpty()) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.NOT_FOUND.value());
            resp.put("success", false);
            resp.put("message", "Course not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }

        if (!courseOpt.get().getTutorId().equals(tutorId)) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.FORBIDDEN.value());
            resp.put("success", false);
            resp.put("message", "You are not allowed to update this course. Only the owner can update.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        }

        // Pass the sections to the service layer - This will require CourseService to be updated
        Optional<Course> updatedCourseOpt = courseService.updateCourse(courseId, req.name, req.description, req.price, req.sections);

        if (updatedCourseOpt.isPresent()) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.OK.value());
            resp.put("success", true);
            resp.put("message", "Course updated successfully.");
            resp.put("course", updatedCourseOpt.get());
            return ResponseEntity.ok(resp);
        } else {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value()); // Or NOT_FOUND if appropriate
            resp.put("success", false);
            resp.put("message", "Failed to update course.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    // DELETE /courses/{courseId}
    @DeleteMapping("/{courseId}")
    public ResponseEntity<?> deleteCourse(@PathVariable UUID courseId, Principal principal) {
        UUID tutorId = UUID.fromString(principal.getName());

        // Validasi: hanya tutor yang memiliki kursus dan status ACCEPTED yang bisa hapus
        var appOpt = tutorApplicationService.getMostRecentApplicationByStudentId(tutorId);
        if (appOpt.isEmpty() || appOpt.get().getStatus() != com.example.coursebe.model.TutorApplication.Status.ACCEPTED) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.FORBIDDEN.value());
            resp.put("success", false);
            resp.put("message", "You are not allowed to delete this course. Tutor application must be ACCEPTED.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        }

        var courseOpt = courseService.getCourseById(courseId);
        if (courseOpt.isEmpty() || !courseOpt.get().getTutorId().equals(tutorId)) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.FORBIDDEN.value());
            resp.put("success", false);
            resp.put("message", "You are not allowed to delete this course. Only the owner can delete.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        }
        boolean deleted = courseService.deleteCourse(courseId);
        Map<String, Object> resp = new HashMap<>();
        if (deleted) {
            resp.put("code", HttpStatus.OK.value());
            resp.put("success", true);
            resp.put("message", "Course deleted successfully.");
            return ResponseEntity.ok(resp);
        } else {
            resp.put("code", HttpStatus.NOT_FOUND.value());
            resp.put("success", false);
            resp.put("message", "Course not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }
    }

    // GET /courses/{courseId}/students
    @GetMapping("/{courseId}/students")
    public ResponseEntity<?> getEnrolledStudents(@PathVariable UUID courseId, Principal principal) {
        UUID tutorId = UUID.fromString(principal.getName());
        // Validasi: hanya tutor owner & status ACCEPTED yang bisa akses
        var appOpt = tutorApplicationService.getMostRecentApplicationByStudentId(tutorId);
        if (appOpt.isEmpty() || appOpt.get().getStatus() != com.example.coursebe.model.TutorApplication.Status.ACCEPTED) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.FORBIDDEN.value());
            resp.put("success", false);
            resp.put("message", "You are not allowed to view students. Tutor application must be ACCEPTED.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        }
        var courseOpt = courseService.getCourseById(courseId);
        if (courseOpt.isEmpty() || !courseOpt.get().getTutorId().equals(tutorId)) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.FORBIDDEN.value());
            resp.put("success", false);
            resp.put("message", "You are not allowed to view students. Only the owner can view.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        }
        var students = courseService.getEnrolledStudents(courseId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", HttpStatus.OK.value());
        resp.put("success", true);
        resp.put("students", students);
        return ResponseEntity.ok(resp);
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