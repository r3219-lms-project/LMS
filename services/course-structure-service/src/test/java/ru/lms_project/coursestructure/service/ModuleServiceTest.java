package ru.lms_project.coursestructure.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.lms_project.coursestructure.dto.ModuleCreateRequest;
import ru.lms_project.coursestructure.dto.ModuleDto;
import ru.lms_project.coursestructure.dto.ModuleUpdateRequest;
import ru.lms_project.coursestructure.model.Lesson;
import ru.lms_project.coursestructure.model.Module;
import ru.lms_project.coursestructure.repository.LessonRepository;
import ru.lms_project.coursestructure.repository.ModuleRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModuleServiceTest {

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private LessonRepository lessonRepository;

    @InjectMocks
    private ModuleService moduleService;

    private Module testModule;

    @BeforeEach
    void setUp() {
        testModule = new Module();
        testModule.setId("module-1");
        testModule.setCourseId("course-1");
        testModule.setTitle("Test Module");
        testModule.setDescription("Test Description");
        testModule.setOrderIndex(1);
    }

    @Test
    void createModule_ShouldReturnCreatedModule() {
        // Arrange
        ModuleCreateRequest request = new ModuleCreateRequest();
        request.setTitle("New Module");
        request.setDescription("New Description");
        request.setOrderIndex(1);

        when(moduleRepository.save(any(Module.class))).thenReturn(testModule);
        when(lessonRepository.findByModuleIdOrderByOrderIndexAsc(anyString())).thenReturn(Collections.emptyList());

        // Act
        ModuleDto result = moduleService.createModule("course-1", request);

        // Assert
        assertNotNull(result);
        assertEquals(testModule.getId(), result.getId());
        assertEquals(testModule.getTitle(), result.getTitle());
        verify(moduleRepository, times(1)).save(any(Module.class));
    }

    @Test
    void getModulesByCourseId_ShouldReturnSortedList() {
        // Arrange
        Module module2 = new Module();
        module2.setId("module-2");
        module2.setCourseId("course-1");
        module2.setTitle("Module 2");
        module2.setOrderIndex(2);

        when(moduleRepository.findByCourseIdOrderByOrderIndexAsc("course-1"))
                .thenReturn(Arrays.asList(testModule, module2));
        when(lessonRepository.findByModuleIdOrderByOrderIndexAsc(anyString())).thenReturn(Collections.emptyList());

        // Act
        List<ModuleDto> result = moduleService.getModulesByCourseId("course-1");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("module-1", result.get(0).getId());
        assertEquals("module-2", result.get(1).getId());
    }

    @Test
    void getModuleById_WhenExists_ShouldReturnModule() {
        // Arrange
        when(moduleRepository.findById("module-1")).thenReturn(Optional.of(testModule));
        when(lessonRepository.findByModuleIdOrderByOrderIndexAsc("module-1"))
                .thenReturn(Arrays.asList(new Lesson(), new Lesson(), new Lesson()));

        // Act
        ModuleDto result = moduleService.getModuleById("module-1");

        // Assert
        assertNotNull(result);
        assertEquals("module-1", result.getId());
        assertEquals(3L, result.getLessonsCount());
    }

    @Test
    void getModuleById_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(moduleRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> moduleService.getModuleById("nonexistent"));
    }

    @Test
    void updateModule_ShouldUpdateFields() {
        // Arrange
        ModuleUpdateRequest request = new ModuleUpdateRequest();
        request.setTitle("Updated Title");
        request.setOrderIndex(2);

        when(moduleRepository.findById("module-1")).thenReturn(Optional.of(testModule));
        when(moduleRepository.save(any(Module.class))).thenReturn(testModule);
        when(lessonRepository.findByModuleIdOrderByOrderIndexAsc(anyString())).thenReturn(Collections.emptyList());

        // Act
        ModuleDto result = moduleService.updateModule("module-1", request);

        // Assert
        assertNotNull(result);
        verify(moduleRepository, times(1)).save(any(Module.class));
    }

    @Test
    void deleteModule_ShouldDeleteModuleAndLessons() {
        // Arrange
        when(moduleRepository.existsById("module-1")).thenReturn(true);
        when(lessonRepository.findByModuleIdOrderByOrderIndexAsc("module-1")).thenReturn(Collections.emptyList());
        doNothing().when(moduleRepository).deleteById("module-1");

        // Act
        moduleService.deleteModule("module-1");

        // Assert
        verify(moduleRepository, times(1)).deleteById("module-1");
    }
}
