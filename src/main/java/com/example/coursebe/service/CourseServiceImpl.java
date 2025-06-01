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

import java.util.Map;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of CourseService
 * Uses the Repository pattern to abstract data access
 */
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
    public Course createCourse(String name, String description, UUID tutorId, BigDecimal price) {
        // Validate inputs
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Course name cannot be empty");
        }
        if (tutorId == null) {
            throw new IllegalArgumentException("Tutor ID cannot be null");
        }

        Course course = new Course(name, description, tutorId, price);
        return courseRepository.save(course);
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
        }

        Course course = optionalCourse.get();

        // Update basic course details
        if (name != null && !name.trim().isEmpty()) {
            course.setName(name);
        }
        if (description != null) {
            course.setDescription(description);
        }
        if (price != null) {
            course.setPrice(price);
        }

        // Handle sections and articles
        if (sectionDtos != null) {
            updateSectionsAndArticles(course, sectionDtos);
        }

        Course updatedCourse = courseRepository.save(course);
        return Optional.of(updatedCourse);
    }    private void updateSectionsAndArticles(Course course, List<SectionDto> sectionDtos) {
        Map<UUID, Section> existingSectionsMap = course.getSections().stream()
                .collect(Collectors.toMap(Section::getId, s -> s));
        List<Section> updatedSections = new ArrayList<>();

        for (SectionDto sectionDto : sectionDtos) {
            Section section;
            if (sectionDto.getId() == null) { // New section
                section = new Section(sectionDto.getTitle(), sectionDto.getPosition());
                section.setCourse(course);
            } else { // Existing section
                section = existingSectionsMap.remove(sectionDto.getId());
                if (section == null) {
                    section = new Section(sectionDto.getTitle(), sectionDto.getPosition());
                    section.setCourse(course);
                }
                section.setTitle(sectionDto.getTitle());
                section.setPosition(sectionDto.getPosition());
            }

            if (sectionDto.getArticles() != null) {
                updateArticles(section, sectionDto.getArticles());
            }
            updatedSections.add(section); // Add new or updated section
        }

        // Remove sections that were not in the DTO list (orphanRemoval=true will handle DB deletion)
        // Clear and add all ensures correct associations and JPA lifecycle management
        course.getSections().clear();
        course.getSections().addAll(updatedSections);

        // Explicitly delete sections that were in existingSectionsMap but not in sectionDtos
        // This is needed if orphanRemoval=true is not sufficient or if you want to manage it explicitly
        for (Section sectionToRemove : existingSectionsMap.values()) {
            sectionRepository.delete(sectionToRemove);
        }
    }    private void updateArticles(Section section, List<com.example.coursebe.dto.ArticleDto> articleDtos) {
        Map<UUID, Article> existingArticlesMap = section.getArticles().stream()
                .collect(Collectors.toMap(Article::getId, a -> a));
        List<Article> updatedArticles = new ArrayList<>();

        for (com.example.coursebe.dto.ArticleDto articleDto : articleDtos) {
            Article article;
            if (articleDto.getId() == null) { // New article
                article = new Article(articleDto.getTitle(), articleDto.getContent(), articleDto.getPosition());
                article.setSection(section);
            } else { // Existing article
                article = existingArticlesMap.remove(articleDto.getId());
                if (article == null) {
                    article = new Article(articleDto.getTitle(), articleDto.getContent(), articleDto.getPosition());
                    article.setSection(section);
                }
                article.setTitle(articleDto.getTitle());
                article.setContent(articleDto.getContent());
                article.setPosition(articleDto.getPosition());
            }
            updatedArticles.add(article);
        }

        // Clear and add all ensures correct associations and JPA lifecycle management
        section.getArticles().clear();
        section.getArticles().addAll(updatedArticles);

        // Explicitly delete articles that were in existingArticlesMap but not in articleDtos
        // This is needed if orphanRemoval=true is not sufficient or if you want to manage it explicitly
         for (Article articleToRemove : existingArticlesMap.values()) {
            articleRepository.delete(articleToRemove);
        }
    }

    @Override
    @Transactional
    public boolean deleteCourse(UUID id) {
        // Validate inputs
        if (id == null) {
            throw new IllegalArgumentException("Course ID cannot be null");
        }

        // Check if course exists
        if (courseRepository.existsById(id)) {
            courseRepository.deleteById(id);
            return true;
        }
          return false;
    }

    @Override
    @Transactional
    public void deleteCourseWithValidation(UUID courseId, UUID tutorId) {
        validateTutorAccess(tutorId);

        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found."));

        if (!course.getTutorId().equals(tutorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "You are not allowed to delete this course. Only the owner can delete.");
        }

        courseRepository.delete(course);
    }    @Override
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
    public Course createCourseWithBuilder(CourseRequest courseRequest) {
        return courseBuilder.buildEntity(courseRequest);
    }

    @Override
    public void validateTutorAccess(UUID tutorId) {
        if (tutorId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tutor ID is required");
        }
        
        Optional<TutorApplication> tutorApplicationOpt = tutorApplicationService.findByTutorId(tutorId);
        if (tutorApplicationOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "No tutor application found. You must be an approved tutor to create courses.");
        }
        
        TutorApplication tutorApplication = tutorApplicationOpt.get();
        if (tutorApplication.getStatus() != TutorApplication.Status.ACCEPTED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                String.format("Cannot create course. Your tutor status is %s, but must be ACCEPTED to create courses.", 
                             tutorApplication.getStatus()));
        }
    }

    @Override
    public void validateCourseOwnership(UUID courseId, UUID tutorId) {
        if (courseId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course ID is required");
        }
        if (tutorId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tutor ID is required");
        }
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        
        if (!course.getTutorId().equals(tutorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "You are not allowed to perform this action. Only the course owner can do this.");
        }
    }
}