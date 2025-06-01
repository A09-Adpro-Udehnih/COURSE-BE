package com.example.coursebe.service;

import com.example.coursebe.controller.CourseController;
import com.example.coursebe.enums.Status;
import com.example.coursebe.model.Article;
import com.example.coursebe.exception.UnsupportedSearchTypeException;
import com.example.coursebe.model.Course;
import com.example.coursebe.model.Section;
import com.example.coursebe.model.Enrollment;
import com.example.coursebe.repository.ArticleRepository;
import com.example.coursebe.pattern.strategy.CourseSearchContext;
import com.example.coursebe.pattern.strategy.CourseSearchStrategy;
import com.example.coursebe.repository.CourseRepository;
import com.example.coursebe.repository.SectionRepository;
import com.example.coursebe.repository.EnrollmentRepository;
import com.example.coursebe.dto.builder.CourseRequest;
import com.example.coursebe.pattern.builder.CourseBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;

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

    public CourseServiceImpl(CourseRepository courseRepository,
                           SectionRepository sectionRepository,
                           ArticleRepository articleRepository,
                           EnrollmentRepository enrollmentRepository,
                           CourseSearchContext courseSearchContext,
                           CourseBuilder courseBuilder) {
        this.courseRepository = courseRepository;
        this.sectionRepository = sectionRepository;
        this.articleRepository = articleRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseSearchContext = courseSearchContext;
        this.courseBuilder = courseBuilder;
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
    public CompletableFuture<Course> createCourseAsync(String name, String description, UUID tutorId, BigDecimal price) {
        return CompletableFuture.completedFuture(createCourse(name, description, tutorId, price));
    }

    @Override
    @Transactional
    public Optional<Course> updateCourse(UUID id, String name, String description, BigDecimal price, List<CourseController.SectionDto> sectionDtos) {
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
    }

    private void updateSectionsAndArticles(Course course, List<CourseController.SectionDto> sectionDtos) {
        Map<UUID, Section> existingSectionsMap = course.getSections().stream()
                .collect(Collectors.toMap(Section::getId, s -> s));
        List<Section> updatedSections = new ArrayList<>();

        for (CourseController.SectionDto sectionDto : sectionDtos) {
            Section section;
            if (sectionDto.id == null) { // New section
                section = new Section(sectionDto.title, sectionDto.position);
                section.setCourse(course);
            } else { // Existing section
                section = existingSectionsMap.remove(sectionDto.id);
                if (section == null) {
                    // Section ID provided but not found in course, could throw error or ignore
                    // For now, let's assume it's an error or treat as new if desired
                    // Or, if it's a section from another course, it's an error.
                    // Let's create it as new for now if not found, or you might want to throw an exception.
                     section = new Section(sectionDto.title, sectionDto.position);
                     section.setCourse(course);
                }
                section.setTitle(sectionDto.title);
                section.setPosition(sectionDto.position);
            }

            if (sectionDto.articles != null) {
                updateArticles(section, sectionDto.articles);
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
    }

    private void updateArticles(Section section, List<CourseController.ArticleDto> articleDtos) {
        Map<UUID, Article> existingArticlesMap = section.getArticles().stream()
                .collect(Collectors.toMap(Article::getId, a -> a));
        List<Article> updatedArticles = new ArrayList<>();

        for (CourseController.ArticleDto articleDto : articleDtos) {
            Article article;
            if (articleDto.id == null) { // New article
                article = new Article(articleDto.title, articleDto.content, articleDto.position);
                article.setSection(section);
            } else { // Existing article
                article = existingArticlesMap.remove(articleDto.id);
                if (article == null) {
                    // Similar to sections, handle error or create new
                    article = new Article(articleDto.title, articleDto.content, articleDto.position);
                    article.setSection(section);
                }
                article.setTitle(articleDto.title);
                article.setContent(articleDto.content);
                article.setPosition(articleDto.position);
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
    }    @Override
    public List<String> getEnrolledStudents(UUID courseId) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isEmpty()) {
            return List.of(); // Atau throw exception jika course tidak ditemukan
        }

        Course course = courseOpt.get();
        List<Enrollment> enrollments = enrollmentRepository.findByCourse(course);

        return enrollments.stream()
                .map(enrollment -> enrollment.getStudentId().toString()) // Asumsi studentId adalah UUID
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Course createCourseWithBuilder(CourseRequest courseRequest) {
        // Validate the request and tutor status
        courseRequest.validate();
        
        // Use the builder to create the course entity with validation
        Course course = courseBuilder
                .name(courseRequest.getName())
                .description(courseRequest.getDescription())
                .tutorId(courseRequest.getTutorId())
                .price(courseRequest.getPrice())
                .addSections(courseRequest.getSections())
                .buildEntity();
        
        // Save the course (cascading will save sections and articles)
        Course savedCourse = courseRepository.save(course);
          // Reset the builder for reuse
        courseBuilder.reset();
        
        return savedCourse;
    }

    /**
     * Get the status of a course by its ID
     * @param courseId Course ID
     * @return Optional containing the course status if course is found
     */    @Override
    public Optional<Status> getCourseStatus(UUID courseId) {
        return courseRepository.findById(courseId)
                .map(Course::getStatus);
    }    @Override
    public Optional<Course> updateCourseStatus(UUID courseId, Status status) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isPresent()) {
            Course course = courseOpt.get();
            course.setStatus(status);
            Course savedCourse = courseRepository.save(course);
            return Optional.of(savedCourse);
        }
        return Optional.empty();
    }
}