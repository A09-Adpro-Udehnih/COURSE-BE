package com.example.coursebe.service;

import com.example.coursebe.model.Article;
import com.example.coursebe.model.Section;
import com.example.coursebe.repository.ArticleRepository;
import com.example.coursebe.repository.SectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ArticleServiceImplTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private SectionRepository sectionRepository;

    @InjectMocks
    private ArticleServiceImpl articleService;

    private UUID sectionId;
    private UUID articleId;
    private Section testSection;
    private Article testArticle;
    private List<Article> testArticles;

    @BeforeEach
    void setUp() {
        sectionId = UUID.randomUUID();
        articleId = UUID.randomUUID();
        
        testSection = new Section("Test Section", 1);
        // Set section ID using reflection
        try {
            java.lang.reflect.Field field = Section.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(testSection, sectionId);
        } catch (Exception e) {
            fail("Failed to set section ID");
        }
        
        testArticle = new Article("Test Article", "Test Content", 1);
        testArticle.setSection(testSection);
        // Set article ID using reflection
        try {
            java.lang.reflect.Field field = Article.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(testArticle, articleId);
        } catch (Exception e) {
            fail("Failed to set article ID");
        }
        
        Article article2 = new Article("Second Article", "Second Content", 2);
        article2.setSection(testSection);
        // Set article2 ID using reflection
        try {
            java.lang.reflect.Field field = Article.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(article2, UUID.randomUUID());
        } catch (Exception e) {
            fail("Failed to set article ID");
        }
        
        testArticles = Arrays.asList(testArticle, article2);
    }

    @Test
    @DisplayName("Should get articles by section ID")
    void getArticlesBySectionId() {
        // Given
        when(articleRepository.findBySectionId(sectionId)).thenReturn(testArticles);
        
        // When
        List<Article> result = articleService.getArticlesBySectionId(sectionId);
        
        // Then
        assertEquals(2, result.size());
        assertEquals(testArticles.get(0).getTitle(), result.get(0).getTitle());
        assertEquals(testArticles.get(0).getPosition(), result.get(0).getPosition());
        verify(articleRepository).findBySectionId(sectionId);
    }

    @Test
    @DisplayName("Should get article by ID")
    void getArticleById() {
        // Given
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(testArticle));
        
        // When
        Optional<Article> result = articleService.getArticleById(articleId);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(testArticle, result.get());
        verify(articleRepository).findById(articleId);
    }

    @Test
    @DisplayName("Should return empty optional when article not found")
    void getArticleByIdNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(articleRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        
        // When
        Optional<Article> result = articleService.getArticleById(nonExistentId);
        
        // Then
        assertFalse(result.isPresent());
        verify(articleRepository).findById(nonExistentId);
    }

    @Test
    @DisplayName("Should create article with provided position")
    void createArticleWithPosition() {
        // Given
        String title = "New Article";
        String content = "New Content";
        Integer position = 3;
        
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(testSection));
        when(articleRepository.save(any(Article.class))).thenAnswer(i -> {
            Article article = (Article) i.getArguments()[0];
            // Set article ID using reflection
            try {
                java.lang.reflect.Field field = Article.class.getDeclaredField("id");
                field.setAccessible(true);
                field.set(article, UUID.randomUUID());
            } catch (Exception e) {
                fail("Failed to set article ID");
            }
            return article;
        });
        
        // When
        Article result = articleService.createArticle(sectionId, title, content, position);
        
        // Then
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertEquals(content, result.getContent());
        assertEquals(position, result.getPosition());
        assertEquals(sectionId, result.getSection().getId());
        verify(sectionRepository).findById(sectionId);
        verify(articleRepository).save(any(Article.class));
    }

    @Test
    @DisplayName("Should create article with calculated position when position is null")
    void createArticleWithCalculatedPosition() {
        // Given
        String title = "New Article";
        String content = "New Content";
        Integer position = null;
        List<Article> existingArticles = new ArrayList<>(testArticles);
        
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(testSection));
        when(articleRepository.findBySection(testSection)).thenReturn(existingArticles);
        when(articleRepository.save(any(Article.class))).thenAnswer(i -> {
            Article article = (Article) i.getArguments()[0];
            // Set article ID using reflection
            try {
                java.lang.reflect.Field field = Article.class.getDeclaredField("id");
                field.setAccessible(true);
                field.set(article, UUID.randomUUID());
            } catch (Exception e) {
                fail("Failed to set article ID");
            }
            return article;
        });
        
        // When
        Article result = articleService.createArticle(sectionId, title, content, position);
        
        // Then
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertEquals(content, result.getContent());
        assertEquals(2, result.getPosition()); // Max position (2) + 1 = 3
        assertEquals(sectionId, result.getSection().getId());
        verify(sectionRepository).findById(sectionId);
        verify(articleRepository).findBySection(testSection);
        verify(articleRepository).save(any(Article.class));
    }

    @Test
    @DisplayName("Should return null when creating article for non-existent section")
    void createArticleForNonExistentSection() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        String title = "New Article";
        String content = "New Content";
        Integer position = 1;
        
        when(sectionRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        
        // When
        Article result = articleService.createArticle(nonExistentId, title, content, position);
        
        // Then
        assertNull(result);
        verify(sectionRepository).findById(nonExistentId);
        verify(articleRepository, never()).save(any(Article.class));
    }

    @Test
    @DisplayName("Should update article")
    void updateArticle() {
        // Given
        String updatedTitle = "Updated Article";
        String updatedContent = "Updated Content";
        Integer updatedPosition = 3;
        
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(testArticle));
        when(articleRepository.save(any(Article.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // When
        Optional<Article> result = articleService.updateArticle(articleId, updatedTitle, updatedContent, updatedPosition);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(updatedTitle, result.get().getTitle());
        assertEquals(updatedContent, result.get().getContent());
        assertEquals(updatedPosition, result.get().getPosition());
        verify(articleRepository).findById(articleId);
        verify(articleRepository).save(any(Article.class));
    }

    @Test
    @DisplayName("Should return empty optional when updating non-existent article")
    void updateNonExistentArticle() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(articleRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        
        // When
        Optional<Article> result = articleService.updateArticle(nonExistentId, "Title", "Content", 1);
        
        // Then
        assertFalse(result.isPresent());
        verify(articleRepository).findById(nonExistentId);
        verify(articleRepository, never()).save(any(Article.class));
    }

    @Test
    @DisplayName("Should delete article")
    void deleteArticle() {
        // Given
        when(articleRepository.existsById(articleId)).thenReturn(true);
        
        // When
        boolean result = articleService.deleteArticle(articleId);
        
        // Then
        assertTrue(result);
        verify(articleRepository).existsById(articleId);
        verify(articleRepository).deleteById(articleId);
    }

    @Test
    @DisplayName("Should return false when deleting non-existent article")
    void deleteNonExistentArticle() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(articleRepository.existsById(nonExistentId)).thenReturn(false);
        
        // When
        boolean result = articleService.deleteArticle(nonExistentId);
        
        // Then
        assertFalse(result);
        verify(articleRepository).existsById(nonExistentId);
        verify(articleRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    @DisplayName("Should reorder articles")
    void reorderArticles() {
        // Given
        List<UUID> orderedIds = Arrays.asList(
            testArticles.get(1).getId(), 
            testArticles.get(0).getId()
        );
        
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(testSection));
        when(articleRepository.findBySection(testSection)).thenReturn(testArticles);
        when(articleRepository.save(any(Article.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // When
        List<Article> result = articleService.reorderArticles(sectionId, orderedIds);
        
        // Then
        assertEquals(2, result.size());
        assertEquals(testArticles.get(1).getId(), result.get(0).getId());
        assertEquals(0, result.get(0).getPosition());
        assertEquals(testArticles.get(0).getId(), result.get(1).getId());
        assertEquals(1, result.get(1).getPosition());
        verify(sectionRepository).findById(sectionId);
        verify(articleRepository).findBySection(testSection);
        verify(articleRepository, times(2)).save(any(Article.class));
    }

    @Test
    @DisplayName("Should return empty list when reordering articles for non-existent section")
    void reorderArticlesForNonExistentSection() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        List<UUID> orderedIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        
        when(sectionRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        
        // When
        List<Article> result = articleService.reorderArticles(nonExistentId, orderedIds);
        
        // Then
        assertTrue(result.isEmpty());
        verify(sectionRepository).findById(nonExistentId);
        verify(articleRepository, never()).save(any(Article.class));
    }
}