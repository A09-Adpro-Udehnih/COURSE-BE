package com.example.coursebe.dto.builder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for Section creation requests in Builder pattern
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionRequest {
    private String title;
    private Integer position;
    
    @Builder.Default
    private List<ArticleRequest> articles = new ArrayList<>();
    
    /**
     * Validate section request data
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Section title cannot be empty");
        }
        if (position == null || position < 0) {
            throw new IllegalArgumentException("Section position must be a non-negative number");
        }
        
        // Validate all articles
        if (articles != null) {
            for (ArticleRequest article : articles) {
                article.validate();
            }
        }
    }
    
    /**
     * Add an article to this section
     * @param article Article to add
     * @return this SectionRequest for fluent chaining
     */
    public SectionRequest addArticle(ArticleRequest article) {
        if (articles == null) {
            articles = new ArrayList<>();
        }
        articles.add(article);
        return this;
    }
}
