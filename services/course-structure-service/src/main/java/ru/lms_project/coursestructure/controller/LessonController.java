package ru.lms_project.coursestructure.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.lms_project.common.security.annotation.RequireAdmin;
import ru.lms_project.coursestructure.dto.LessonCreateRequest;
import ru.lms_project.coursestructure.dto.LessonDto;
import ru.lms_project.coursestructure.dto.LessonUpdateRequest;
import ru.lms_project.coursestructure.service.LessonService;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @PostMapping("/modules/{moduleId}/lessons")
    @ResponseStatus(HttpStatus.CREATED)
    @RequireAdmin
    public LessonDto createLesson(
            @PathVariable String moduleId,
            @Valid @RequestBody LessonCreateRequest request) {
        return lessonService.createLesson(moduleId, request);
    }

    @GetMapping("/modules/{moduleId}/lessons")
    public List<LessonDto> getLessonsByModule(@PathVariable String moduleId) {
        return lessonService.getLessonsByModuleId(moduleId);
    }

    @GetMapping("/lessons/{id}")
    public LessonDto getLessonById(@PathVariable String id) {
        return lessonService.getLessonById(id);
    }

    @PutMapping("/lessons/{id}")
    @RequireAdmin
    public LessonDto updateLesson(
            @PathVariable String id,
            @Valid @RequestBody LessonUpdateRequest request) {
        return lessonService.updateLesson(id, request);
    }

    @DeleteMapping("/lessons/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequireAdmin
    public void deleteLesson(@PathVariable String id) {
        lessonService.deleteLesson(id);
    }
}
