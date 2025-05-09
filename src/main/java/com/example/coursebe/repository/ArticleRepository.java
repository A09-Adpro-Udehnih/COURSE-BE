package com.example.coursebe.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.coursebe.model.Article;
import com.example.coursebe.model.Section;

/**
 * Repository interface for Article entity
 * Provides CRUD operations and custom query methods for Article
 */
@Repository
public interface ArticleRepository extends JpaRepository<Article, UUID> {
    
    /**
     * Find all articles belonging to a specific section
     * @param section the section entity
     * @return list of articles for the section
     */
    List<Article> findBySection(Section section);
    
    /**
     * Find all articles belonging to a specific section, ordered by position
     * @param section the section entity
     * @return ordered list of articles for the section
     */
    List<Article> findBySectionOrderByPositionAsc(Section section);
    
    /**
     * Find all articles belonging to a section with the given ID
     * @param sectionId the section ID
     * @return list of articles for the section
     */
    List<Article> findBySectionId(UUID sectionId);

}
