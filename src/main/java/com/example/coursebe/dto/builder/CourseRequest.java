package com.example.coursebe.dto.builder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseRequest {
    
    @NotBlank(message = "Course name is required")
    @Size(min = 3, max = 100, message = "Course name must be between 3 and 100 characters")
    private String name;
    
    @NotBlank(message = "Course description is required")
    @Size(min = 10, max = 1000, message = "Course description must be between 10 and 1000 characters")
    private String description;
    
    @NotNull(message = "Tutor ID is required")
    private UUID tutorId;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be non-negative")
    private BigDecimal price;
    
    @Valid
    private List<SectionRequest> sections = new ArrayList<>();
    
    public CourseRequest addSection(SectionRequest section) {
        this.sections.add(section);
        return this;
    }
    
    public CourseRequest addSections(List<SectionRequest> sections) {
        this.sections.addAll(sections);
        return this;
    }
    
    public void validate() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Course name is required");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Course description is required");
        }
        if (tutorId == null) {
            throw new IllegalArgumentException("Tutor ID is required");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be non-negative");
        }
        
        // Validate all sections
        for (SectionRequest section : sections) {
            section.validate();
        }
    }
}
