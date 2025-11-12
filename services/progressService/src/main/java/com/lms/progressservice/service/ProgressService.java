package com.lms.progressService.service;

import com.lms.progressService.dto.CourseProgressStatsDto;
import com.lms.progressService.dto.LessonProgressDto;
import com.lms.progressService.model.LessonProgress;
import com.lms.progressService.repository.LessonProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProgressService {

    private final LessonProgressRepository lessonProgressRepository;

    public LessonProgressDto completeLessonProgress(UUID userId, UUID lessonId, UUID courseId) {
        LessonProgress progress = lessonProgressRepository
            .findByUserIdAndLessonId(userId, lessonId)
            .orElse(new LessonProgress());

        progress.setUserId(userId);
        progress.setLessonId(lessonId);
        progress.setCourseId(courseId);
        progress.setCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());
        progress.setLastAccessedAt(LocalDateTime.now());

        LessonProgress saved = lessonProgressRepository.save(progress);
        return convertToDto(saved);
    }

    public List<LessonProgressDto> getCourseProgress(UUID userId, UUID courseId) {
        List<LessonProgress> progressList = lessonProgressRepository
            .findByUserIdAndCourseId(userId, courseId);

        return progressList.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    public List<LessonProgressDto> getAllUserProgress(UUID userId) {
        List<LessonProgress> progressList = lessonProgressRepository
            .findByUserId(userId);

        return progressList.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    public CourseProgressStatsDto getCourseStats(UUID userId, UUID courseId) {
        long completedLessons = lessonProgressRepository
            .countCompletedLessonsByCourseAndUser(userId, courseId);
        long totalLessons = lessonProgressRepository
            .countTotalLessonsByCourse(courseId);

        double completionPercentage = totalLessons > 0 ?
            (double) completedLessons / totalLessons * 100 : 0.0;

        return new CourseProgressStatsDto(courseId, totalLessons,
            completedLessons, Math.round(completionPercentage * 100.0) / 100.0);
    }

    private LessonProgressDto convertToDto(LessonProgress progress) {
        return new LessonProgressDto(
            progress.getId(),
            progress.getUserId(),
            progress.getLessonId(),
            progress.getCourseId(),
            progress.getCompleted(),
            progress.getCompletedAt(),
            progress.getLastAccessedAt()
        );
    }
}
