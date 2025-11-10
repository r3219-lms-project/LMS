package ru.lms_project.coursestructure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.lms_project.coursestructure.model.Lesson;
import ru.lms_project.coursestructure.model.LessonType;
import ru.lms_project.coursestructure.model.Module;
import ru.lms_project.coursestructure.repository.LessonRepository;
import ru.lms_project.coursestructure.repository.ModuleRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LessonControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    private String moduleId;

    @BeforeEach
    void setUp() {
        lessonRepository.deleteAll();
        moduleRepository.deleteAll();


        Module module = new Module();
        module.setCourseId("course-1");
        module.setTitle("Test Module");
        module.setOrderIndex(1);
        Module savedModule = moduleRepository.save(module);
        moduleId = savedModule.getId();
    }

    @Test
    void getLessonsByModule_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/modules/" + moduleId + "/lessons"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getLessonsByModule_ShouldReturnLessonsSortedByOrderIndex() throws Exception {
        Lesson lesson1 = new Lesson();
        lesson1.setModuleId(moduleId);
        lesson1.setTitle("Lesson 2");
        lesson1.setType(LessonType.VIDEO);
        lesson1.setOrderIndex(2);
        lessonRepository.save(lesson1);

        Lesson lesson2 = new Lesson();
        lesson2.setModuleId(moduleId);
        lesson2.setTitle("Lesson 1");
        lesson2.setType(LessonType.TEXT);
        lesson2.setOrderIndex(1);
        lessonRepository.save(lesson2);

        mockMvc.perform(get("/api/v1/modules/" + moduleId + "/lessons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Lesson 1"))
                .andExpect(jsonPath("$[0].orderIndex").value(1))
                .andExpect(jsonPath("$[1].title").value("Lesson 2"))
                .andExpect(jsonPath("$[1].orderIndex").value(2));
    }

    @Test
    void getLessonById_WhenExists_ShouldReturnLesson() throws Exception {
        Lesson lesson = new Lesson();
        lesson.setModuleId(moduleId);
        lesson.setTitle("Test Lesson");
        lesson.setContent("Test Content");
        lesson.setType(LessonType.VIDEO);
        lesson.setDuration(30);
        lesson.setOrderIndex(1);
        lesson.setVideoUrl("https://example.com/video.mp4");
        Lesson saved = lessonRepository.save(lesson);

        mockMvc.perform(get("/api/v1/lessons/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.title").value("Test Lesson"))
                .andExpect(jsonPath("$.type").value("VIDEO"))
                .andExpect(jsonPath("$.duration").value(30));
    }

    @Test
    void getLessonById_WhenNotExists_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/lessons/nonexistent"))
                .andExpect(status().isBadRequest());
    }
}
