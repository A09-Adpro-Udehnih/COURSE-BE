package com.example.coursebe.pattern.builder;

import com.example.coursebe.dto.builder.CourseRequest;
import com.example.coursebe.dto.builder.SectionRequest;
import com.example.coursebe.enums.Status;
import com.example.coursebe.model.Course;
import com.example.coursebe.model.Section;
import com.example.coursebe.model.TutorApplication;
import com.example.coursebe.service.TutorApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class CourseBuilder {
    
    @Autowired
    private TutorApplicationService tutorApplicationService;
    
    @Autowired
    private SectionBuilder sectionBuilder;
    
    private String name;
    private String description;
    private UUID tutorId;
    private BigDecimal price;
    private List<SectionRequest> sections = new ArrayList<>();
    
    public CourseBuilder name(String name) {
        this.name = name;
        return this;
    }
    
    public CourseBuilder description(String description) {
        this.description = description;
        return this;
    }
    
    public CourseBuilder tutorId(UUID tutorId) {
        this.tutorId = tutorId;
        return this;
    }
    
    public CourseBuilder price(BigDecimal price) {
        this.price = price;
        return this;
    }
    
    public CourseBuilder addSection(SectionRequest section) {
        this.sections.add(section);
        return this;
    }
    
    public CourseBuilder addSections(List<SectionRequest> sections) {
        this.sections.addAll(sections);
        return this;
    }
    
    /**
     * Convenience method to add a section using a configured SectionBuilder
     */
    public CourseBuilder addSection(String title, int position) {
        SectionRequest section = sectionBuilder.title(title).position(position).build();
        this.sections.add(section);
        return this;
    }
    
    /**
     * Build and validate the CourseRequest DTO
     */
    public CourseRequest build() {
        CourseRequest request = new CourseRequest(name, description, tutorId, price, new ArrayList<>(sections));
        request.validate();
        return request;
    }
    
    /**
     * Build and validate the Course entity with tutor status validation
     * This method performs the critical business rule validation
     */
    public Course buildEntity() {
        // First validate the request
        CourseRequest request = build();
        
        // Critical validation: Check tutor status
        validateTutorStatus(tutorId);
        
        // Create the course entity
        Course course = new Course();
        course.setName(name);
        course.setDescription(description);
        course.setTutorId(tutorId);        course.setPrice(price);
        course.setStatus(Status.PENDING); // New courses start as PENDING
        
        // Build sections using SectionBuilder
        List<Section> courseSections = new ArrayList<>();
        for (SectionRequest sectionRequest : sections) {
            Section section = sectionBuilder
                .title(sectionRequest.getTitle())
                .position(sectionRequest.getPosition())
                .addArticles(sectionRequest.getArticles())
                .buildEntity();
            
            section.setCourse(course);
            courseSections.add(section);
        }
        
        course.setSections(courseSections);
        return course;
    }
    
    /**
     * Validates that the tutor has ACCEPTED status and can create courses
     * This enforces the business rule that only accepted tutors can create courses
     */
    private void validateTutorStatus(UUID tutorId) {
        if (tutorId == null) {
            throw new IllegalArgumentException("Tutor ID is required");
        }
        
        TutorApplication tutorApplication = tutorApplicationService.findByTutorId(tutorId);
        if (tutorApplication == null) {
            throw new IllegalArgumentException("Tutor application not found for tutor ID: " + tutorId);
        }
          if (tutorApplication.getStatus() != TutorApplication.Status.ACCEPTED) {
            throw new IllegalArgumentException(
                String.format("Cannot create course. Tutor status is %s, but must be ACCEPTED to create courses.", 
                             tutorApplication.getStatus())
            );
        }
    }
    
    /**
     * Reset the builder to initial state for reuse
     */
    public CourseBuilder reset() {
        this.name = null;
        this.description = null;
        this.tutorId = null;
        this.price = null;
        this.sections.clear();
        return this;
    }
    
    /**
     * Create a new CourseBuilder instance from an existing CourseRequest
     */
    public static CourseBuilder from(CourseRequest request) {
        CourseBuilder builder = new CourseBuilder();
        builder.name = request.getName();
        builder.description = request.getDescription();
        builder.tutorId = request.getTutorId();
        builder.price = request.getPrice();
        builder.sections = new ArrayList<>(request.getSections());
        return builder;
    }
}
