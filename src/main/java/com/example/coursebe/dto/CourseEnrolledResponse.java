package com.example.coursebe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseEnrolledResponse {
    private UUID id;
    private String name;
    private String description;
    private String tutor;
    private BigDecimal price;
    private LocalDateTime enrollmentDate;
    private List<Section> sections;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Section {
        private UUID id;
        private String title;
        private List<Article> articles;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Article {
        private UUID id;
        private String title;
    }
}
