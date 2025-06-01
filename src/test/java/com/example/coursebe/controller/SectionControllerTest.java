package com.example.coursebe.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.coursebe.controller.SectionController;
import com.example.coursebe.model.Course;
import com.example.coursebe.model.Section;
import com.example.coursebe.model.TutorApplication;
import com.example.coursebe.service.CourseService;
import com.example.coursebe.service.SectionService;
import com.example.coursebe.service.TutorApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class SectionControllerTest {

    @Mock
    private SectionService sectionService;

    @Mock
    private CourseService courseService;

    @Mock
    private TutorApplicationService tutorApplicationService;

    @Mock
    private Principal principal;

    @InjectMocks
    private SectionController sectionController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UUID courseId = UUID.randomUUID();
    private final UUID sectionId = UUID.randomUUID();
    private final UUID tutorId = UUID.randomUUID();
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sectionController).build();
        when(principal.getName()).thenReturn(tutorId.toString());
    }

    @Test
    @DisplayName("POST /courses/{courseId}/sections - Success")
    void createSectionSuccess() throws Exception {
        // Arrange
        TutorApplication tutorApplication = new TutorApplication();
        tutorApplication.setStatus(TutorApplication.Status.ACCEPTED);
        
        // Using reflection to set the courseId since there's no setId method
        Course course = new Course("Test Course", "Description", tutorId, BigDecimal.valueOf(100));
        // Use reflection to set the ID to our test courseId
        setPrivateField(course, "id", courseId);
        
        Section section = new Section("Test Section", 0);
        // Use reflection to set the ID to our test sectionId
        setPrivateField(section, "id", sectionId);
        section.setCourse(course);
        
        SectionController.SectionRequest request = new SectionController.SectionRequest();
        request.title = "Test Section";
        request.position = 0;
        
        when(tutorApplicationService.getMostRecentApplicationByStudentId(tutorId)).thenReturn(Optional.of(tutorApplication));
        when(courseService.getCourseById(courseId)).thenReturn(Optional.of(course));
        when(sectionService.createSection(eq(courseId), eq(request.title), eq(request.position))).thenReturn(section);
        
        // Act & Assert
        mockMvc.perform(post("/courses/{courseId}/sections", courseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(principal))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Section created successfully."))
                .andExpect(jsonPath("$.sectionId").value(sectionId.toString()));
        
        verify(sectionService).createSection(courseId, request.title, request.position);
    }
    
    @Test
    @DisplayName("POST /courses/{courseId}/sections - Forbidden (Not Accepted Tutor)")
    void createSectionForbiddenNotAcceptedTutor() throws Exception {
        // Arrange
        TutorApplication tutorApplication = new TutorApplication();
        tutorApplication.setStatus(TutorApplication.Status.PENDING);
        
        SectionController.SectionRequest request = new SectionController.SectionRequest();
        request.title = "Test Section";
        request.position = 0;
        
        when(tutorApplicationService.getMostRecentApplicationByStudentId(tutorId)).thenReturn(Optional.of(tutorApplication));
        
        // Act & Assert
        mockMvc.perform(post("/courses/{courseId}/sections", courseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(principal))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You are not allowed to modify this course. Tutor application must be ACCEPTED."));
        
        verify(sectionService, never()).createSection(any(), any(), any());
    }
    
    @Test
    @DisplayName("POST /courses/{courseId}/sections - Forbidden (Not Course Owner)")
    void createSectionForbiddenNotCourseOwner() throws Exception {
        // Arrange
        UUID anotherTutorId = UUID.randomUUID();
        TutorApplication tutorApplication = new TutorApplication();
        tutorApplication.setStatus(TutorApplication.Status.ACCEPTED);
        
        Course course = new Course("Test Course", "Description", anotherTutorId, BigDecimal.valueOf(100));
        setPrivateField(course, "id", courseId);
        
        SectionController.SectionRequest request = new SectionController.SectionRequest();
        request.title = "Test Section";
        request.position = 0;
        
        when(tutorApplicationService.getMostRecentApplicationByStudentId(tutorId)).thenReturn(Optional.of(tutorApplication));
        when(courseService.getCourseById(courseId)).thenReturn(Optional.of(course));
        
        // Act & Assert
        mockMvc.perform(post("/courses/{courseId}/sections", courseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(principal))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You are not the owner of this course."));
        
        verify(sectionService, never()).createSection(any(), any(), any());
    }
    
    @Test
    @DisplayName("POST /courses/{courseId}/sections - Course Not Found")
    void createSectionCourseNotFound() throws Exception {
        // Arrange
        TutorApplication tutorApplication = new TutorApplication();
        tutorApplication.setStatus(TutorApplication.Status.ACCEPTED);
        
        SectionController.SectionRequest request = new SectionController.SectionRequest();
        request.title = "Test Section";
        request.position = 0;
        
        when(tutorApplicationService.getMostRecentApplicationByStudentId(tutorId)).thenReturn(Optional.of(tutorApplication));
        when(courseService.getCourseById(courseId)).thenReturn(Optional.empty());
        
        // Act & Assert
        mockMvc.perform(post("/courses/{courseId}/sections", courseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(principal))
                .andExpect(status().isForbidden())  // Changed from isNotFound to isForbidden
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You are not the owner of this course."));  // Updated expected message
        
        verify(sectionService, never()).createSection(any(), any(), any());
    }
    
    @Test
    @DisplayName("GET /courses/{courseId}/sections - Success")
    void getSectionsSuccess() throws Exception {
        // Arrange
        Course course = new Course("Test Course", "Description", tutorId, BigDecimal.valueOf(100));
        setPrivateField(course, "id", courseId);
        
        Section section1 = new Section("Test Section 1", 0);
        UUID section1Id = UUID.randomUUID();
        setPrivateField(section1, "id", section1Id);
        section1.setCourse(course);
        
        Section section2 = new Section("Test Section 2", 1);
        UUID section2Id = UUID.randomUUID();
        setPrivateField(section2, "id", section2Id);
        section2.setCourse(course);
        
        List<Section> sections = Arrays.asList(section1, section2);
        
        when(courseService.getCourseById(courseId)).thenReturn(Optional.of(course));
        when(sectionService.getSectionsByCourseId(courseId)).thenReturn(sections);
        
        // Act & Assert
        mockMvc.perform(get("/courses/{courseId}/sections", courseId)
                .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.sections.length()").value(2))
                .andExpect(jsonPath("$.sections[0].title").value("Test Section 1"))
                .andExpect(jsonPath("$.sections[1].title").value("Test Section 2"));
    }
    
    @Test
    @DisplayName("GET /courses/{courseId}/sections - Course Not Found")
    void getSectionsCourseNotFound() throws Exception {
        // Arrange
        when(courseService.getCourseById(courseId)).thenReturn(Optional.empty());
        
        // Act & Assert
        mockMvc.perform(get("/courses/{courseId}/sections", courseId)
                .principal(principal))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Course not found."));
        
        verify(sectionService, never()).getSectionsByCourseId(any());
    }
    
    @Test
    @DisplayName("GET /courses/{courseId}/sections/{sectionId} - Success")
    void getSectionByIdSuccess() throws Exception {
        // Arrange
        Course course = new Course("Test Course", "Description", tutorId, BigDecimal.valueOf(100));
        setPrivateField(course, "id", courseId);
        
        Section section = new Section("Test Section", 0);
        setPrivateField(section, "id", sectionId);
        section.setCourse(course);
        
        when(sectionService.getSectionById(sectionId)).thenReturn(Optional.of(section));
        
        // Act & Assert
        mockMvc.perform(get("/courses/{courseId}/sections/{sectionId}", courseId, sectionId)
                .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.section.id").value(sectionId.toString()))
                .andExpect(jsonPath("$.section.title").value("Test Section"));
    }
    
    @Test
    @DisplayName("GET /courses/{courseId}/sections/{sectionId} - Section Not In Course")
    void getSectionByIdSectionNotInCourse() throws Exception {
        // Arrange
        Course anotherCourse = new Course("Another Course", "Description", tutorId, BigDecimal.valueOf(100));
        UUID anotherCourseId = UUID.randomUUID();
        setPrivateField(anotherCourse, "id", anotherCourseId);
        
        Section section = new Section("Test Section", 0);
        setPrivateField(section, "id", sectionId);
        section.setCourse(anotherCourse);
        
        when(sectionService.getSectionById(sectionId)).thenReturn(Optional.of(section));
        
        // Act & Assert
        mockMvc.perform(get("/courses/{courseId}/sections/{sectionId}", courseId, sectionId)
                .principal(principal))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Section not found in this course."));
    }
    
    @Test
    @DisplayName("PUT /courses/{courseId}/sections/{sectionId} - Success")
    void updateSectionSuccess() throws Exception {
        // Arrange
        TutorApplication tutorApplication = new TutorApplication();
        tutorApplication.setStatus(TutorApplication.Status.ACCEPTED);
        
        Course course = new Course("Test Course", "Description", tutorId, BigDecimal.valueOf(100));
        setPrivateField(course, "id", courseId);
        
        Section section = new Section("Original Title", 0);
        setPrivateField(section, "id", sectionId);
        section.setCourse(course);
        
        Section updatedSection = new Section("Updated Title", 1);
        setPrivateField(updatedSection, "id", sectionId);
        updatedSection.setCourse(course);
        
        SectionController.SectionRequest request = new SectionController.SectionRequest();
        request.title = "Updated Title";
        request.position = 1;
        
        when(tutorApplicationService.getMostRecentApplicationByStudentId(tutorId)).thenReturn(Optional.of(tutorApplication));
        when(courseService.getCourseById(courseId)).thenReturn(Optional.of(course));
        when(sectionService.getSectionById(sectionId)).thenReturn(Optional.of(section));
        when(sectionService.updateSection(eq(sectionId), eq(request.title), eq(request.position)))
                .thenReturn(Optional.of(updatedSection));
        
        // Act & Assert
        mockMvc.perform(put("/courses/{courseId}/sections/{sectionId}", courseId, sectionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Section updated successfully."))
                .andExpect(jsonPath("$.section.title").value("Updated Title"));
        
        verify(sectionService).updateSection(sectionId, request.title, request.position);
    }
    
    @Test
    @DisplayName("PUT /courses/{courseId}/sections/{sectionId} - Forbidden (Not Accepted Tutor)")
    void updateSectionForbiddenNotAcceptedTutor() throws Exception {
        // Arrange
        TutorApplication tutorApplication = new TutorApplication();
        tutorApplication.setStatus(TutorApplication.Status.PENDING);
        
        SectionController.SectionRequest request = new SectionController.SectionRequest();
        request.title = "Updated Title";
        request.position = 1;
        
        when(tutorApplicationService.getMostRecentApplicationByStudentId(tutorId)).thenReturn(Optional.of(tutorApplication));
        
        // Act & Assert
        mockMvc.perform(put("/courses/{courseId}/sections/{sectionId}", courseId, sectionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(principal))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You are not allowed to modify this course. Tutor application must be ACCEPTED."));
        
        verify(sectionService, never()).updateSection(any(), any(), any());
    }
    
    @Test
    @DisplayName("DELETE /courses/{courseId}/sections/{sectionId} - Success")
    void deleteSectionSuccess() throws Exception {
        // Arrange
        TutorApplication tutorApplication = new TutorApplication();
        tutorApplication.setStatus(TutorApplication.Status.ACCEPTED);
        
        Course course = new Course("Test Course", "Description", tutorId, BigDecimal.valueOf(100));
        setPrivateField(course, "id", courseId);
        
        Section section = new Section("Test Section", 0);
        setPrivateField(section, "id", sectionId);
        section.setCourse(course);
        
        when(tutorApplicationService.getMostRecentApplicationByStudentId(tutorId)).thenReturn(Optional.of(tutorApplication));
        when(courseService.getCourseById(courseId)).thenReturn(Optional.of(course));
        when(sectionService.getSectionById(sectionId)).thenReturn(Optional.of(section));
        when(sectionService.deleteSection(sectionId)).thenReturn(true);
        
        // Act & Assert
        mockMvc.perform(delete("/courses/{courseId}/sections/{sectionId}", courseId, sectionId)
                .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Section deleted successfully."));
        
        verify(sectionService).deleteSection(sectionId);
    }
    
    @Test
    @DisplayName("DELETE /courses/{courseId}/sections/{sectionId} - Not Found")
    void deleteSectionNotFound() throws Exception {
        // Arrange
        TutorApplication tutorApplication = new TutorApplication();
        tutorApplication.setStatus(TutorApplication.Status.ACCEPTED);
        
        Course course = new Course("Test Course", "Description", tutorId, BigDecimal.valueOf(100));
        setPrivateField(course, "id", courseId);
        
        when(tutorApplicationService.getMostRecentApplicationByStudentId(tutorId)).thenReturn(Optional.of(tutorApplication));
        when(courseService.getCourseById(courseId)).thenReturn(Optional.of(course));
        when(sectionService.getSectionById(sectionId)).thenReturn(Optional.empty());
        
        // Act & Assert
        mockMvc.perform(delete("/courses/{courseId}/sections/{sectionId}", courseId, sectionId)
                .principal(principal))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Section not found in this course."));
        
        verify(sectionService, never()).deleteSection(any());
    }
    
    // Helper method to set private fields using reflection
    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field " + fieldName, e);
        }
    }
}