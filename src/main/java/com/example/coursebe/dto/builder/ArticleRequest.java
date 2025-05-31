package com.example.coursebe.dto.builder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Article creation requests in Builder pattern
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleRequest {
    private String title;
    private String content;
    private Integer position;
    
    /**
     * Validate article request data
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Article title cannot be empty");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Article content cannot be empty");
        }
        if (position == null || position < 0) {
            throw new IllegalArgumentException("Article position must be a non-negative number");
        }
    }
}
