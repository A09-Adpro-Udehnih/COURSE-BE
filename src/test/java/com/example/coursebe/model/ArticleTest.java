package com.example.coursebe.model;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ArticleTest {
    
    private Article article;
    private String title;
    private String content;
    private Integer position;
    
    @BeforeEach
    void setUp() {
        title = "Test Article";
        content = "This is test content for the article";
        position = 1;
        article = new Article(title, content, position);
    }
    
    @Test
    void testConstructor() {
        assertNotNull(article.getId());
        assertEquals(title, article.getTitle());
        assertEquals(content, article.getContent());
        assertEquals(position, article.getPosition());
        assertNull(article.getSection());
    }
    
    @Test
    void testDefaultConstructor() {
        Article defaultArticle = new Article();
        assertNotNull(defaultArticle.getId());
    }
    
    @Test
    void testConstructorWithNullPosition() {
        Article articleWithNullPosition = new Article(title, content, null);
        assertEquals(0, articleWithNullPosition.getPosition());
    }
    
    @Test
    void testOnCreate() {
        article.onCreate();
        assertNotNull(article.getCreatedAt());
        assertNotNull(article.getUpdatedAt());
        // Check if timestamps are very close instead of exactly equal
        assertTrue(java.time.Duration.between(article.getCreatedAt(), article.getUpdatedAt()).abs().toMillis() < 1000);
        
        Article articleWithNullPosition = new Article(title, content, null);
        articleWithNullPosition.onCreate();
        assertEquals(0, articleWithNullPosition.getPosition());
    }
    
    @Test
    void testOnUpdate() {
        article.onCreate(); // Set initial timestamps
        LocalDateTime initialUpdateTime = article.getUpdatedAt();
        
        // Wait a small amount of time to ensure timestamp difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        article.onUpdate();
        assertTrue(article.getUpdatedAt().isAfter(initialUpdateTime));
    }
    
    @Test
    void testSetSection() {
        Section section = new Section("Test Section", 1);
        article.setSection(section);
        assertEquals(section, article.getSection());
    }
    
    @Test
    void testSetters() {
        String newTitle = "Updated Article Title";
        article.setTitle(newTitle);
        assertEquals(newTitle, article.getTitle());
        
        String newContent = "Updated article content";
        article.setContent(newContent);
        assertEquals(newContent, article.getContent());
        
        Integer newPosition = 2;
        article.setPosition(newPosition);
        assertEquals(newPosition, article.getPosition());
    }
}