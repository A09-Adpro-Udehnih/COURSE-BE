package com.example.coursebe.repository;

import com.example.coursebe.model.Article;
import com.example.coursebe.model.Course;
import com.example.coursebe.model.Section;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ArticleRepository
 */
@DataJpaTest
public class ArticleRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private ArticleRepository articleRepository;
    
    private Course course;
    private Section section1;
    private Section section2;
    private Article article1;
    private Article article2;
    private Article article3;
    
    @BeforeEach
    void setUp() {
        UUID tutorId = UUID.randomUUID();
        
        // Create test course
        course = new Course("Java Programming", "Learn Java basics", tutorId, new BigDecimal("99.99"));
        entityManager.persist(course);
        
        // Create test sections
        section1 = new Section("Java Fundamentals", 1);
        section1.setCourse(course);
        
        section2 = new Section("Object-Oriented Programming", 2);
        section2.setCourse(course);
        
        entityManager.persist(section1);
        entityManager.persist(section2);
        
        // Create test articles
        article1 = new Article("Introduction to Java", "Java is a programming language...", 1);
        article1.setSection(section1);
        
        article2 = new Article("Variables and Data Types", "In Java, variables are...", 2);
        article2.setSection(section1);
        
        article3 = new Article("Classes and Objects", "Object-oriented programming...", 1);
        article3.setSection(section2);
        
        entityManager.persist(article1);
        entityManager.persist(article2);
        entityManager.persist(article3);
        entityManager.flush();
    }
    
    @Test
    @DisplayName("Should find all articles")
    void findAllArticles() {
        // when
        List<Article> articles = articleRepository.findAll();
        
        // then
        assertNotNull(articles);
        assertEquals(3, articles.size());
    }
    
    @Test
    @DisplayName("Should find article by ID")
    void findArticleById() {
        // when
        Optional<Article> found = articleRepository.findById(article1.getId());
        
        // then
        assertTrue(found.isPresent());
        assertEquals(article1.getTitle(), found.get().getTitle());
        assertEquals(article1.getContent(), found.get().getContent());
        assertEquals(article1.getPosition(), found.get().getPosition());
        assertEquals(section1.getId(), found.get().getSection().getId());
    }
    
    @Test
    @DisplayName("Should find articles by section")
    void findArticlesBySection() {
        // when
        List<Article> articles = articleRepository.findBySection(section1);
        
        // then
        assertNotNull(articles);
        assertEquals(2, articles.size());
        assertTrue(articles.stream().allMatch(article -> article.getSection().getId().equals(section1.getId())));
    }
    
    @Test
    @DisplayName("Should find articles by section ID")
    void findArticlesBySectionId() {
        // when
        List<Article> articles = articleRepository.findBySectionId(section1.getId());
        
        // then
        assertNotNull(articles);
        assertEquals(2, articles.size());
        assertTrue(articles.stream().allMatch(article -> article.getSection().getId().equals(section1.getId())));
    }
    
    @Test
    @DisplayName("Should find articles by section ordered by position")
    void findArticlesBySectionOrderedByPosition() {
        // Create a new article with position 0
        Article article0 = new Article("Preface", "Before we start...", 0);
        article0.setSection(section1);
        entityManager.persist(article0);
        entityManager.flush();
        
        // when
        List<Article> orderedArticles = articleRepository.findBySectionOrderByPositionAsc(section1);
        
        // then
        assertNotNull(orderedArticles);
        assertEquals(3, orderedArticles.size());
        assertEquals(0, orderedArticles.get(0).getPosition());
        assertEquals(1, orderedArticles.get(1).getPosition());
        assertEquals(2, orderedArticles.get(2).getPosition());
    }
    
    @Test
    @DisplayName("Should save article")
    void saveArticle() {
        // given
        Article newArticle = new Article("Control Flow", "In this article...", 3);
        newArticle.setSection(section1);
        
        // when
        Article saved = articleRepository.save(newArticle);
        
        // then
        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("Control Flow", saved.getTitle());
        assertEquals("In this article...", saved.getContent());
        assertEquals(3, saved.getPosition());
        assertEquals(section1.getId(), saved.getSection().getId());
        
        // when
        Optional<Article> found = articleRepository.findById(saved.getId());
        
        // then
        assertTrue(found.isPresent());
    }
    
    @Test
    @DisplayName("Should delete article")
    void deleteArticle() {
        // given
        Article articleToDelete = article3;
        
        // when
        articleRepository.delete(articleToDelete);
        Optional<Article> found = articleRepository.findById(articleToDelete.getId());
        
        // then
        assertFalse(found.isPresent());
    }
}