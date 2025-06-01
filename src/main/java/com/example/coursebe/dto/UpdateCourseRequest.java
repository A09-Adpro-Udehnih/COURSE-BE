package com.example.coursebe.dto;

import java.math.BigDecimal;
import java.util.List;

public class UpdateCourseRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private List<SectionDto> sections;

    public UpdateCourseRequest() {}

    public UpdateCourseRequest(String name, String description, BigDecimal price, List<SectionDto> sections) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.sections = sections;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public List<SectionDto> getSections() {
        return sections;
    }

    public void setSections(List<SectionDto> sections) {
        this.sections = sections;
    }
}
