package com.example.coursebe.controller;

import com.example.coursebe.model.Article;
import com.example.coursebe.model.Course;
import com.example.coursebe.model.Section;
import com.example.coursebe.model.TutorApplication;
import com.example.coursebe.service.ArticleService;
import com.example.coursebe.service.CourseService;
import com.example.coursebe.service.SectionService;
import com.example.coursebe.service.TutorApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/courses/{courseId}/sections/{sectionId}/articles")
public class ArticleController {

    private final ArticleService articleService;
    private final SectionService sectionService;
    private final CourseService courseService;
    private final TutorApplicationService tutorApplicationService;

    public ArticleController(ArticleService articleService, SectionService sectionService, CourseService courseService, TutorApplicationService tutorApplicationService) {
        this.articleService = articleService;
        this.sectionService = sectionService;
        this.courseService = courseService;
        this.tutorApplicationService = tutorApplicationService;
    }

    // DTO for Article creation and update
    public static class ArticleRequest {
        public String title;
        public String content;
        public Integer position;
    }

    private ResponseEntity<?> checkTutorAndCourseOwnership(UUID courseId, Principal principal) {
        UUID tutorId = UUID.fromString(principal.getName());
        Optional<TutorApplication> appOpt = tutorApplicationService.getMostRecentApplicationByStudentId(tutorId);
        if (appOpt.isEmpty() || appOpt.get().getStatus() != TutorApplication.Status.ACCEPTED) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.FORBIDDEN.value());
            resp.put("success", false);
            resp.put("message", "You are not allowed to modify this course. Tutor application must be ACCEPTED.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        }

        Optional<Course> courseOpt = courseService.getCourseById(courseId);
        if (courseOpt.isEmpty() || !courseOpt.get().getTutorId().equals(tutorId)) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.FORBIDDEN.value());
            resp.put("success", false);
            resp.put("message", "You are not the owner of this course.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        }
        return null;
    }

    private ResponseEntity<?> checkSectionBelongsToCourse(UUID courseId, UUID sectionId) {
        Optional<Section> sectionOpt = sectionService.getSectionById(sectionId);
        if (sectionOpt.isEmpty() || !sectionOpt.get().getCourse().getId().equals(courseId)) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.NOT_FOUND.value());
            resp.put("success", false);
            resp.put("message", "Section not found in this course.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }
        return null;
    }    
    
