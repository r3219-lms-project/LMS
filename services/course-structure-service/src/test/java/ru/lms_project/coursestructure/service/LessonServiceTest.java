package ru.lms_project.coursestructure.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.lms_project.coursestructure.dto.LessonCreateRequest;
import ru.lms_project.coursestructure.dto.LessonDto;
import ru.lms_project.coursestructure.dto.LessonUpdateRequest;
import ru.lms_project.coursestructure.model.Lesson;
import ru.lms_project.coursestructure.model.LessonType;
import ru.lms_project.coursestructure.repository.LessonRepository;
import ru.lms_project.coursestructure.repository.ModuleRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private ModuleRepository moduleRepository;

    @InjectMocks
    private LessonService lessonService;

    private Lesson testLesson;

    @BeforeEach
    void setUp() {
        testLesson = new Lesson();
        testLesson.setId("lesson-1");
        testLesson.setModuleId("module-1");
        testLesson.setTitle("Test Lesson");
        testLesson.setContent("Test Content");
        testLesson.setType(LessonType.VIDEO);
        testLesson.setDuration(30);
        testLesson.setOrderIndex(1);
        testLesson.setVideoUrl("https://example.com/video.mp4");
    }

    @Test
    void createLesson_WhenModuleExists_ShouldReturnCreatedLesson() {
        // Arrange
        LessonCreateRequest request = new LessonCreateRequest();
        request.setTitle("New Lesson");
        request.setContent("New Content");
        request.setType(LessonType.TEXT);
        request.setDuration(15);
        request.setOrderIndex(1);

        when(moduleRepository.existsById("module-1")).thenReturn(true);
        when(lessonRepository.save(any(Lesson.class))).thenReturn(testLesson);

        // Act
        LessonDto result = lessonService.createLesson("module-1", request);

        // Assert
        assertNotNull(result);
        assertEquals(testLesson.getId(), result.getId());
        assertEquals(testLesson.getTitle(), result.getTitle());
        verify(lessonRepository, times(1)).save(any(Lesson.class));
    }

    @Test
    void createLesson_WhenModuleNotExists_ShouldThrowException() {
        // Arrange
        LessonCreateRequest request = new LessonCreateRequest();
        request.setTitle("New Lesson");
        request.setType(LessonType.TEXT);
        request.setOrderIndex(1);

        when(moduleRepository.existsById("nonexistent")).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class,
            () -> lessonService.createLesson("nonexistent", request));
    }

    @Test
    void getLessonsByModuleId_ShouldReturnSortedList() {
        // Arrange
        Lesson lesson2 = new Lesson();
        lesson2.setId("lesson-2");
        lesson2.setModuleId("module-1");
        lesson2.setTitle("Lesson 2");
        lesson2.setType(LessonType.QUIZ);
        lesson2.setOrderIndex(2);

        when(lessonRepository.findByModuleIdOrderByOrderIndexAsc("module-1"))
                .thenReturn(Arrays.asList(testLesson, lesson2));

        // Act
        List<LessonDto> result = lessonService.getLessonsByModuleId("module-1");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("lesson-1", result.get(0).getId());
        assertEquals("lesson-2", result.get(1).getId());
    }

    @Test
    void getLessonById_WhenExists_ShouldReturnLesson() {
        // Arrange
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(testLesson));

        // Act
        LessonDto result = lessonService.getLessonById("lesson-1");

        // Assert
        assertNotNull(result);
        assertEquals("lesson-1", result.getId());
        assertEquals(LessonType.VIDEO, result.getType());
    }

    @Test
    void updateLesson_ShouldUpdateFields() {
        // Arrange
        LessonUpdateRequest request = new LessonUpdateRequest();
        request.setTitle("Updated Title");
        request.setDuration(45);

        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(testLesson));
        when(lessonRepository.save(any(Lesson.class))).thenReturn(testLesson);

        // Act
        LessonDto result = lessonService.updateLesson("lesson-1", request);

        // Assert
        assertNotNull(result);
        verify(lessonRepository, times(1)).save(any(Lesson.class));
    }

    @Test
    void deleteLesson_WhenExists_ShouldDelete() {
        // Arrange
        when(lessonRepository.existsById("lesson-1")).thenReturn(true);
        doNothing().when(lessonRepository).deleteById("lesson-1");

        // Act
        lessonService.deleteLesson("lesson-1");

        // Assert
        verify(lessonRepository, times(1)).deleteById("lesson-1");
    }
}
