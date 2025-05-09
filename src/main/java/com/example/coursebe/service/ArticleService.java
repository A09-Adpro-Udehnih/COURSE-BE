package com.example.coursebe.service;

import com.example.coursebe.model.Article;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for managing Article entities
 */
public interface ArticleService {
    
    /**
     * Get all articles for a section
     * @param sectionId Section ID
     * @return List of articles for the section, ordered by position
     */
    List<Article> getArticlesBySectionId(UUID sectionId);
    
    /**
     * Get article by ID
     * @param id Article ID
     * @return Optional containing article if found
     */
    Optional<Article> getArticleById(UUID id);
    
    /**
     * Create a new article for a section
     * @param sectionId Section ID
     * @param title Article title
     * @param content Article content
     * @param position Article position (optional, will determine automatically if null)
     * @return Created article or null if section not found
     */
    Article createArticle(UUID sectionId, String title, String content, Integer position);
    
    /**
     * Update an existing article
     * @param id Article ID
     * @param title Updated title
     * @param content Updated content
     * @param position Updated position
     * @return Updated article or empty optional if not found
     */
    Optional<Article> updateArticle(UUID id, String title, String content, Integer position);
    
    /**
     * Delete an article
     * @param id Article ID
     * @return true if deleted, false if not found
     */
    boolean deleteArticle(UUID id);
    
    /**
     * Reorder articles within a section
     * @param sectionId Section ID
     * @param articleIds Ordered list of article IDs
     * @return List of updated articles or empty list if section not found
     */
    List<Article> reorderArticles(UUID sectionId, List<UUID> articleIds);
}