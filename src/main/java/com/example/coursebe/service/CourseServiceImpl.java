package com.example.coursebe.service;

import com.example.coursebe.dto.SectionDto;
import com.example.coursebe.dto.builder.CourseRequest;
import com.example.coursebe.model.Article;
import com.example.coursebe.exception.UnsupportedSearchTypeException;
import com.example.coursebe.model.Course;
import com.example.coursebe.model.Section;
import com.example.coursebe.model.Enrollment;
import com.example.coursebe.model.TutorApplication;
import com.example.coursebe.repository.ArticleRepository;
import com.example.coursebe.pattern.strategy.CourseSearchContext;
import com.example.coursebe.pattern.strategy.CourseSearchStrategy;
import com.example.coursebe.repository.CourseRepository;
import com.example.coursebe.repository.SectionRepository;
import com.example.coursebe.repository.EnrollmentRepository;
import com.example.coursebe.pattern.builder.CourseBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final ArticleRepository articleRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseSearchContext courseSearchContext;
    private final CourseBuilder courseBuilder;
    private final TutorApplicationService tutorApplicationService;

    public CourseServiceImpl(CourseRepository courseRepository,
                           SectionRepository sectionRepository,
                           ArticleRepository articleRepository,
                           EnrollmentRepository enrollmentRepository,
                           CourseSearchContext courseSearchContext,
                           CourseBuilder courseBuilder,
                           TutorApplicationService tutorApplicationService) {
        this.courseRepository = courseRepository;
        this.sectionRepository = sectionRepository;
        this.articleRepository = articleRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseSearchContext = courseSearchContext;
        this.courseBuilder = courseBuilder;
        this.tutorApplicationService = tutorApplicationService;
    }

    @Override
    public Page<Course> getAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable);
    }

    @Override
    public Optional<Course> getCourseById(UUID id) {
        return courseRepository.findById(id);
    }

    @Override
    public List<Course> getCoursesByTutorId(UUID tutorId) {
        return courseRepository.findByTutorId(tutorId);
    }

    @Override
    public Page<Course> searchCourses(String type, String keyword, Pageable pageable) {
        if (!courseSearchContext.isValidStrategy(type)) {
            throw new UnsupportedSearchTypeException(type);
        }
        CourseSearchStrategy strategy = courseSearchContext.getStrategy(type);
        return strategy.search(keyword, pageable);
    }

    @Override
    public Page<Course> getEnrolledCourses(UUID userId, Pageable pageable) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return courseRepository.findByEnrollmentsStudentId(userId, pageable);
    }

    @Override
    public Page<Course> searchEnrolledCourses(UUID userId, String type, String keyword, Pageable pageable) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (!courseSearchContext.isValidStrategy(type)) {
            throw new UnsupportedSearchTypeException(type);
        }
        CourseSearchStrategy strategy = courseSearchContext.getStrategy(type);
        return strategy.searchForUser(userId, keyword, pageable);
    }

    @Override
    @Transactional
    public Optional<Course> updateCourse(UUID id, String name, String description, BigDecimal price, List<SectionDto> sectionDtos) {
        if (id == null) {
            throw new IllegalArgumentException("Course ID cannot be null");
        }

        Optional<Course> optionalCourse = courseRepository.findById(id);
        if (optionalCourse.isEmpty()) {
            return Optional.empty();
        }        Course course = optionalCourse.get();
        
        Optional.ofNullable(name)
            .filter(StringUtils::hasText)
            .map(String::trim)
            .ifPresent(course::setName);
            
        Optional.ofNullable(description)
            .filter(StringUtils::hasText)
            .map(String::trim)
            .ifPresent(course::setDescription);
            
        Optional.ofNullable(price)
            .ifPresent(course::setPrice);

        if (sectionDtos != null) {
            course.getSections().clear();
            for (SectionDto sectionDto : sectionDtos) {
                Section section = new Section(sectionDto.getTitle(), sectionDto.getPosition());
                section.setCourse(course);
                
                if (sectionDto.getArticles() != null) {
                    for (com.example.coursebe.dto.ArticleDto articleDto : sectionDto.getArticles()) {
                        Article article = new Article(articleDto.getTitle(), articleDto.getContent(), articleDto.getPosition());
                        article.setSection(section);
                        section.getArticles().add(article);
                    }
                }
                course.getSections().add(section);
            }
        }

        Course updatedCourse = courseRepository.save(course);
        return Optional.of(updatedCourse);
    }

    @Override
    @Transactional
    public void deleteCourseWithValidation(UUID courseId, UUID tutorId) {
        var appOpt = tutorApplicationService.getMostRecentApplicationByStudentId(tutorId);
        if (appOpt.isEmpty() || appOpt.get().getStatus() != TutorApplication.Status.ACCEPTED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "You are not allowed to perform this action. Tutor application must be ACCEPTED.");
        }

        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found."));

        if (!course.getTutorId().equals(tutorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "You are not allowed to delete this course. Only the owner can delete.");
        }

        courseRepository.delete(course);
    }

    @Override
    public List<String> getEnrolledStudents(UUID courseId) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isEmpty()) {
            return List.of();
        }

        Course course = courseOpt.get();
        List<Enrollment> enrollments = enrollmentRepository.findByCourse(course);

        return enrollments.stream()
                .map(enrollment -> enrollment.getStudentId().toString())
                .collect(Collectors.toList());
    }    
    
    @Override
    @Transactional
    public Course createCourseWithBuilder(CourseRequest courseRequest) {
        Course course = courseBuilder
            .name(courseRequest.getName())
            .description(courseRequest.getDescription())
            .tutorId(courseRequest.getTutorId())
            .price(courseRequest.getPrice())
            .buildEntity();
        return courseRepository.save(course);
    }

    @Override
    public void validateTutorAccess(UUID tutorId) {
        var appOpt = tutorApplicationService.getMostRecentApplicationByStudentId(tutorId);
        if (appOpt.isEmpty() || appOpt.get().getStatus() != TutorApplication.Status.ACCEPTED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "You are not allowed to perform this action. Tutor application must be ACCEPTED.");
        }
    }

    @Override
    public void validateCourseOwnership(UUID courseId, UUID tutorId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found."));
        
        if (!course.getTutorId().equals(tutorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "You are not allowed to perform this action. Only the course owner can do this.");
        }
    }
}