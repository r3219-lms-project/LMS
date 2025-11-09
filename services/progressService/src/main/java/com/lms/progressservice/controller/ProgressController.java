package com.lms.progressService.controller;

import com.lms.progressService.annotation.RequireAuth;
import com.lms.progressService.dto.CourseProgressStatsDto;
import com.lms.progressService.dto.LessonProgressDto;
import com.lms.progressService.service.ProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/progress")
@RequiredArgsConstructor
@Validated
@Tag(name = "Progress", description = "API для отслеживания прогресса пользователей")
public class ProgressController {

    private final ProgressService progressService;

    @PostMapping("/lessons/{lessonId}/complete")
    @RequireAuth
    @Operation(summary = "Отметить урок как завершенный")
    public ResponseEntity<LessonProgressDto> completeLesson(
            @Parameter(description = "ID урока") @PathVariable @NotNull UUID lessonId,
            @Parameter(description = "ID курса") @RequestParam @NotNull UUID courseId,
            @Parameter(description = "ID пользователя") @RequestHeader("X-User-Id") @NotNull UUID userId) {

        LessonProgressDto progress = progressService.completeLessonProgress(userId, lessonId, courseId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/courses/{courseId}")
    @RequireAuth
    @Operation(summary = "Получить прогресс пользователя по курсу")
    public ResponseEntity<List<LessonProgressDto>> getCourseProgress(
            @Parameter(description = "ID курса") @PathVariable @NotNull UUID courseId,
            @Parameter(description = "ID пользователя") @RequestHeader("X-User-Id") @NotNull UUID userId) {

        List<LessonProgressDto> progress = progressService.getCourseProgress(userId, courseId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/users/me")
    @RequireAuth
    @Operation(summary = "Получить весь прогресс текущего пользователя")
    public ResponseEntity<List<LessonProgressDto>> getAllMyProgress(
            @Parameter(description = "ID пользователя") @RequestHeader("X-User-Id") @NotNull UUID userId) {

        List<LessonProgressDto> progress = progressService.getAllUserProgress(userId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/courses/{courseId}/stats")
    @RequireAuth
    @Operation(summary = "Получить статистику завершения курса")
    public ResponseEntity<CourseProgressStatsDto> getCourseStats(
            @Parameter(description = "ID курса") @PathVariable @NotNull UUID courseId,
            @Parameter(description = "ID пользователя") @RequestHeader("X-User-Id") @NotNull UUID userId) {

        CourseProgressStatsDto stats = progressService.getCourseStats(userId, courseId);
        return ResponseEntity.ok(stats);
    }
}