    @PostMapping
    public ResponseEntity<?> createArticle(@PathVariable UUID courseId, @PathVariable UUID sectionId, @RequestBody ArticleRequest req, Principal principal) {
        ResponseEntity<?> ownershipCheck = checkTutorAndCourseOwnership(courseId, principal);
        if (ownershipCheck != null) {
            return ownershipCheck;
        }

        ResponseEntity<?> sectionCheck = checkSectionBelongsToCourse(courseId, sectionId);
        if (sectionCheck != null) {
            return sectionCheck;
        }

        // Check if course is approved before allowing article creation
        Optional<Course> courseOpt = courseService.getCourseById(courseId);
        if (courseOpt.isEmpty()) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.NOT_FOUND.value());
            resp.put("success", false);
            resp.put("message", "Course not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }        Course course = courseOpt.get();
        
        // Check if course is approved before allowing article creation
        if (course.getStatus() == com.example.coursebe.enums.Status.PENDING) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.FORBIDDEN.value());
            resp.put("success", false);
            resp.put("message", "Cannot create articles for courses with PENDING status. Please wait for course approval.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        }
        
        if (course.getStatus() == com.example.coursebe.enums.Status.DENIED) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.FORBIDDEN.value());
            resp.put("success", false);
            resp.put("message", "Cannot create articles for DENIED courses.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        }        try {
            Article article = articleService.createArticle(sectionId, req.title, req.content, req.position);
            if (article == null) {
                Map<String, Object> resp = new HashMap<>();
                resp.put("code", HttpStatus.NOT_FOUND.value());
                resp.put("success", false);
                resp.put("message", "Section not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
            }
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.CREATED.value());
            resp.put("success", true);
            resp.put("message", "Article created successfully.");
            resp.put("articleId", article.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (IllegalArgumentException e) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.BAD_REQUEST.value());
            resp.put("success", false);
            resp.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
        }
    }

    @GetMapping
    public ResponseEntity<?> getArticles(@PathVariable UUID courseId, @PathVariable UUID sectionId, Principal principal) {
        ResponseEntity<?> sectionCheck = checkSectionBelongsToCourse(courseId, sectionId);
        if (sectionCheck != null) {
            return sectionCheck;
        }

        List<Article> articles = articleService.getArticlesBySectionId(sectionId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", HttpStatus.OK.value());
        resp.put("success", true);
        resp.put("articles", articles);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{articleId}")
    public ResponseEntity<?> getArticleById(@PathVariable UUID courseId, @PathVariable UUID sectionId, @PathVariable UUID articleId, Principal principal) {
        Optional<Article> articleOpt = articleService.getArticleById(articleId);
        if (articleOpt.isEmpty() || !articleOpt.get().getSection().getId().equals(sectionId) || !articleOpt.get().getSection().getCourse().getId().equals(courseId)) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.NOT_FOUND.value());
            resp.put("success", false);
            resp.put("message", "Article not found in this section/course.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("code", HttpStatus.OK.value());
        resp.put("success", true);
        resp.put("article", articleOpt.get());
        return ResponseEntity.ok(resp);
    }    @PutMapping("/{articleId}")
    public ResponseEntity<?> updateArticle(@PathVariable UUID courseId, @PathVariable UUID sectionId, @PathVariable UUID articleId, @RequestBody ArticleRequest req, Principal principal) {
        ResponseEntity<?> ownershipCheck = checkTutorAndCourseOwnership(courseId, principal);
        if (ownershipCheck != null) {
            return ownershipCheck;
        }

        // Check if course is approved before allowing article updates
        Optional<Course> courseOpt = courseService.getCourseById(courseId);
        if (courseOpt.isEmpty()) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.NOT_FOUND.value());
            resp.put("success", false);
            resp.put("message", "Course not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }        Course course = courseOpt.get();
        
        // Check if course is approved before allowing article updates
        if (course.getStatus() == com.example.coursebe.enums.Status.PENDING) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.FORBIDDEN.value());
            resp.put("success", false);
            resp.put("message", "Cannot modify articles for courses with PENDING status. Please wait for course approval.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        }
        
        if (course.getStatus() == com.example.coursebe.enums.Status.DENIED) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.FORBIDDEN.value());
            resp.put("success", false);
            resp.put("message", "Cannot modify articles for DENIED courses.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        }

        Optional<Article> articleOpt = articleService.getArticleById(articleId);
        if (articleOpt.isEmpty() || !articleOpt.get().getSection().getId().equals(sectionId) || !articleOpt.get().getSection().getCourse().getId().equals(courseId)) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.NOT_FOUND.value());
            resp.put("success", false);
            resp.put("message", "Article not found in this section/course.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }

        try {
            Optional<Article> updatedArticleOpt = articleService.updateArticle(articleId, req.title, req.content, req.position);
            if (updatedArticleOpt.isEmpty()) {
                Map<String, Object> resp = new HashMap<>();
                resp.put("code", HttpStatus.NOT_FOUND.value());
                resp.put("success", false);
                resp.put("message", "Article not found for update.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
            }
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.OK.value());
            resp.put("success", true);
            resp.put("message", "Article updated successfully.");
            resp.put("article", updatedArticleOpt.get());
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.BAD_REQUEST.value());
            resp.put("success", false);
            resp.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
        }
    }    @DeleteMapping("/{articleId}")
    public ResponseEntity<?> deleteArticle(@PathVariable UUID courseId, @PathVariable UUID sectionId, @PathVariable UUID articleId, Principal principal) {
        ResponseEntity<?> ownershipCheck = checkTutorAndCourseOwnership(courseId, principal);
        if (ownershipCheck != null) {
            return ownershipCheck;
        }

        // Check if course is approved before allowing article deletion
        Optional<Course> courseOpt = courseService.getCourseById(courseId);
        if (courseOpt.isEmpty()) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.NOT_FOUND.value());
            resp.put("success", false);
            resp.put("message", "Course not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }

        Course course = courseOpt.get();
        if (course.getStatus() != com.example.coursebe.enums.Status.ACCEPTED) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.FORBIDDEN.value());
            resp.put("success", false);
            resp.put("message", "Cannot delete articles for courses that are not approved. Current status: " + course.getStatus());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        }

        Optional<Article> articleOpt = articleService.getArticleById(articleId);
        if (articleOpt.isEmpty() || !articleOpt.get().getSection().getId().equals(sectionId) || !articleOpt.get().getSection().getCourse().getId().equals(courseId)) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", HttpStatus.NOT_FOUND.value());
            resp.put("success", false);
            resp.put("message", "Article not found in this section/course.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }

        boolean deleted = articleService.deleteArticle(articleId);
        Map<String, Object> resp = new HashMap<>();
        if (deleted) {
            resp.put("code", HttpStatus.OK.value());
            resp.put("success", true);
            resp.put("message", "Article deleted successfully.");
            return ResponseEntity.ok(resp);
        } else {
            resp.put("code", HttpStatus.NOT_FOUND.value());
            resp.put("success", false);
            resp.put("message", "Article not found for deletion.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }
    }
}
