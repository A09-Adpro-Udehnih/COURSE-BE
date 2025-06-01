package com.example.coursebe.dto;

import java.util.UUID;

public class ArticleDto {
    private UUID id;
    private String title;
    private String content;
    private Integer position;

    public ArticleDto() {}

    public ArticleDto(UUID id, String title, String content, Integer position) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.position = position;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }
}
