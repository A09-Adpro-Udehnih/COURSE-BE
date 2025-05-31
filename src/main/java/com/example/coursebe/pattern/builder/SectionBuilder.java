package com.example.coursebe.pattern.builder;

import com.example.coursebe.dto.builder.ArticleRequest;
import com.example.coursebe.dto.builder.SectionRequest;
import com.example.coursebe.model.Article;
import com.example.coursebe.model.Section;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder class for creating Section objects with fluent API
 */
@Component
public class SectionBuilder {
    private String title;
    private Integer position;
    private List<ArticleRequest> articles;
    
    public SectionBuilder() {
        this.articles = new ArrayList<>();
    }
    
    /**
     * Set the section title
     * @param title Section title
     * @return this builder for fluent chaining
     */
    public SectionBuilder title(String title) {
        this.title = title;
        return this;
    }
    
    /**
     * Set the section position
     * @param position Section position
     * @return this builder for fluent chaining
     */
    public SectionBuilder position(Integer position) {
        this.position = position;
        return this;
    }
    
    /**
     * Add an article to this section
     * @param title Article title
     * @param content Article content
     * @param position Article position
     * @return this builder for fluent chaining
     */
    public SectionBuilder addArticle(String title, String content, Integer position) {
        ArticleRequest article = ArticleRequest.builder()
                .title(title)
                .content(content)
                .position(position)
                .build();
        this.articles.add(article);
        return this;
    }
    
    /**
     * Add an article using ArticleRequest
     * @param article Article request
     * @return this builder for fluent chaining
     */
    public SectionBuilder addArticle(ArticleRequest article) {
        this.articles.add(article);
        return this;
    }
    
    /**
     * Add multiple articles at once
     * @param articles List of article requests
     * @return this builder for fluent chaining
     */
    public SectionBuilder addArticles(List<ArticleRequest> articles) {
        this.articles.addAll(articles);
        return this;
    }
    
    /**
     * Build and validate the SectionRequest
     * @return SectionRequest object
     * @throws IllegalArgumentException if validation fails
     */
    public SectionRequest build() {
        SectionRequest sectionRequest = SectionRequest.builder()
                .title(title)
                .position(position)
                .articles(new ArrayList<>(articles))
                .build();
        
        // Validate the built section
        sectionRequest.validate();
        
        return sectionRequest;
    }
    
    /**
     * Build a Section entity directly
     * @return Section entity
     * @throws IllegalArgumentException if validation fails
     */
    public Section buildEntity() {
        SectionRequest sectionRequest = build();
        
        Section section = new Section(sectionRequest.getTitle(), sectionRequest.getPosition());
        
        // Add articles to the section
        for (ArticleRequest articleRequest : sectionRequest.getArticles()) {
            Article article = new Article(
                articleRequest.getTitle(),
                articleRequest.getContent(),
                articleRequest.getPosition()
            );
            article.setSection(section);
            section.getArticles().add(article);
        }
        
        return section;
    }
    
    /**
     * Reset the builder to initial state
     * @return this builder for reuse
     */
    public SectionBuilder reset() {
        this.title = null;
        this.position = null;
        this.articles = new ArrayList<>();
        return this;
    }
}
