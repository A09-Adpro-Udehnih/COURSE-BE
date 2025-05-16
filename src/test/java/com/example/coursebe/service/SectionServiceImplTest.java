package com.example.coursebe.service;

import com.example.coursebe.model.Course;
import com.example.coursebe.model.Section;
import com.example.coursebe.repository.CourseRepository;
import com.example.coursebe.repository.SectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SectionServiceImplTest {

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private SectionServiceImpl sectionService;

    private UUID courseId;
    private UUID sectionId;
    private Course testCourse;
    private Section testSection;
    private List<Section> testSections;

    @BeforeEach
    void setUp() {
        courseId = UUID.randomUUID();
        sectionId = UUID.randomUUID();
        
        testCourse = new Course("Test Course", "Test Description", UUID.randomUUID(), new BigDecimal("99.99"));
        // Set course ID using reflection
        try {
            java.lang.reflect.Field field = Course.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(testCourse, courseId);
        } catch (Exception e) {
            fail("Failed to set course ID");
        }
        
        testSection = new Section("Test Section", 1);
        testSection.setCourse(testCourse);
        // Set section ID using reflection
        try {
            java.lang.reflect.Field field = Section.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(testSection, sectionId);
        } catch (Exception e) {
            fail("Failed to set section ID");
        }
        
        Section section2 = new Section("Second Section", 2);
        section2.setCourse(testCourse);
        // Set section2 ID using reflection
        try {
            java.lang.reflect.Field field = Section.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(section2, UUID.randomUUID());
        } catch (Exception e) {
            fail("Failed to set section ID");
        }
        
        testSections = Arrays.asList(testSection, section2);
    }

    @Test
    @DisplayName("Should get sections by course ID")
    void getSectionsByCourseId() {
        // Given
        when(sectionRepository.findByCourseId(courseId)).thenReturn(testSections);
        
        // When
        List<Section> result = sectionService.getSectionsByCourseId(courseId);
        
        // Then
        assertEquals(2, result.size());
        assertEquals(testSections.get(0).getTitle(), result.get(0).getTitle());
        assertEquals(testSections.get(0).getPosition(), result.get(0).getPosition());
        verify(sectionRepository).findByCourseId(courseId);
    }

    @Test
    @DisplayName("Should get section by ID")
    void getSectionById() {
        // Given
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(testSection));
        
        // When
        Optional<Section> result = sectionService.getSectionById(sectionId);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(testSection, result.get());
        verify(sectionRepository).findById(sectionId);
    }

    @Test
    @DisplayName("Should return empty optional when section not found")
    void getSectionByIdNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(sectionRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        
        // When
        Optional<Section> result = sectionService.getSectionById(nonExistentId);
        
        // Then
        assertFalse(result.isPresent());
        verify(sectionRepository).findById(nonExistentId);
    }

    @Test
    @DisplayName("Should create section with provided position")
    void createSectionWithPosition() {
        // Given
        String title = "New Section";
        Integer position = 3;
        
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));
        when(sectionRepository.save(any(Section.class))).thenAnswer(i -> {
            Section section = (Section) i.getArguments()[0];
            // Set section ID using reflection
            try {
                java.lang.reflect.Field field = Section.class.getDeclaredField("id");
                field.setAccessible(true);
                field.set(section, UUID.randomUUID());
            } catch (Exception e) {
                fail("Failed to set section ID");
            }
            return section;
        });
        
        // When
        Section result = sectionService.createSection(courseId, title, position);
        
        // Then
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertEquals(position, result.getPosition());
        assertEquals(courseId, result.getCourse().getId());
        verify(courseRepository).findById(courseId);
        verify(sectionRepository).save(any(Section.class));
    }

    @Test
    @DisplayName("Should create section with calculated position when position is null")
    void createSectionWithCalculatedPosition() {
        // Given
        String title = "New Section";
        Integer position = null;
        List<Section> existingSections = new ArrayList<>(testSections);
        
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));
        when(sectionRepository.findByCourse(testCourse)).thenReturn(existingSections);
        when(sectionRepository.save(any(Section.class))).thenAnswer(i -> {
            Section section = (Section) i.getArguments()[0];
            // Set section ID using reflection
            try {
                java.lang.reflect.Field field = Section.class.getDeclaredField("id");
                field.setAccessible(true);
                field.set(section, UUID.randomUUID());
            } catch (Exception e) {
                fail("Failed to set section ID");
            }
            return section;
        });
        
        // When
        Section result = sectionService.createSection(courseId, title, position);
        
        // Then
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertEquals(2, result.getPosition()); // Max position (2) + 1 = 3
        assertEquals(courseId, result.getCourse().getId());
        verify(courseRepository).findById(courseId);
        verify(sectionRepository).findByCourse(testCourse);
        verify(sectionRepository).save(any(Section.class));
    }

    @Test
    @DisplayName("Should return null when creating section for non-existent course")
    void createSectionForNonExistentCourse() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        String title = "New Section";
        Integer position = 1;
        
        when(courseRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        
        // When
        Section result = sectionService.createSection(nonExistentId, title, position);
        
        // Then
        assertNull(result);
        verify(courseRepository).findById(nonExistentId);
        verify(sectionRepository, never()).save(any(Section.class));
    }

    @Test
    @DisplayName("Should update section")
    void updateSection() {
        // Given
        String updatedTitle = "Updated Section";
        Integer updatedPosition = 3;
        
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(testSection));
        when(sectionRepository.save(any(Section.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // When
        Optional<Section> result = sectionService.updateSection(sectionId, updatedTitle, updatedPosition);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(updatedTitle, result.get().getTitle());
        assertEquals(updatedPosition, result.get().getPosition());
        verify(sectionRepository).findById(sectionId);
        verify(sectionRepository).save(any(Section.class));
    }

    @Test
    @DisplayName("Should return empty optional when updating non-existent section")
    void updateNonExistentSection() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(sectionRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        
        // When
        Optional<Section> result = sectionService.updateSection(nonExistentId, "Title", 1);
        
        // Then
        assertFalse(result.isPresent());
        verify(sectionRepository).findById(nonExistentId);
        verify(sectionRepository, never()).save(any(Section.class));
    }

    @Test
    @DisplayName("Should delete section")
    void deleteSection() {
        // Given
        when(sectionRepository.existsById(sectionId)).thenReturn(true);
        
        // When
        boolean result = sectionService.deleteSection(sectionId);
        
        // Then
        assertTrue(result);
        verify(sectionRepository).existsById(sectionId);
        verify(sectionRepository).deleteById(sectionId);
    }

    @Test
    @DisplayName("Should return false when deleting non-existent section")
    void deleteNonExistentSection() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(sectionRepository.existsById(nonExistentId)).thenReturn(false);
        
        // When
        boolean result = sectionService.deleteSection(nonExistentId);
        
        // Then
        assertFalse(result);
        verify(sectionRepository).existsById(nonExistentId);
        verify(sectionRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    @DisplayName("Should reorder sections")
    void reorderSections() {
        // Given
        List<UUID> orderedIds = Arrays.asList(
            testSections.get(1).getId(), 
            testSections.get(0).getId()
        );
        
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));
        when(sectionRepository.findByCourse(testCourse)).thenReturn(testSections);
        when(sectionRepository.save(any(Section.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // When
        List<Section> result = sectionService.reorderSections(courseId, orderedIds);
        
        // Then
        assertEquals(2, result.size());
        assertEquals(testSections.get(1).getId(), result.get(0).getId());
        assertEquals(0, result.get(0).getPosition());
        assertEquals(testSections.get(0).getId(), result.get(1).getId());
        assertEquals(1, result.get(1).getPosition());
        verify(courseRepository).findById(courseId);
        verify(sectionRepository).findByCourse(testCourse);
        verify(sectionRepository, times(2)).save(any(Section.class));
    }

    @Test
    @DisplayName("Should return empty list when reordering sections for non-existent course")
    void reorderSectionsForNonExistentCourse() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        List<UUID> orderedIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        
        when(courseRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        
        // When
        List<Section> result = sectionService.reorderSections(nonExistentId, orderedIds);
        
        // Then
        assertTrue(result.isEmpty());
        verify(courseRepository).findById(nonExistentId);
        verify(sectionRepository, never()).save(any(Section.class));
    }
}