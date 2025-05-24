package com.example.coursebe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private Date enrollmentDate;
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
