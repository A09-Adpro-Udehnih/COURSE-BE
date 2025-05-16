package com.example.coursebe.service;

import com.example.coursebe.model.Article;
import com.example.coursebe.model.Section;
import com.example.coursebe.repository.ArticleRepository;
import com.example.coursebe.repository.SectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of ArticleService
 */
@Service
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final SectionRepository sectionRepository;

    @Autowired
    public ArticleServiceImpl(ArticleRepository articleRepository, SectionRepository sectionRepository) {
        this.articleRepository = articleRepository;
        this.sectionRepository = sectionRepository;
    }

    @Override
    public List<Article> getArticlesBySectionId(UUID sectionId) {
        return articleRepository.findBySectionId(sectionId).stream()
                .sorted((a1, a2) -> a1.getPosition().compareTo(a2.getPosition()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Article> getArticleById(UUID id) {
        return articleRepository.findById(id);
    }

    @Override
    @Transactional
    public Article createArticle(UUID sectionId, String title, String content, Integer position) {
        // Validate inputs
        if (sectionId == null) {
            throw new IllegalArgumentException("Section ID cannot be null");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Article title cannot be empty");
        }

        // Find section
        Optional<Section> optionalSection = sectionRepository.findById(sectionId);
        if (optionalSection.isEmpty()) {
            return null;
        }
        Section section = optionalSection.get();

        // If position is null, calculate next position
        if (position == null) {
            List<Article> existingArticles = articleRepository.findBySection(section);
            position = existingArticles.size();
        }

        // Create and link article
        Article article = new Article(title, content, position);
        article.setSection(section);

        // Save and return
        return articleRepository.save(article);
    }

    @Override
    @Transactional
    public Optional<Article> updateArticle(UUID id, String title, String content, Integer position) {
        // Validate inputs
        if (id == null) {
            throw new IllegalArgumentException("Article ID cannot be null");
        }

        // Find article
        Optional<Article> optionalArticle = articleRepository.findById(id);
        if (optionalArticle.isEmpty()) {
            return Optional.empty();
        }
        
        Article article = optionalArticle.get();
        
        // Update article
        if (title != null && !title.trim().isEmpty()) {
            article.setTitle(title);
        }
        
        if (content != null) {
            article.setContent(content);
        }
        
        if (position != null) {
            article.setPosition(position);
        }
        
        // Save and return
        Article updatedArticle = articleRepository.save(article);
        return Optional.of(updatedArticle);
    }

    @Override
    @Transactional
    public boolean deleteArticle(UUID id) {
        // Validate inputs
        if (id == null) {
            throw new IllegalArgumentException("Article ID cannot be null");
        }
        
        // Check if article exists
        if (articleRepository.existsById(id)) {
            articleRepository.deleteById(id);
            return true;
        }
        
        return false;
    }

    @Override
    @Transactional
    public List<Article> reorderArticles(UUID sectionId, List<UUID> articleIds) {
        // Validate inputs
        if (sectionId == null) {
            throw new IllegalArgumentException("Section ID cannot be null");
        }
        if (articleIds == null || articleIds.isEmpty()) {
            throw new IllegalArgumentException("Article IDs list cannot be null or empty");
        }

        // Check section exists
        Optional<Section> optionalSection = sectionRepository.findById(sectionId);
        if (optionalSection.isEmpty()) {
            return new ArrayList<>();
        }
        Section section = optionalSection.get();

        // Get all articles of the section
        List<Article> articles = articleRepository.findBySection(section);

        // Make sure all specified articles belong to the section
        for (UUID id : articleIds) {
            boolean found = articles.stream().anyMatch(article -> article.getId().equals(id));
            if (!found) {
                throw new IllegalArgumentException("All articles must belong to the specified section");
            }
        }

        // Update positions
        List<Article> updatedArticles = new ArrayList<>();
        for (int i = 0; i < articleIds.size(); i++) {
            UUID articleId = articleIds.get(i);
            Optional<Article> optionalArticle = articles.stream()
                    .filter(a -> a.getId().equals(articleId))
                    .findFirst();
            if (optionalArticle.isPresent()) {
                Article article = optionalArticle.get();
                article.setPosition(i);
                updatedArticles.add(articleRepository.save(article));
            }
        }
        return updatedArticles;
    }
}