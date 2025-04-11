package com.example.coursebe.repository;

import com.example.coursebe.model.Article;
import com.example.coursebe.model.Section;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ArticleRepository extends JpaRepository<Article, UUID> {
    List<Article> findBySection(Section section);
}
