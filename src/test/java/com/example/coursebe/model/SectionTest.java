package com.example.coursebe.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SectionTest {
    
    private Section section;
    private String title;
    private Integer position;
    
    @BeforeEach
    void setUp() {
        title = "Test Section";
        position = 1;
        section = new Section(title, position);
    }
    
    @Test
    void testConstructor() {
        assertNotNull(section.getId());
        assertEquals(title, section.getTitle());
        assertEquals(position, section.getPosition());
        assertNull(section.getCourse());
        assertNotNull(section.getArticles());
        assertTrue(section.getArticles().isEmpty());
    }
    
    @Test
    void testDefaultConstructor() {
        Section defaultSection = new Section();
        assertNotNull(defaultSection.getId());
        assertNotNull(defaultSection.getArticles());
        assertTrue(defaultSection.getArticles().isEmpty());
    }
    
    @Test
    void testConstructorWithNullPosition() {
        Section sectionWithNullPosition = new Section(title, null);
        assertEquals(0, sectionWithNullPosition.getPosition());
    }
    
    @Test
    void testOnCreate() {
        section.onCreate();
        assertNotNull(section.getCreatedAt());
        assertNotNull(section.getUpdatedAt());
        // Check if timestamps are very close instead of exactly equal
        assertTrue(java.time.Duration.between(section.getCreatedAt(), section.getUpdatedAt()).abs().toMillis() < 1000);
        
        Section sectionWithNullPosition = new Section(title, null);
        sectionWithNullPosition.onCreate();
        assertEquals(0, sectionWithNullPosition.getPosition());
    }
    
    @Test
    void testOnUpdate() {
        section.onCreate(); // Set initial timestamps
        LocalDateTime initialUpdateTime = section.getUpdatedAt();
        
        // Wait a small amount of time to ensure timestamp difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        section.onUpdate();
        assertTrue(section.getUpdatedAt().isAfter(initialUpdateTime));
    }
    
    @Test
    void testSetCourse() {
        Course course = new Course("Test Course", "Test Description", UUID.randomUUID(), new BigDecimal("99.99"));
        section.setCourse(course);
        assertEquals(course, section.getCourse());
    }
    
    @Test
    void testAddArticle() {
        Article article = new Article("Test Article", "Test Content", 1);
        section.addArticle(article);
        
        assertEquals(1, section.getArticles().size());
        assertTrue(section.getArticles().contains(article));
        assertEquals(section, article.getSection());
    }
    
    @Test
    void testRemoveArticle() {
        Article article = new Article("Test Article", "Test Content", 1);
        section.addArticle(article);
        assertEquals(1, section.getArticles().size());
        
        section.removeArticle(article);
        assertEquals(0, section.getArticles().size());
        assertNull(article.getSection());
    }
    
    @Test
    void testSetters() {
        String newTitle = "Updated Section Title";
        section.setTitle(newTitle);
        assertEquals(newTitle, section.getTitle());
        
        Integer newPosition = 2;
        section.setPosition(newPosition);
        assertEquals(newPosition, section.getPosition());
    }
}