package com.lms.progressService.service;

import com.lms.progressService.dto.CourseProgressStatsDto;
import com.lms.progressService.dto.LessonProgressDto;
import com.lms.progressService.model.LessonProgress;
import com.lms.progressService.repository.LessonProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgressServiceTest {

    @Mock
    private LessonProgressRepository lessonProgressRepository;

    @InjectMocks
    private ProgressService progressService;

    private UUID userId;
    private UUID lessonId;
    private UUID courseId;
    private LessonProgress lessonProgress;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        lessonId = UUID.randomUUID();
        courseId = UUID.randomUUID();

        lessonProgress = new LessonProgress();
        lessonProgress.setId(UUID.randomUUID());
        lessonProgress.setUserId(userId);
        lessonProgress.setLessonId(lessonId);
        lessonProgress.setCourseId(courseId);
        lessonProgress.setCompleted(true);
        lessonProgress.setCompletedAt(LocalDateTime.now());
        lessonProgress.setLastAccessedAt(LocalDateTime.now());
    }

    @Test
    void completeLessonProgress_ShouldCreateNewProgress_WhenNotExists() {
        // Given
        when(lessonProgressRepository.findByUserIdAndLessonId(userId, lessonId))
            .thenReturn(Optional.empty());
        when(lessonProgressRepository.save(any(LessonProgress.class)))
            .thenReturn(lessonProgress);

        // When
        LessonProgressDto result = progressService.completeLessonProgress(userId, lessonId, courseId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getLessonId()).isEqualTo(lessonId);
        assertThat(result.getCourseId()).isEqualTo(courseId);
        assertThat(result.getCompleted()).isTrue();

        verify(lessonProgressRepository).save(any(LessonProgress.class));
    }

    @Test
    void getCourseProgress_ShouldReturnProgressList() {
        // Given
        List<LessonProgress> progressList = Arrays.asList(lessonProgress);
        when(lessonProgressRepository.findByUserIdAndCourseId(userId, courseId))
            .thenReturn(progressList);

        // When
        List<LessonProgressDto> result = progressService.getCourseProgress(userId, courseId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(userId);
        assertThat(result.get(0).getCourseId()).isEqualTo(courseId);
    }

    @Test
    void getCourseStats_ShouldCalculateCorrectPercentage() {
        // Given
        when(lessonProgressRepository.countCompletedLessonsByCourseAndUser(userId, courseId))
            .thenReturn(3L);
        when(lessonProgressRepository.countTotalLessonsByCourse(courseId))
            .thenReturn(5L);

        // When
        CourseProgressStatsDto result = progressService.getCourseStats(userId, courseId);

        // Then
        assertThat(result.getCourseId()).isEqualTo(courseId);
        assertThat(result.getTotalLessons()).isEqualTo(5L);
        assertThat(result.getCompletedLessons()).isEqualTo(3L);
        assertThat(result.getCompletionPercentage()).isEqualTo(60.0);
    }
}
