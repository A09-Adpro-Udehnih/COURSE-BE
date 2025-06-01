package com.example.coursebe.dto;

import java.util.List;
import java.util.UUID;

public class SectionDto {
    private UUID id;
    private String title;
    private Integer position;
    private List<ArticleDto> articles;

    public SectionDto() {}

    public SectionDto(UUID id, String title, Integer position, List<ArticleDto> articles) {
        this.id = id;
        this.title = title;
        this.position = position;
        this.articles = articles;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public List<ArticleDto> getArticles() {
        return articles;
    }

    public void setArticles(List<ArticleDto> articles) {
        this.articles = articles;
    }
}
