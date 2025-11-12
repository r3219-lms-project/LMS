//package ru.lms_project.coursestructure.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//import ru.lms_project.coursestructure.model.Module;
//import ru.lms_project.coursestructure.repository.ModuleRepository;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@ActiveProfiles("test")
//class ModuleControllerIntegrationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    private ModuleRepository moduleRepository;
//
//    @BeforeEach
//    void setUp() {
//        moduleRepository.deleteAll();
//    }
//
//    @Test
//    void getModulesByCourse_ShouldReturnEmptyList() throws Exception {
//        mockMvc.perform(get("/api/v1/courses/course-1/modules"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$").isArray())
//                .andExpect(jsonPath("$").isEmpty());
//    }
//
//    @Test
//    void getModulesByCourse_ShouldReturnModulesSortedByOrderIndex() throws Exception {
//        // Arrange
//        Module module1 = new Module();
//        module1.setCourseId("course-1");
//        module1.setTitle("Module 2");
//        module1.setOrderIndex(2);
//        moduleRepository.save(module1);
//
//        Module module2 = new Module();
//        module2.setCourseId("course-1");
//        module2.setTitle("Module 1");
//        module2.setOrderIndex(1);
//        moduleRepository.save(module2);
//
//        // Act & Assert
//        mockMvc.perform(get("/api/v1/courses/course-1/modules"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$").isArray())
//                .andExpect(jsonPath("$.length()").value(2))
//                .andExpect(jsonPath("$[0].title").value("Module 1"))
//                .andExpect(jsonPath("$[0].orderIndex").value(1))
//                .andExpect(jsonPath("$[1].title").value("Module 2"))
//                .andExpect(jsonPath("$[1].orderIndex").value(2));
//    }
//
//    @Test
//    void getModuleById_WhenExists_ShouldReturnModule() throws Exception {
//        // Arrange
//        Module module = new Module();
//        module.setCourseId("course-1");
//        module.setTitle("Test Module");
//        module.setDescription("Test Description");
//        module.setOrderIndex(1);
//        Module saved = moduleRepository.save(module);
//
//        // Act & Assert
//        mockMvc.perform(get("/api/v1/modules/" + saved.getId()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(saved.getId()))
//                .andExpect(jsonPath("$.title").value("Test Module"))
//                .andExpect(jsonPath("$.description").value("Test Description"));
//    }
//
//    @Test
//    void getModuleById_WhenNotExists_ShouldReturn400() throws Exception {
//        mockMvc.perform(get("/api/v1/modules/nonexistent"))
//                .andExpect(status().isBadRequest());
//    }
//}
