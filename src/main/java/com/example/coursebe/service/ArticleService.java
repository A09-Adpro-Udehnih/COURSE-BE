package com.example.coursebe.service;

import com.example.coursebe.model.Article;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ArticleService {
    List<Article> getArticlesBySectionId(UUID sectionId);
    Optional<Article> getArticleById(UUID id);
    Article createArticle(UUID sectionId, String title, String content, Integer position);
    Optional<Article> updateArticle(UUID id, String title, String content, Integer position);
    boolean deleteArticle(UUID id);
    List<Article> reorderArticles(UUID sectionId, List<UUID> articleIds);
}