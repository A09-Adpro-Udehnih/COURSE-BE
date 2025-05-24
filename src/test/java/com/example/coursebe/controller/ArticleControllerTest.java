package com.example.coursebe.controller;

import com.example.coursebe.model.Article;
import com.example.coursebe.model.Course;
import com.example.coursebe.model.Section;
import com.example.coursebe.model.TutorApplication;
import com.example.coursebe.service.ArticleService;
import com.example.coursebe.service.CourseService;
import com.example.coursebe.service.SectionService;
import com.example.coursebe.service.TutorApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ArticleControllerTest {

    @Mock
    private ArticleService articleService;

    @Mock
    private SectionService sectionService;

    @Mock
    private CourseService courseService;

    @Mock
    private TutorApplicationService tutorApplicationService;

    @Mock
    private Principal principal;

    @InjectMocks
    private ArticleController articleController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UUID courseId = UUID.randomUUID();
    private final UUID sectionId = UUID.randomUUID();
    private final UUID articleId = UUID.randomUUID();
    private final UUID tutorId = UUID.randomUUID();
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(articleController).build();
        when(principal.getName()).thenReturn(tutorId.toString());
    }

    @Test
    @DisplayName("POST /courses/{courseId}/sections/{sectionId}/articles - Success")
    void createArticleSuccess() throws Exception {
        // Arrange
        TutorApplication tutorApplication = new TutorApplication();
        tutorApplication.setStatus(TutorApplication.Status.ACCEPTED);
        
        Course course = new Course("Test Course", "Description", tutorId, BigDecimal.valueOf(100));
        setPrivateField(course, "id", courseId);
        
        Section section = new Section("Test Section", 0);
        setPrivateField(section, "id", sectionId);
        section.setCourse(course);
        
        Article article = new Article("Test Article", "Test Content", 0);
        setPrivateField(article, "id", articleId);
        article.setSection(section);
        
        ArticleController.ArticleRequest request = new ArticleController.ArticleRequest();
        request.title = "Test Article";
        request.content = "Test Content";
        request.position = 0;
        
        when(tutorApplicationService.getMostRecentApplicationByStudentId(tutorId)).thenReturn(Optional.of(tutorApplication));
        when(courseService.getCourseById(courseId)).thenReturn(Optional.of(course));
        when(sectionService.getSectionById(sectionId)).thenReturn(Optional.of(section));
        when(articleService.createArticle(eq(sectionId), eq(request.title), eq(request.content), eq(request.position))).thenReturn(article);
        
        // Act & Assert
        mockMvc.perform(post("/courses/{courseId}/sections/{sectionId}/articles", courseId, sectionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(principal))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Article created successfully."))
                .andExpect(jsonPath("$.articleId").value(articleId.toString()));
    }
    
    @Test
    @DisplayName("POST /courses/{courseId}/sections/{sectionId}/articles - Forbidden (Not Accepted Tutor)")
    void createArticleForbiddenNotAcceptedTutor() throws Exception {
        // Arrange
        TutorApplication tutorApplication = new TutorApplication();
        tutorApplication.setStatus(TutorApplication.Status.PENDING);
        
        ArticleController.ArticleRequest request = new ArticleController.ArticleRequest();
        request.title = "Test Article";
        request.content = "Test Content";
        request.position = 0;
        
        when(tutorApplicationService.getMostRecentApplicationByStudentId(tutorId)).thenReturn(Optional.of(tutorApplication));
        
        // Act & Assert
        mockMvc.perform(post("/courses/{courseId}/sections/{sectionId}/articles", courseId, sectionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(principal))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You are not allowed to modify this course. Tutor application must be ACCEPTED."));
    }
    
    @Test
    @DisplayName("POST /courses/{courseId}/sections/{sectionId}/articles - Forbidden (Not Course Owner)")
    void createArticleForbiddenNotCourseOwner() throws Exception {
        // Arrange
        UUID anotherTutorId = UUID.randomUUID();
        TutorApplication tutorApplication = new TutorApplication();
        tutorApplication.setStatus(TutorApplication.Status.ACCEPTED);
        
        Course course = new Course("Test Course", "Description", anotherTutorId, BigDecimal.valueOf(100));
        setPrivateField(course, "id", courseId);
        
        ArticleController.ArticleRequest request = new ArticleController.ArticleRequest();
        request.title = "Test Article";
        request.content = "Test Content";
        request.position = 0;
        
        when(tutorApplicationService.getMostRecentApplicationByStudentId(tutorId)).thenReturn(Optional.of(tutorApplication));
        when(courseService.getCourseById(courseId)).thenReturn(Optional.of(course));
        
        // Act & Assert
        mockMvc.perform(post("/courses/{courseId}/sections/{sectionId}/articles", courseId, sectionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(principal))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You are not the owner of this course."));
    }
    
    @Test
    @DisplayName("POST /courses/{courseId}/sections/{sectionId}/articles - Not Found (Section not in Course)")
    void createArticleNotFoundSectionNotInCourse() throws Exception {
        // Arrange
        TutorApplication tutorApplication = new TutorApplication();
        tutorApplication.setStatus(TutorApplication.Status.ACCEPTED);
        
        Course course = new Course("Test Course", "Description", tutorId, BigDecimal.valueOf(100));
        setPrivateField(course, "id", courseId);
        
        Course anotherCourse = new Course("Another Course", "Description", tutorId, BigDecimal.valueOf(100));
        UUID anotherCourseId = UUID.randomUUID();
        setPrivateField(anotherCourse, "id", anotherCourseId);
        
        Section section = new Section("Test Section", 0);
        setPrivateField(section, "id", sectionId);
        section.setCourse(anotherCourse); // Section belongs to another course
        
        ArticleController.ArticleRequest request = new ArticleController.ArticleRequest();
        request.title = "Test Article";
        request.content = "Test Content";
        request.position = 0;
        
        when(tutorApplicationService.getMostRecentApplicationByStudentId(tutorId)).thenReturn(Optional.of(tutorApplication));
        when(courseService.getCourseById(courseId)).thenReturn(Optional.of(course));
        when(sectionService.getSectionById(sectionId)).thenReturn(Optional.of(section));
        
        // Act & Assert
        mockMvc.perform(post("/courses/{courseId}/sections/{sectionId}/articles", courseId, sectionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(principal))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Section not found in this course."));
    }
    
    @Test
    @DisplayName("GET /courses/{courseId}/sections/{sectionId}/articles - Success")
    void getArticlesSuccess() throws Exception {
        // Arrange
        Course course = new Course("Test Course", "Description", tutorId, BigDecimal.valueOf(100));
        setPrivateField(course, "id", courseId);
        
        Section section = new Section("Test Section", 0);
        setPrivateField(section, "id", sectionId);
        section.setCourse(course);
        
        Article article1 = new Article("Test Article 1", "Test Content 1", 0);
        UUID article1Id = UUID.randomUUID();
        setPrivateField(article1, "id", article1Id);
        article1.setSection(section);
        
        Article article2 = new Article("Test Article 2", "Test Content 2", 1);
        UUID article2Id = UUID.randomUUID();
        setPrivateField(article2, "id", article2Id);
        article2.setSection(section);
        
        List<Article> articles = Arrays.asList(article1, article2);
        
        when(sectionService.getSectionById(sectionId)).thenReturn(Optional.of(section));
        when(articleService.getArticlesBySectionId(sectionId)).thenReturn(articles);
        
        // Act & Assert
        mockMvc.perform(get("/courses/{courseId}/sections/{sectionId}/articles", courseId, sectionId)
                .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.articles.length()").value(2))
                .andExpect(jsonPath("$.articles[0].title").value("Test Article 1"))
                .andExpect(jsonPath("$.articles[1].title").value("Test Article 2"));
    }
    
    @Test
    @DisplayName("GET /courses/{courseId}/sections/{sectionId}/articles - Not Found (Section not in Course)")
    void getArticlesNotFoundSectionNotInCourse() throws Exception {
        // Arrange
        Course anotherCourse = new Course("Another Course", "Description", tutorId, BigDecimal.valueOf(100));
        UUID anotherCourseId = UUID.randomUUID();
        setPrivateField(anotherCourse, "id", anotherCourseId);
        
        Section section = new Section("Test Section", 0);
        setPrivateField(section, "id", sectionId);
        section.setCourse(anotherCourse);
        
        when(sectionService.getSectionById(sectionId)).thenReturn(Optional.of(section));
        
        // Act & Assert
        mockMvc.perform(get("/courses/{courseId}/sections/{sectionId}/articles", courseId, sectionId)
                .principal(principal))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Section not found in this course."));
    }
    
    @Test
    @DisplayName("GET /courses/{courseId}/sections/{sectionId}/articles/{articleId} - Success")
    void getArticleByIdSuccess() throws Exception {
        // Arrange
        Course course = new Course("Test Course", "Description", tutorId, BigDecimal.valueOf(100));
        setPrivateField(course, "id", courseId);
        
        Section section = new Section("Test Section", 0);
        setPrivateField(section, "id", sectionId);
        section.setCourse(course);
        
        Article article = new Article("Test Article", "Test Content", 0);
        setPrivateField(article, "id", articleId);
        article.setSection(section);
        
        when(articleService.getArticleById(articleId)).thenReturn(Optional.of(article));
        
        // Act & Assert
        mockMvc.perform(get("/courses/{courseId}/sections/{sectionId}/articles/{articleId}", courseId, sectionId, articleId)
                .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.article.id").value(articleId.toString()))
                .andExpect(jsonPath("$.article.title").value("Test Article"));
    }
    
    @Test
    @DisplayName("GET /courses/{courseId}/sections/{sectionId}/articles/{articleId} - Not Found (Article not in Section)")
    void getArticleByIdNotFoundArticleNotInSection() throws Exception {
        // Arrange
        UUID anotherSectionId = UUID.randomUUID();
        Course course = new Course("Test Course", "Description", tutorId, BigDecimal.valueOf(100));
        setPrivateField(course, "id", courseId);
        
        Section anotherSection = new Section("Another Section", 0);
        setPrivateField(anotherSection, "id", anotherSectionId);
        anotherSection.setCourse(course);
        
        Article article = new Article("Test Article", "Test Content", 0);
        setPrivateField(article, "id", articleId);
        article.setSection(anotherSection);
        
        when(articleService.getArticleById(articleId)).thenReturn(Optional.of(article));
        
        // Act & Assert
        mockMvc.perform(get("/courses/{courseId}/sections/{sectionId}/articles/{articleId}", courseId, sectionId, articleId)
                .principal(principal))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Article not found in this section/course."));
    }
    
    @Test
    @DisplayName("PUT /courses/{courseId}/sections/{sectionId}/articles/{articleId} - Success")
    void updateArticleSuccess() throws Exception {
        // Arrange
        TutorApplication tutorApplication = new TutorApplication();
        tutorApplication.setStatus(TutorApplication.Status.ACCEPTED);
        
        Course course = new Course("Test Course", "Description", tutorId, BigDecimal.valueOf(100));
        setPrivateField(course, "id", courseId);
        
        Section section = new Section("Test Section", 0);
        setPrivateField(section, "id", sectionId);
        section.setCourse(course);
        
        Article article = new Article("Original Title", "Original Content", 0);
        setPrivateField(article, "id", articleId);
        article.setSection(section);
        
        Article updatedArticle = new Article("Updated Title", "Updated Content", 1);
        setPrivateField(updatedArticle, "id", articleId);
        updatedArticle.setSection(section);
        
        ArticleController.ArticleRequest request = new ArticleController.ArticleRequest();
        request.title = "Updated Title";
        request.content = "Updated Content";
        request.position = 1;
        
        when(tutorApplicationService.getMostRecentApplicationByStudentId(tutorId)).thenReturn(Optional.of(tutorApplication));
        when(courseService.getCourseById(courseId)).thenReturn(Optional.of(course));
        when(articleService.getArticleById(articleId)).thenReturn(Optional.of(article));
        when(articleService.updateArticle(eq(articleId), eq(request.title), eq(request.content), eq(request.position)))
                .thenReturn(Optional.of(updatedArticle));
        
        // Act & Assert
        mockMvc.perform(put("/courses/{courseId}/sections/{sectionId}/articles/{articleId}", courseId, sectionId, articleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Article updated successfully."))
                .andExpect(jsonPath("$.article.title").value("Updated Title"));
    }
    
    @Test
    @DisplayName("DELETE /courses/{courseId}/sections/{sectionId}/articles/{articleId} - Success")
    void deleteArticleSuccess() throws Exception {
        // Arrange
        TutorApplication tutorApplication = new TutorApplication();
        tutorApplication.setStatus(TutorApplication.Status.ACCEPTED);
        
        Course course = new Course("Test Course", "Description", tutorId, BigDecimal.valueOf(100));
        setPrivateField(course, "id", courseId);
        
        Section section = new Section("Test Section", 0);
        setPrivateField(section, "id", sectionId);
        section.setCourse(course);
        
        Article article = new Article("Test Article", "Test Content", 0);
        setPrivateField(article, "id", articleId);
        article.setSection(section);
        
        when(tutorApplicationService.getMostRecentApplicationByStudentId(tutorId)).thenReturn(Optional.of(tutorApplication));
        when(courseService.getCourseById(courseId)).thenReturn(Optional.of(course));
        when(articleService.getArticleById(articleId)).thenReturn(Optional.of(article));
        when(articleService.deleteArticle(articleId)).thenReturn(true);
        
        // Act & Assert
        mockMvc.perform(delete("/courses/{courseId}/sections/{sectionId}/articles/{articleId}", courseId, sectionId, articleId)
                .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Article deleted successfully."));
    }
    
    @Test
    @DisplayName("DELETE /courses/{courseId}/sections/{sectionId}/articles/{articleId} - Not Found")
    void deleteArticleNotFound() throws Exception {
        // Arrange
        TutorApplication tutorApplication = new TutorApplication();
        tutorApplication.setStatus(TutorApplication.Status.ACCEPTED);
        
        Course course = new Course("Test Course", "Description", tutorId, BigDecimal.valueOf(100));
        setPrivateField(course, "id", courseId);
        
        when(tutorApplicationService.getMostRecentApplicationByStudentId(tutorId)).thenReturn(Optional.of(tutorApplication));
        when(courseService.getCourseById(courseId)).thenReturn(Optional.of(course));
        when(articleService.getArticleById(articleId)).thenReturn(Optional.empty());
        
        // Act & Assert
        mockMvc.perform(delete("/courses/{courseId}/sections/{sectionId}/articles/{articleId}", courseId, sectionId, articleId)
                .principal(principal))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Article not found in this section/course."));
    }
    
    // Helper method to set private fields using reflection
    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field " + fieldName, e);
        }
    }
}