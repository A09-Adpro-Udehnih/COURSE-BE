package com.example.coursebe.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.coursebe.model.Article;
import com.example.coursebe.model.Section;

@Repository
public interface ArticleRepository extends JpaRepository<Article, UUID> {
    List<Article> findBySection(Section section);
    List<Article> findBySectionOrderByPositionAsc(Section section);
    List<Article> findBySectionId(UUID sectionId);
}