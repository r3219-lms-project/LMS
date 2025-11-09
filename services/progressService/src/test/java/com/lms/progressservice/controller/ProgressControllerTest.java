package com.lms.progressService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.progressService.dto.CourseProgressStatsDto;
import com.lms.progressService.dto.LessonProgressDto;
import com.lms.progressService.service.ProgressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProgressController.class)
class ProgressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProgressService progressService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID userId;
    private UUID lessonId;
    private UUID courseId;
    private LessonProgressDto lessonProgressDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        lessonId = UUID.randomUUID();
        courseId = UUID.randomUUID();

        lessonProgressDto = new LessonProgressDto();
        lessonProgressDto.setId(UUID.randomUUID());
        lessonProgressDto.setUserId(userId);
        lessonProgressDto.setLessonId(lessonId);
        lessonProgressDto.setCourseId(courseId);
        lessonProgressDto.setCompleted(true);
        lessonProgressDto.setCompletedAt(LocalDateTime.now());
        lessonProgressDto.setLastAccessedAt(LocalDateTime.now());
    }

    @Test
    void completeLesson_ShouldReturnProgress() throws Exception {
        // Given
        when(progressService.completeLessonProgress(any(UUID.class), any(UUID.class), any(UUID.class)))
            .thenReturn(lessonProgressDto);

        // When & Then
        mockMvc.perform(post("/progress/lessons/{lessonId}/complete", lessonId)
                .param("courseId", courseId.toString())
                .header("X-User-Id", userId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.lessonId").value(lessonId.toString()))
                .andExpect(jsonPath("$.courseId").value(courseId.toString()))
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void getCourseProgress_ShouldReturnProgressList() throws Exception {
        // Given
        List<LessonProgressDto> progressList = Arrays.asList(lessonProgressDto);
        when(progressService.getCourseProgress(any(UUID.class), any(UUID.class)))
            .thenReturn(progressList);

        // When & Then
        mockMvc.perform(get("/progress/courses/{courseId}", courseId)
                .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$[0].courseId").value(courseId.toString()));
    }

    @Test
    void getAllMyProgress_ShouldReturnAllProgress() throws Exception {
        // Given
        List<LessonProgressDto> progressList = Arrays.asList(lessonProgressDto);
        when(progressService.getAllUserProgress(any(UUID.class)))
            .thenReturn(progressList);

        // When & Then
        mockMvc.perform(get("/progress/users/me")
                .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userId").value(userId.toString()));
    }

    @Test
    void getCourseStats_ShouldReturnStats() throws Exception {
        // Given
        CourseProgressStatsDto stats = new CourseProgressStatsDto(courseId, 10L, 6L, 60.0);
        when(progressService.getCourseStats(any(UUID.class), any(UUID.class)))
            .thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/progress/courses/{courseId}/stats", courseId)
                .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(courseId.toString()))
                .andExpect(jsonPath("$.totalLessons").value(10))
                .andExpect(jsonPath("$.completedLessons").value(6))
                .andExpect(jsonPath("$.completionPercentage").value(60.0));
    }
}
